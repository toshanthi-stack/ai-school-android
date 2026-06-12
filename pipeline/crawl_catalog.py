"""Crawl the AI School site into a lessons.json for the content pipeline.

Site structure:
  /ai-school/index.html         -> learning paths (`../<path>/index.html`)
                                   + core courses (`<slug>/index.html`)
  /<path>/index.html            -> topic pages   (`<topic>/index.html`)
  /<path>/<topic>/index.html    -> one lesson    (numbered sections within)

Mapping to lessons.json (which the content pipeline consumes):
  category = learning path,  course = topic,  lesson = the topic page.

The pipeline summarizes each topic page into a single spoken lesson, so one
topic = one audio lesson. Use --max-paths / --max-topics to crawl a manageable
subset first (the full catalog is ~2,000+ pages).
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

# Tokens to upper-case when turning a slug into a title.
ACRONYMS = {
    "ai", "api", "apis", "llm", "llms", "gpu", "gpus", "rag", "cuda", "mcp",
    "sdk", "ui", "ux", "cli", "aws", "gcp", "ml", "nlp", "io", "db", "hnsw",
    "ivf", "tpu", "npu", "sql", "etl", "rl", "xai", "3d", "ar", "vr", "iot",
}


def titleize(slug: str) -> str:
    parts = slug.replace("_", "-").split("-")
    return " ".join(p.upper() if p.lower() in ACRONYMS else p.capitalize() for p in parts)


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


def crawl(max_paths: int, max_topics: int, include_core: bool, out: str) -> None:
    index = get(INDEX)
    if index is None:
        sys.exit("could not fetch the main index")

    # Learning paths: ../<slug>/index.html
    paths = _unique_links(index, r"^\.\./([a-z0-9-]+)/index\.html$")
    sources: list[tuple[str, str]] = [(titleize(slug), urljoin(INDEX, href))
                                      for slug, href in paths]

    if include_core:
        # Core courses: <slug>/index.html, relative to /ai-school/. Treat the whole
        # set as one "AI School Core" category whose topics are those pages.
        core = _unique_links(index, r"^([a-z0-9-]+)/index\.html$")
        for slug, href in core:
            sources.append(("AI School Core", urljoin(INDEX, href), titleize(slug)))  # type: ignore

    if max_paths:
        sources = sources[:max_paths]
    print(f"learning paths to crawl: {len(sources)}")

    courses = []
    for entry in sources:
        # entry is (category, path_url) for paths, or (category, topic_url, topic_title) for core
        if len(entry) == 3:
            category, topic_url, topic_title = entry
            topics = [(topic_title, topic_url)]
        else:
            category, path_url = entry
            soup = get(path_url)
            if soup is None:
                print(f"  skip {category} (fetch failed)")
                continue
            topic_links = _unique_links(soup, r"^([a-z0-9-]+)/index\.html$")
            topics = [(titleize(slug), urljoin(path_url, href))
                      for slug, href in topic_links]
            if max_topics:
                topics = topics[:max_topics]
            print(f"  {category}: {len(topics)} topics")

        for title, url in topics:
            lesson_id = re.sub(r"[^a-z0-9]+", "-",
                              url.replace(BASE, "").strip("/").replace("/index.html", "")).strip("-")
            courses.append({
                "id": lesson_id,
                "title": title,
                "description": f"{title}, part of {category}.",
                "category": category,
                "lessons": [{
                    "id": lesson_id,
                    "title": title,
                    "webUrl": url,
                }],
            })

    with open(out, "w") as f:
        json.dump({"courses": courses}, f, indent=2)
    print(f"wrote {out}: {len(courses)} courses/lessons")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--max-paths", type=int, default=0,
                       help="limit number of learning paths (0 = all)")
    parser.add_argument("--max-topics", type=int, default=0,
                       help="limit topics per path (0 = all)")
    parser.add_argument("--include-core", action="store_true",
                       help="also include the ~379 AI School Core course pages")
    parser.add_argument("--out", default="lessons.json")
    args = parser.parse_args()
    crawl(args.max_paths, args.max_topics, args.include_core, args.out)
