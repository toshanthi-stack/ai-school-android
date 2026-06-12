"""Adapt one AI School web tutorial into an audio-first lesson via Claude.

Fetches the page, strips chrome, and asks Claude to (1) classify the lesson as
conceptual vs code-heavy and (2) rewrite it into a spoken, podcast-style script
that drops literal code/commands (useless and unsafe as audio). Returns a
structured result the feed builder turns into a syllabus entry.
"""
from __future__ import annotations

import os
import re

import anthropic
import requests
from bs4 import BeautifulSoup
from pydantic import BaseModel

# Anthropic's most capable model is the default. For bulk runs over thousands of
# tutorials, set ADAPT_MODEL to a cheaper model (e.g. claude-haiku-4-5 or
# claude-sonnet-4-6) - both support the structured output used here. This is the
# operator's cost lever; the call shape is unchanged.
MODEL = os.environ.get("ADAPT_MODEL", "claude-opus-4-8")

MAX_INPUT_CHARS = 20_000


class LessonAdaptation(BaseModel):
    """Structured output Claude returns for each lesson."""

    content_type: str   # "conceptual" or "code"
    audio_summary: str  # one glanceable sentence
    spoken_script: str  # podcast-style narration, no literal code


SYSTEM = """You adapt AI School web tutorials into audio-first lessons for a \
hands-free learning app used in cars and on phones. For each tutorial:

1. Set content_type:
   - "code" if the lesson centers on code, commands, step-by-step setup, or
     copy-paste snippets.
   - "conceptual" otherwise (ideas, explanations, trade-offs, mental models).

2. Write spoken_script: a natural, podcast-style narration of the lesson,
   150-260 words, that a listener follows with no screen. Explain the ideas;
   for code, describe what the code DOES and why, but never read literal code,
   commands, file paths, URLs, or copy-paste text aloud (they are useless as
   audio and unsafe to follow while driving). For a code-heavy lesson, end by
   telling the listener the exact code is in the written lesson to open on
   their phone.

3. Write audio_summary: one short sentence summarizing the lesson.

No markdown, no headings, no bullet points: just spoken prose."""


def fetch_text(url: str) -> str:
    """Fetch a page and return its main text with nav/script/style stripped."""
    resp = requests.get(url, timeout=20,
                        headers={"User-Agent": "aischool-content-pipeline/1.0"})
    resp.raise_for_status()
    soup = BeautifulSoup(resp.text, "html.parser")
    for tag in soup(["script", "style", "nav", "header", "footer", "aside", "noscript"]):
        tag.decompose()
    text = re.sub(r"\n{3,}", "\n\n", soup.get_text(separator="\n")).strip()
    return text[:MAX_INPUT_CHARS]


def adapt(url: str, client: anthropic.Anthropic | None = None) -> LessonAdaptation:
    """Adapt a lesson URL into a classified, spoken-script LessonAdaptation."""
    client = client or anthropic.Anthropic()  # reads ANTHROPIC_API_KEY
    content = fetch_text(url)
    response = client.messages.parse(
        model=MODEL,
        max_tokens=2000,
        system=SYSTEM,
        messages=[{
            "role": "user",
            "content": f"Adapt this lesson page (source: {url}):\n\n{content}",
        }],
        output_format=LessonAdaptation,
    )
    return response.parsed_output


if __name__ == "__main__":
    import json
    import sys

    if len(sys.argv) != 2:
        print("usage: python adapt_lesson.py <lesson-url>")
        raise SystemExit(2)
    print(json.dumps(adapt(sys.argv[1]).model_dump(), indent=2))
