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

# Word-level casing fixes for titles derived from URL slugs.
ACRONYM_FIX = {
    "Gpt": "GPT", "Api": "API", "Apis": "APIs", "Llm": "LLM", "Llms": "LLMs",
    "Sdk": "SDK", "Gpu": "GPU", "Gpus": "GPUs", "Ai": "AI", "Ui": "UI",
    "Ux": "UX", "Cli": "CLI", "Aws": "AWS", "Gcp": "GCP", "Mcp": "MCP",
    "Rag": "RAG", "Ml": "ML", "Nlp": "NLP", "Iot": "IoT", "Sql": "SQL",
    "Cuda": "CUDA", "Vscode": "VS Code", "Github": "GitHub", "Tpu": "TPU",
}


def clean_title(title: str) -> str:
    words = [ACRONYM_FIX.get(w, w) for w in title.split()]
    out = " ".join(words)
    # Join version digit runs: "4 7" -> "4.7".
    import re
    return re.sub(r"(?<=\d) (?=\d)", ".", out)


def cleaned_courses(feed: list) -> list:
    out = []
    for course in feed:
        lessons = []
        first_summary = ""
        for i, lesson in enumerate(course.get("lessons", [])):
            lesson = dict(lesson)
            lesson["title"] = clean_title(lesson["title"])
            if i == 0:
                first_summary = lesson.get("audioSummary", "")
            lessons.append(lesson)
        course = dict(course)
        course["title"] = clean_title(course["title"])
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
    # Sync: the app audio dirs should contain exactly this feed's narration, so
    # clear previously-bundled .m4a first (no orphans from earlier batches).
    for d in (IOS_AUDIO, AND_RAW):
        os.makedirs(d, exist_ok=True)
        for f in os.listdir(d):
            if f.endswith(".m4a"):
                os.remove(os.path.join(d, f))
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
