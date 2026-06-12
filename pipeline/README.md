# AI School content pipeline (the "smart service")

Turns AI School web tutorials into the audio-first feed the apps consume. For
each lesson it fetches the page, asks Claude to classify it (conceptual vs
code-heavy) and rewrite it into a spoken, podcast-style script that drops literal
code/commands, then synthesizes narration and emits `syllabus.json` + audio.

This is the automated, production version of the adaptation that was done by hand
for the iOS proof-of-concept. See [`docs/CONTENT-PIPELINE.md`](../docs/CONTENT-PIPELINE.md)
for the architecture and feed schema.

## Files

| File | Role |
|---|---|
| `adapt_lesson.py` | Fetch a URL, classify + rewrite via Claude (structured output) |
| `tts.py` | Text-to-speech (prototype: macOS `say`; swap in a production TTS) |
| `build_feed.py` | Run every lesson in `lessons.json` -> `out/syllabus.json` + `out/audio/` |
| `lessons.json` | Input config: courses -> lessons with `webUrl` |

## Setup

```bash
cd pipeline
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
export ANTHROPIC_API_KEY=sk-ant-...      # your Anthropic API key
```

## Run

Adapt a single lesson (prints the classification + spoken script):

```bash
.venv/bin/python adapt_lesson.py https://www.lillytechsystems.com/ai-prompts/index.html
```

Build the whole feed from `lessons.json`:

```bash
.venv/bin/python build_feed.py
# writes out/syllabus.json and out/audio/*.m4a
```

Then host `out/syllabus.json` + the audio at lillytechsystems.com (the apps
prefer the live feed) or bundle them into the apps.

## Model and TTS

- **Model**: defaults to `claude-opus-4-8` (`MODEL` in `adapt_lesson.py`). For bulk
  runs over thousands of tutorials, switch to a cheaper model
  (`claude-sonnet-4-6` / `claude-haiku-4-5`) - both support the structured output
  used here. That's a cost decision for the operator.
- **TTS**: the prototype uses macOS `say` (the same voice as the demo audio).
  For production quality, replace `synthesize` in `tts.py` with a real TTS
  service; keep the signature and `build_feed.py` is unchanged.

## Scaling

`lessons.json` is hand-listed here. To cover the full catalog, generate it by
crawling the site's learning-path index pages (each lists ~50 topics), then run
`build_feed.py` on a schedule when new content is published so the feed stays
current without app releases.
