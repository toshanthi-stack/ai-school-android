"""Text-to-speech for the pipeline.

Prototype backend: macOS `say` + `afconvert` (same as the bundled demo audio).
For production, replace `synthesize` with a real TTS (ElevenLabs, Azure, Google,
OpenAI, etc.) that returns an .m4a/.mp3 - keep the signature so build_feed.py is
unchanged.
"""
from __future__ import annotations

import os
import re
import shutil
import subprocess
import tempfile


def synthesize(text: str, out_path: str, voice: str = "Samantha") -> int:
    """Write an m4a narration of `text` to `out_path`; return duration (seconds)."""
    if not (shutil.which("say") and shutil.which("afconvert")):
        raise RuntimeError(
            "macOS `say`/`afconvert` not found. Plug a production TTS into tts.py."
        )
    with tempfile.TemporaryDirectory() as tmp:
        aiff = os.path.join(tmp, "tts.aiff")
        subprocess.run(["say", "-v", voice, "-o", aiff, text], check=True)
        subprocess.run(["afconvert", aiff, out_path, "-f", "m4af", "-d", "aac"], check=True)
    return _duration_seconds(out_path)


def _duration_seconds(path: str) -> int:
    out = subprocess.run(["afinfo", path], capture_output=True, text=True).stdout
    match = re.search(r"estimated duration:\s*([\d.]+)", out)
    return round(float(match.group(1))) if match else 0
