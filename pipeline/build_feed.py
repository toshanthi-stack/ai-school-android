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


def build(config: str, audio_dir: str, out: str, audio_url_base: str = "") -> None:
    with open(config) as f:
        cfg = json.load(f)

    os.makedirs(audio_dir, exist_ok=True)
    os.makedirs(os.path.dirname(out) or ".", exist_ok=True)
    client = anthropic.Anthropic()  # reads ANTHROPIC_API_KEY

    courses = []
    for course in cfg["courses"]:
        lessons = []
        for lesson in course["lessons"]:
            url = lesson["webUrl"]
            print(f"  adapting {lesson['id']:<22} <- {url}")
            adaptation = adapt(url, client)

            audio_base = os.path.join(audio_dir, lesson["id"].replace("-", "_"))
            audio_path, duration = synthesize(adaptation.spoken_script, audio_base)
            audio_name = os.path.basename(audio_path)
            print(f"    -> {adaptation.content_type:<10} {duration}s  {audio_name}")

            audio_url = lesson.get("audioUrl", "")
            if audio_url_base:
                audio_url = f"{audio_url_base.rstrip('/')}/{audio_name}"

            lessons.append({
                "id": lesson["id"],
                "title": lesson["title"],
                "durationSeconds": duration,
                "audioUrl": audio_url,
                "visualContentUrl": url,
                "isAutomotiveSafe": True,
                "contentType": adaptation.content_type,
                "audioSummary": adaptation.audio_summary,
            })

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
    print(f"wrote {out}: {len(courses)} courses, {n_lessons} lessons; audio in {audio_dir}/")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--config", default="lessons.json")
    parser.add_argument("--audio-dir", default="out/audio")
    parser.add_argument("--out", default="out/syllabus.json")
    parser.add_argument("--audio-url-base", default="",
                       help="public base URL where the audio will be hosted; "
                            "sets each lesson's audioUrl to <base>/<file>")
    build(**vars(parser.parse_args()))
