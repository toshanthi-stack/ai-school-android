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
| `crawl_catalog.py` | Crawl the live site into a `lessons.json` (the full catalog) |
| `lessons.json` | Input config: courses -> lessons with `webUrl` (a small curated example; regenerate with the crawler) |

## Setup

```bash
cd pipeline
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
export ANTHROPIC_API_KEY=sk-ant-...      # your Anthropic API key
```

## Make (chained crawl + build)

A `Makefile` wraps the steps below; it uses the venv and creates it on first use.

```bash
make install                 # create .venv and install dependencies
make check                   # show selected TTS backend and which keys are set
make crawl MAX_PATHS=5 MAX_TOPICS=8   # crawl the site -> lessons.json
make build AUDIO_URL_BASE=https://www.lillytechsystems.com/ai-school/audio
make feed                    # crawl then build (the full chain)
make clean                   # remove out/
```

`make crawl` needs no API key; `make build` / `make feed` require `ANTHROPIC_API_KEY`
(and pick up `ELEVENLABS_API_KEY` / `OPENAI_API_KEY` for production TTS). Run `make`
with no target for the full list.

## Generate the catalog (crawler)

`lessons.json` ships as a small curated example. To build it from the live site,
run the crawler (no API key needed):

```bash
# everything: ~33 learning paths x ~50-60 topics each (~2,000+ lessons)
.venv/bin/python crawl_catalog.py

# a manageable subset to start
.venv/bin/python crawl_catalog.py --max-paths 5 --max-topics 8

# also include the ~379 "AI School Core" course pages
.venv/bin/python crawl_catalog.py --include-core
```

It maps the site to `lessons.json`: category = learning path, course = topic,
lesson = the topic page (the pipeline summarizes each into one spoken lesson).
Mind the scale before a full `build_feed.py` run: every lesson is one Claude call
plus one TTS synthesis.

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
- **TTS**: `tts.py` ships with three backends, chosen by `TTS_BACKEND` or
  auto-detected from whichever key is present:

  | Backend | Set | Needs | Output |
  |---|---|---|---|
  | `elevenlabs` | production narration | `ELEVENLABS_API_KEY` | mp3 |
  | `openai` | production | `OPENAI_API_KEY` | mp3 |
  | `say` | local prototype (default) | nothing | m4a |

  ```bash
  export ELEVENLABS_API_KEY=...        # auto-selects elevenlabs
  # or: export OPENAI_API_KEY=...      # auto-selects openai
  # or: export TTS_BACKEND=say         # force the local prototype
  ```

  Voice/model overrides: `ELEVENLABS_VOICE_ID`, `ELEVENLABS_MODEL`,
  `OPENAI_TTS_VOICE`, `OPENAI_TTS_MODEL`. Both mp3 and m4a play natively on iOS
  and Android, so the feed can reference either. Pass
  `build_feed.py --audio-url-base https://www.lillytechsystems.com/ai-school/audio`
  to set each lesson's `audioUrl` to where you host the files.

## Scaling

`lessons.json` is hand-listed here. To cover the full catalog, generate it by
crawling the site's learning-path index pages (each lists ~50 topics), then run
`build_feed.py` on a schedule when new content is published so the feed stays
current without app releases.

## Scheduled builds (CI)

`.github/workflows/refresh-feed.yml` runs `make feed` weekly (and on demand) and
uploads the result as a `feed` artifact you can download and host. To enable it:

1. Add repo secrets (Settings > Secrets and variables > Actions): `ANTHROPIC_API_KEY`,
   plus `ELEVENLABS_API_KEY` or `OPENAI_API_KEY` (Linux runners have no `say` fallback).
2. Optional: set repo variables `FEED_MAX_PATHS` / `FEED_MAX_TOPICS` to control scope
   (every lesson is one Claude call plus one TTS call, so scope is a cost decision).
3. Optional: set variable `PUBLISH_PAGES=true` and enable Pages (Settings > Pages >
   Source = GitHub Actions) to publish `syllabus.json` + audio to a public Pages URL;
   audio URLs are pointed there automatically. Otherwise host the artifact yourself.

Until the secrets are set the workflow preflights and exits with a clear message, so
it is inert rather than noisy.
