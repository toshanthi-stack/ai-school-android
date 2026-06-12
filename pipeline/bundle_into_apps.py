"""Wire a generated feed (out/syllabus.json + out/audio/) into both apps so the
content ships bundled (offline, no hosting):

  iOS      -> ios/AISchool/Resources/syllabus.json   + Resources/Audio/*.m4a
  Android  -> core/model .../SeedSyllabus.kt          + core/demoaudio res/raw/*.m4a

Both apps prefer the live feed and fall back to this bundled content, so this is
the "free, no hosting" path. Re-run after each `make feed` to refresh the apps.

Cosmetic cleanup applied here (titles come from URL slugs): acronym casing,
version dots, and using each lesson's audioSummary as the course description.
"""
from __future__ import annotations

import json
import os
import shutil

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(HERE)

IOS_RES = os.path.join(REPO, "ios", "AISchool", "Resources")
IOS_AUDIO = os.path.join(IOS_RES, "Audio")
AND_RAW = os.path.join(REPO, "android", "core", "demoaudio", "src", "main", "res", "raw")
AND_SEED = os.path.join(
    REPO, "android", "core", "model", "src", "main", "kotlin",
    "com", "lillytech", "aischool", "core", "model", "SeedSyllabus.kt",
)

# Explicit title fixes for slugs the generic titleizer cannot get right.
TITLE_OVERRIDES = {
    "ai-tools-github-copilot-tool": "GitHub Copilot",
    "ai-tools-continue-dev": "Continue",
    "ai-tools-cline-vscode": "Cline (VS Code)",
    "ai-tools-cursor-editor": "Cursor",
    "ai-models-gpt-5": "GPT-5",
    "ai-models-gpt-4o": "GPT-4o",
    "ai-models-claude-opus-4-7": "Claude Opus 4.7",
    "ai-models-claude-sonnet-4-6": "Claude Sonnet 4.6",
    "ai-models-claude-haiku-4-5": "Claude Haiku 4.5",
    "ai-models-gemini-2-5-pro": "Gemini 2.5 Pro",
}


def clean_title(lesson_id: str, title: str) -> str:
    return TITLE_OVERRIDES.get(lesson_id, title)


def cleaned_courses(feed: list) -> list:
    out = []
    for course in feed:
        lessons = []
        first_summary = ""
        for i, lesson in enumerate(course.get("lessons", [])):
            lid = lesson["id"]
            lesson = dict(lesson)
            lesson["title"] = clean_title(lid, lesson["title"])
            if i == 0:
                first_summary = lesson.get("audioSummary", "")
            lessons.append(lesson)
        course = dict(course)
        course["title"] = clean_title(course["id"], course["title"])
        # A real one-line summary reads better than "<title>, part of <path>".
        if first_summary:
            course["description"] = first_summary
        course["lessons"] = lessons
        out.append(course)
    return out


def kotlin_escape(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$")


def kt_str(s) -> str:
    return "null" if s is None else f'"{kotlin_escape(s)}"'


def emit_kotlin(courses: list) -> str:
    L = []
    L.append("package com.lillytech.aischool.core.model")
    L.append("")
    L.append("/**")
    L.append(" * Structured mirror of the AI School catalog, generated from the content")
    L.append(" * pipeline's feed by `pipeline/bundle_into_apps.py`.")
    L.append(" *")
    L.append(" * The network layer prefers the live `syllabus.json` feed; this seed keeps")
    L.append(" * both app flavors fully functional offline (and is the bundled, no-hosting")
    L.append(" * content of record). Do not hand-edit; re-run the bundler to refresh.")
    L.append(" */")
    L.append("object SeedSyllabus {")
    L.append("")
    L.append("    val courses: List<Course> = listOf(")
    for c in courses:
        L.append("        Course(")
        L.append(f'            id = {kt_str(c["id"])},')
        L.append(f'            title = {kt_str(c["title"])},')
        L.append(f'            description = {kt_str(c["description"])},')
        L.append(f'            category = {kt_str(c["category"])},')
        L.append("            lessons = listOf(")
        for le in c["lessons"]:
            L.append("                Lesson(")
            L.append(f'                    id = {kt_str(le["id"])},')
            L.append(f'                    title = {kt_str(le["title"])},')
            L.append(f'                    durationSeconds = {int(le["durationSeconds"])},')
            L.append(f'                    audioUrl = {kt_str(le.get("audioUrl", ""))},')
            L.append(f'                    visualContentUrl = {kt_str(le.get("visualContentUrl"))},')
            L.append(f'                    isAutomotiveSafe = {"true" if le.get("isAutomotiveSafe", True) else "false"},')
            L.append(f'                    audioSummary = {kt_str(le.get("audioSummary", ""))},')
            L.append("                ),")
        L.append("            ),")
        L.append("        ),")
    L.append("    )")
    L.append("}")
    L.append("")
    return "\n".join(L)


def copy_audio(audio_dir: str) -> int:
    os.makedirs(IOS_AUDIO, exist_ok=True)
    os.makedirs(AND_RAW, exist_ok=True)
    n = 0
    for name in sorted(os.listdir(audio_dir)):
        if not name.endswith(".m4a"):
            continue
        src = os.path.join(audio_dir, name)
        shutil.copy2(src, os.path.join(IOS_AUDIO, name))
        shutil.copy2(src, os.path.join(AND_RAW, name))
        n += 1
    return n


def main() -> None:
    feed_path = os.path.join(HERE, "out", "syllabus.json")
    audio_dir = os.path.join(HERE, "out", "audio")
    feed = json.load(open(feed_path))
    courses = cleaned_courses(feed)

    # iOS bundled feed (cleaned).
    os.makedirs(IOS_RES, exist_ok=True)
    with open(os.path.join(IOS_RES, "syllabus.json"), "w") as f:
        json.dump(courses, f, indent=2)

    # Android Kotlin seed (generated).
    with open(AND_SEED, "w") as f:
        f.write(emit_kotlin(courses))

    n_audio = copy_audio(audio_dir)
    n_lessons = sum(len(c["lessons"]) for c in courses)
    print(f"bundled {len(courses)} courses / {n_lessons} lessons")
    print(f"  iOS feed   -> {os.path.relpath(os.path.join(IOS_RES, 'syllabus.json'), REPO)}")
    print(f"  Android seed -> {os.path.relpath(AND_SEED, REPO)}")
    print(f"  audio copied -> {n_audio} files into iOS Resources/Audio and Android res/raw")


if __name__ == "__main__":
    main()
