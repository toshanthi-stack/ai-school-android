"""Text-to-speech for the content pipeline.

Backend is chosen by the TTS_BACKEND env var, else auto-detected from whichever
API key is present:

  elevenlabs  ElevenLabs, production narration   ELEVENLABS_API_KEY   -> mp3
  openai      OpenAI TTS                          OPENAI_API_KEY       -> mp3
  say         macOS `say` (local prototype)       no key, no network   -> m4a

`synthesize(text, out_base)` writes `<out_base>.<ext>` (extension depends on the
backend) and returns `(path, duration_seconds)`. Both mp3 and m4a play natively
on iOS (AVPlayer) and Android (MediaPlayer), so the feed can reference either.

Optional env overrides:
  ELEVENLABS_VOICE_ID  (default Rachel), ELEVENLABS_MODEL (default eleven_turbo_v2_5)
  OPENAI_TTS_VOICE     (default alloy),  OPENAI_TTS_MODEL (default gpt-4o-mini-tts)
"""
from __future__ import annotations

import os
import re
import shutil
import subprocess
import tempfile

import requests

ELEVENLABS_VOICE = os.environ.get("ELEVENLABS_VOICE_ID", "21m00Tcm4TlvDq8ikWAM")  # "Rachel"
ELEVENLABS_MODEL = os.environ.get("ELEVENLABS_MODEL", "eleven_turbo_v2_5")
OPENAI_VOICE = os.environ.get("OPENAI_TTS_VOICE", "alloy")
OPENAI_MODEL = os.environ.get("OPENAI_TTS_MODEL", "gpt-4o-mini-tts")


def select_backend() -> str:
    backend = os.environ.get("TTS_BACKEND")
    if backend:
        return backend
    if os.environ.get("ELEVENLABS_API_KEY"):
        return "elevenlabs"
    if os.environ.get("OPENAI_API_KEY"):
        return "openai"
    return "say"


def synthesize(text: str, out_base: str) -> tuple[str, int]:
    """Synthesize `text` to audio next to `out_base`; return (path, duration_seconds)."""
    backend = select_backend()
    if backend == "elevenlabs":
        path = _elevenlabs(text, out_base + ".mp3")
    elif backend == "openai":
        path = _openai(text, out_base + ".mp3")
    elif backend == "say":
        path = _say(text, out_base + ".m4a")
    else:
        raise ValueError(f"unknown TTS_BACKEND: {backend!r} (use elevenlabs | openai | say)")
    return path, _duration_seconds(path)


def _elevenlabs(text: str, out_path: str) -> str:
    key = os.environ.get("ELEVENLABS_API_KEY")
    if not key:
        raise RuntimeError("ELEVENLABS_API_KEY is not set")
    resp = requests.post(
        f"https://api.elevenlabs.io/v1/text-to-speech/{ELEVENLABS_VOICE}",
        headers={"xi-api-key": key, "accept": "audio/mpeg", "content-type": "application/json"},
        params={"output_format": "mp3_44100_128"},
        json={"text": text, "model_id": ELEVENLABS_MODEL},
        timeout=120,
    )
    resp.raise_for_status()
    with open(out_path, "wb") as f:
        f.write(resp.content)
    return out_path


def _openai(text: str, out_path: str) -> str:
    key = os.environ.get("OPENAI_API_KEY")
    if not key:
        raise RuntimeError("OPENAI_API_KEY is not set")
    resp = requests.post(
        "https://api.openai.com/v1/audio/speech",
        headers={"authorization": f"Bearer {key}", "content-type": "application/json"},
        json={"model": OPENAI_MODEL, "voice": OPENAI_VOICE,
              "input": text, "response_format": "mp3"},
        timeout=120,
    )
    resp.raise_for_status()
    with open(out_path, "wb") as f:
        f.write(resp.content)
    return out_path


def _say(text: str, out_path: str, voice: str = "Samantha") -> str:
    if not (shutil.which("say") and shutil.which("afconvert")):
        raise RuntimeError(
            "macOS say/afconvert not found; set TTS_BACKEND=elevenlabs or openai"
        )
    with tempfile.TemporaryDirectory() as tmp:
        aiff = os.path.join(tmp, "tts.aiff")
        subprocess.run(["say", "-v", voice, "-o", aiff, text], check=True)
        subprocess.run(["afconvert", aiff, out_path, "-f", "m4af", "-d", "aac"], check=True)
    return out_path


def _duration_seconds(path: str) -> int:
    """Audio duration in seconds. Cross-platform via mutagen; macOS afinfo fallback."""
    try:
        from mutagen import File as MutagenFile  # type: ignore

        audio = MutagenFile(path)
        if audio is not None and audio.info is not None:
            length = getattr(audio.info, "length", 0) or 0
            if length:
                return round(length)
    except Exception:
        pass
    if shutil.which("afinfo"):
        out = subprocess.run(["afinfo", path], capture_output=True, text=True).stdout
        match = re.search(r"estimated duration:\s*([\d.]+)", out)
        if match:
            return round(float(match.group(1)))
    return 0
