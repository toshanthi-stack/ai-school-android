"""Publish the generated feed to the GitHub Pages content repo (ai-school-feed).

After `make feed ... AUDIO_URL_BASE=https://toshanthi-stack.github.io/ai-school-feed/audio`
this cleans the titles, copies syllabus.json + the referenced audio into the
local feed-repo clone, commits, and pushes. GitHub Pages then serves it and the
apps pick up the new tracks automatically (no app rebuild or resubmit).

Default feed-repo clone: ~/ai-school-feed  (override with FEED_REPO env var).
"""
from __future__ import annotations

import json
import os
import shutil
import subprocess

import bundle_into_apps as b

HERE = os.path.dirname(os.path.abspath(__file__))
FEED_REPO = os.environ.get("FEED_REPO", os.path.expanduser("~/ai-school-feed"))


def main() -> None:
    out_feed = os.path.join(HERE, "out", "syllabus.json")
    audio_dir = os.path.join(HERE, "out", "audio")
    if not os.path.exists(out_feed):
        raise SystemExit("no out/syllabus.json - run `make feed ... AUDIO_URL_BASE=...` first")
    if not os.path.isdir(FEED_REPO):
        raise SystemExit(f"feed repo clone not found at {FEED_REPO} "
                         f"(clone it: gh repo clone toshanthi-stack/ai-school-feed {FEED_REPO})")

    courses = b.cleaned_courses(json.load(open(out_feed)))
    json.dump(courses, open(os.path.join(FEED_REPO, "syllabus.json"), "w"), indent=2)

    # Sync audio: the feed repo should hold exactly the feed's referenced files.
    dest = os.path.join(FEED_REPO, "audio")
    os.makedirs(dest, exist_ok=True)
    wanted = set(b.referenced_audio(courses))
    for f in os.listdir(dest):
        if f.endswith(".m4a") and f not in wanted:
            os.remove(os.path.join(dest, f))
    copied = 0
    for name in wanted:
        src = os.path.join(audio_dir, name)
        if os.path.exists(src):
            shutil.copy2(src, os.path.join(dest, name))
            copied += 1
        else:
            print(f"  WARN missing audio: {name}")

    n_lessons = sum(len(c["lessons"]) for c in courses)
    print(f"staged {len(courses)} courses / {n_lessons} lessons, {copied} audio -> {FEED_REPO}")

    subprocess.run(["git", "-C", FEED_REPO, "add", "-A"], check=True)
    msg = f"feed: {len(courses)} courses / {n_lessons} lessons"
    r = subprocess.run(["git", "-C", FEED_REPO, "commit", "-q", "-m", msg])
    if r.returncode != 0:
        print("nothing to commit (feed unchanged)")
        return
    subprocess.run(["git", "-C", FEED_REPO, "push", "-q"], check=True)
    print("pushed. GitHub Pages will update in ~1-2 min; the apps pick it up automatically.")


if __name__ == "__main__":
    main()
