"""Crawl the AI School site into a lessons.json for the content pipeline.

Site structure (three real levels):
  /ai-school/index.html        -> learning paths (`../<path>/index.html`)
  /<path>/index.html           -> topic pages    (`<topic>/index.html`)
  /<path>/<topic>/index.html   -> a stub linking ~6 lesson pages (`<lesson>.html`)
  /<path>/<topic>/<lesson>.html-> one real lesson (the actual content, ~8k chars)

Mapping to lessons.json (which the content pipeline consumes):
  category = learning path,  course = topic,  lesson = each real lesson page.

So one topic becomes a course of ~6 audio lessons (each a real page, not the
stub). Use --max-paths / --max-topics / --max-lessons to crawl a manageable
subset first (the full catalog is thousands of pages). Audio is bundled in the
apps, so keep batches modest (~100 lessons) unless you host the audio.
"""
from __future__ import annotations

import argparse
import json
import re
import sys
import time
from urllib.parse import urljoin

import requests
from bs4 import BeautifulSoup

BASE = "https://www.lillytechsystems.com"
INDEX = f"{BASE}/ai-school/index.html"

DIFFICULTY = ("Beginner", "Intermediate", "Advanced", "Expert")

# Tokens to upper-case when turning a slug into a title.
ACRONYMS = {
    "ai", "api", "apis", "llm", "llms", "gpu", "gpus", "rag", "cuda", "mcp",
    "sdk", "ui", "ux", "cli", "aws", "gcp", "ml", "nlp", "io", "db", "hnsw",
    "ivf", "tpu", "npu", "sql", "etl", "rl", "xai", "3d", "ar", "vr", "iot",
    "gpt",
}


def titleize(slug: str) -> str:
    parts = slug.replace("_", "-").split("-")
    out = " ".join(p.upper() if p.lower() in ACRONYMS else p.capitalize() for p in parts)
    # Join version digit runs: "4 7" -> "4.7", "2 5" -> "2.5".
    return re.sub(r"(?<=\d) (?=\d)", ".", out)


def clean_link_title(text: str, slug: str) -> str:
    """Title from an anchor: drop a leading icon and a trailing difficulty badge."""
    t = re.sub(r"^[^0-9A-Za-z]+", "", text.strip())
    for d in DIFFICULTY:
        if t.endswith(d):
            t = t[: -len(d)]
    t = re.sub(r"[^0-9A-Za-z]+$", "", t).strip()
    return t


def get(url: str, retries: int = 2) -> BeautifulSoup | None:
    for _ in range(retries + 1):
        try:
            resp = requests.get(url, timeout=20,
                               headers={"User-Agent": "aischool-crawler/1.0"})
            if resp.ok:
                return BeautifulSoup(resp.text, "html.parser")
        except requests.RequestException:
            pass
        time.sleep(0.5)
    return None


def _unique_links(soup: BeautifulSoup, pattern: str) -> list[tuple[str, str]]:
    """Return [(slug, href)] for hrefs matching `pattern` (slug in group 1), deduped."""
    seen: dict[str, str] = {}
    for a in soup.find_all("a", href=True):
        m = re.match(pattern, a["href"])
        if m and m.group(1) not in seen:
            seen[m.group(1)] = a["href"]
    return list(seen.items())


def topic_lessons(topic_url: str, soup: BeautifulSoup, max_lessons: int) -> list[tuple[str, str]]:
    """Ordered [(title, url)] of the real lesson pages linked from a topic stub."""
    order: list[str] = []
    titles: dict[str, str] = {}
    hrefs: dict[str, str] = {}
    for a in soup.find_all("a", href=True):
        m = re.match(r"^([a-z0-9][a-z0-9-]*)\.html$", a["href"])
        if not m:
            continue
        slug = m.group(1)
        if slug == "index":
            continue
        if slug not in titles:
            order.append(slug)
            titles[slug] = ""
            hrefs[slug] = a["href"]
        title = clean_link_title(a.get_text(), slug)
        if title and title.lower() != "start topic" and not titles[slug]:
            titles[slug] = title
    lessons = [(titles[s] or titleize(s), urljoin(topic_url, hrefs[s])) for s in order]
    return lessons[:max_lessons] if max_lessons else lessons


def lesson_id(url: str) -> str:
    path = url.replace(BASE, "").strip("/").replace("/index.html", "").replace(".html", "")
    return re.sub(r"[^a-z0-9]+", "-", path).strip("-")


def crawl(max_paths: int, max_topics: int, max_lessons: int, include_core: bool, out: str) -> None:
    index = get(INDEX)
    if index is None:
        sys.exit("could not fetch the main index")

    paths = _unique_links(index, r"^\.\./([a-z0-9-]+)/index\.html$")
    sources = [(titleize(slug), urljoin(INDEX, href)) for slug, href in paths]
    if max_paths:
        sources = sources[:max_paths]
    print(f"learning paths to crawl: {len(sources)}")

    courses = []
    n_lessons = 0
    for category, path_url in sources:
        soup = get(path_url)
        if soup is None:
            print(f"  skip {category} (fetch failed)")
            continue
        topic_links = _unique_links(soup, r"^([a-z0-9-]+)/index\.html$")
        if max_topics:
            topic_links = topic_links[:max_topics]
        print(f"  {category}: {len(topic_links)} topics")

        for slug, href in topic_links:
            topic_url = urljoin(path_url, href)
            topic_soup = get(topic_url)
            if topic_soup is None:
                continue
            lessons = topic_lessons(topic_url, topic_soup, max_lessons)
            if not lessons:
                # No sub-lessons found: fall back to the topic page itself.
                lessons = [(titleize(slug), topic_url)]
            course_title = titleize(slug)
            courses.append({
                "id": lesson_id(topic_url),
                "title": course_title,
                "description": f"{course_title}, part of {category}.",
                "category": category,
                "lessons": [{
                    "id": lesson_id(url),
                    "title": title,
                    "webUrl": url,
                } for title, url in lessons],
            })
            n_lessons += len(lessons)
            print(f"    {course_title}: {len(lessons)} lessons")

    with open(out, "w") as f:
        json.dump({"courses": courses}, f, indent=2)
    print(f"wrote {out}: {len(courses)} courses / {n_lessons} lessons")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--max-paths", type=int, default=0,
                       help="limit number of learning paths (0 = all)")
    parser.add_argument("--max-topics", type=int, default=0,
                       help="limit topics (courses) per path (0 = all)")
    parser.add_argument("--max-lessons", type=int, default=0,
                       help="limit lessons per topic (0 = all, usually ~6)")
    parser.add_argument("--include-core", action="store_true",
                       help="(reserved) include the AI School Core course pages")
    parser.add_argument("--out", default="lessons.json")
    args = parser.parse_args()
    crawl(args.max_paths, args.max_topics, args.max_lessons, args.include_core, args.out)
