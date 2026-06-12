# AI School Content Pipeline (the "smart service")

The AI School website publishes thousands of web tutorials, many heavy with code,
steps, and copy-paste. That content is great to read but useless to listen to. The
apps are audio-first (the car is audio-only by necessity; mobile is audio-first
with reading for code). So the content has to be **adapted per surface** by an
AI pipeline before the apps consume it.

## The pipeline

Runs per lesson, automatically as new content is published:

```
Source web tutorial (text, code, steps, visuals)
        │
        ▼
 1. Classify        conceptual  |  code-heavy
 2. LLM rewrite     -> a spoken script: explains the ideas, describes what code
                       DOES, drops literal code / commands / copy-paste
 3. Text-to-speech  -> audio file (real duration)
 4. Emit metadata   -> title, summary, durationSeconds, contentType, webUrl
        │
        ▼
  syllabus.json (feed) + audio files
        │
        ├─► Automotive app : audio only (code-heavy collapses to a spoken summary)
        └─► Mobile app     : audio first, plus a "read the full lesson" web view
                             (the real page) for code-heavy lessons
```

This is the same "sanitize in the data layer" idea the apps already use, made
AI-powered and automatic.

## Feed contract (`syllabus.json`)

The apps consume a JSON array of courses. Each lesson:

| Field | Type | Meaning |
|---|---|---|
| `id` | string | Stable lesson id (also maps to the audio file name) |
| `title` | string | Lesson title |
| `durationSeconds` | int | **Real** narration length |
| `audioUrl` | string | URL of the adapted narration audio |
| `visualContentUrl` | string? | The real web lesson, for the "read the full lesson" view |
| `contentType` | string | `conceptual` (audio is the lesson) or `code` (audio is an overview; read for the code) |
| `isAutomotiveSafe` | bool | Safe to surface in the car (always true once it has audio) |
| `audioSummary` | string | Short glanceable summary |

Courses carry `id`, `title`, `description`, `category`, and `lessons[]`; the apps
group courses by `category`.

## Per-surface behavior

- **Automotive**: audio only. Code-heavy lessons play the spoken overview; no code
  is ever shown (driver-distraction safety).
- **Mobile**: audio first. Conceptual lessons show a subtle "Read the full lesson"
  link; code-heavy lessons show a prominent "View the full lesson & code" button
  that opens the real web page (chrome stripped) so the code is readable and
  copyable.

## Status

- **Proof-of-concept (done)**: three real lessons adapted end to end (a conceptual
  one and a code-heavy one), real narration generated, a real `syllabus.json`
  bundled in the iOS app, and the app loads it. See `ios/docs/screenshots/`.
- **Pipeline implementation (done)**: a runnable pipeline in
  [`pipeline/`](../pipeline/) automates steps 1-4: `adapt_lesson.py` fetches a
  lesson URL and uses the Claude API (structured output) to classify it and
  rewrite it into a spoken script; `tts.py` synthesizes the narration;
  `build_feed.py` runs every lesson in `lessons.json` and emits
  `out/syllabus.json` + audio. See `pipeline/README.md`.
- **Production (Lilly Tech infra)**:
  1. Provide an `ANTHROPIC_API_KEY` and (optionally) swap the prototype macOS
     `say` TTS for a production TTS in `tts.py`.
  2. Host the generated `syllabus.json` + audio at lillytechsystems.com (the app
     already prefers the live feed and falls back to the bundled one).
  3. Generate `lessons.json` by crawling the learning-path index pages, and run
     `build_feed.py` on a schedule when new content is published, so the catalog
     stays current without app releases.
