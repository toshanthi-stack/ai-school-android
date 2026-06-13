"""Build syllabus.json + audio by running each configured lesson through the
adaptation pipeline (fetch -> classify + rewrite via Claude -> TTS).

Input:  lessons.json  (courses -> lessons with id, title, webUrl)
Output: out/syllabus.json + out/audio/<id>.m4a

The output syllabus.json matches the feed the apps consume; host it (and the
audio) at lillytechsystems.com and the apps load it automatically.
"""
from __future__ import annotations

import argparse
import json
import os

import anthropic

from adapt_lesson import adapt
from tts import synthesize


def _cached_audio(audio_base: str) -> str | None:
    """Existing audio file for a lesson (.m4a or .mp3), if already generated."""
    for ext in (".m4a", ".mp3"):
        if os.path.exists(audio_base + ext):
            return audio_base + ext
    return None


def _load_existing(out: str) -> dict:
    """Map lesson id -> prior feed entry, so re-runs can skip what's done."""
    by_id: dict = {}
    if os.path.exists(out):
        try:
            with open(out) as f:
                for course in json.load(f):
                    for lesson in course.get("lessons", []):
                        by_id[lesson["id"]] = lesson
        except Exception:
            pass
    return by_id


def build(config: str, audio_dir: str, out: str, audio_url_base: str = "") -> None:
    with open(config) as f:
        cfg = json.load(f)

    os.makedirs(audio_dir, exist_ok=True)
    os.makedirs(os.path.dirname(out) or ".", exist_ok=True)

    # Incremental: reuse any lesson already in out/ (entry + audio on disk) so a
    # re-run only spends Claude/TTS on NEW lessons. The Anthropic client is
    # created lazily, so a fully-cached run needs no API call.
    existing = _load_existing(out)
    client = None
    reused = 0
    built = 0

    courses = []
    for course in cfg["courses"]:
        lessons = []
        for lesson in course["lessons"]:
            lid = lesson["id"]
            url = lesson["webUrl"]
            audio_base = os.path.join(audio_dir, lid.replace("-", "_"))
            cached = _cached_audio(audio_base)

            if lid in existing and cached:
                entry = dict(existing[lid])
                # Keep title/source fresh from the config; reuse the rest.
                entry["title"] = lesson["title"]
                entry["visualContentUrl"] = url
                if audio_url_base:
                    entry["audioUrl"] = f"{audio_url_base.rstrip('/')}/{os.path.basename(cached)}"
                lessons.append(entry)
                reused += 1
                continue

            if client is None:
                client = anthropic.Anthropic()  # reads ANTHROPIC_API_KEY
            print(f"  adapting {lid:<28} <- {url}")
            adaptation = adapt(url, client)
            audio_path, duration = synthesize(adaptation.spoken_script, audio_base)
            audio_name = os.path.basename(audio_path)
            print(f"    -> {adaptation.content_type:<10} {duration}s  {audio_name}")

            audio_url = lesson.get("audioUrl", "")
            if audio_url_base:
                audio_url = f"{audio_url_base.rstrip('/')}/{audio_name}"

            lessons.append({
                "id": lid,
                "title": lesson["title"],
                "durationSeconds": duration,
                "audioUrl": audio_url,
                "visualContentUrl": url,
                "isAutomotiveSafe": True,
                "contentType": adaptation.content_type,
                "audioSummary": adaptation.audio_summary,
            })
            built += 1

        courses.append({
            "id": course["id"],
            "title": course["title"],
            "description": course["description"],
            "category": course["category"],
            "lessons": lessons,
        })

    with open(out, "w") as f:
        json.dump(courses, f, indent=2)
    n_lessons = sum(len(c["lessons"]) for c in courses)
    print(f"wrote {out}: {len(courses)} courses, {n_lessons} lessons "
          f"({built} new, {reused} reused); audio in {audio_dir}/")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--config", default="lessons.json")
    parser.add_argument("--audio-dir", default="out/audio")
    parser.add_argument("--out", default="out/syllabus.json")
    parser.add_argument("--audio-url-base", default="",
                       help="public base URL where the audio will be hosted; "
                            "sets each lesson's audioUrl to <base>/<file>")
    build(**vars(parser.parse_args()))
