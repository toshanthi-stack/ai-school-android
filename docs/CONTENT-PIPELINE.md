# AI School Content Pipeline (the "smart service")

The AI School website publishes thousands of web tutorials, many heavy with code,
steps, and copy-paste. That content is great to read but useless to listen to.
The car is audio-only by necessity, so content has to be **adapted** by an AI
pipeline before the app consumes it.

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
        └─► Automotive app : audio only (code-heavy collapses to a spoken summary)
```

This is the same "sanitize in the data layer" idea the app already uses, made
AI-powered and automatic.

## Feed contract (`syllabus.json`)

The app consumes a JSON array of courses. Each lesson:

| Field | Type | Meaning |
|---|---|---|
| `id` | string | Stable lesson id (also maps to the audio file name) |
| `title` | string | Lesson title |
| `durationSeconds` | int | **Real** narration length |
| `audioUrl` | string | URL of the adapted narration audio |
| `visualContentUrl` | string? | The real web lesson (for reference) |
| `contentType` | string | `conceptual` (audio is the lesson) or `code` (audio is an overview) |
| `isAutomotiveSafe` | bool | Safe to surface in the car (always true once it has audio) |
| `audioSummary` | string | Short glanceable summary |

Courses carry `id`, `title`, `description`, `category`, and `lessons[]`; the app
groups courses by `category`.

## Per-surface behavior

- **Automotive**: audio only. Code-heavy lessons play the spoken overview; no code
  is ever shown (driver-distraction safety).

## Status

The pipeline runs end to end and the feed is **live, hosted, and streamed** by
the app. Current catalog: **8 tracks / ~240 lessons**, ~4-minute narrations.

- **Hosting**: the pipeline publishes `syllabus.json` + audio to a hosted feed
  served by **GitHub Pages** (the `ai-school-feed` repo). The app prefers the live
  feed and falls back to a bundled seed catalog so the browse renders offline;
  lesson audio is streamed.
- **Adding content (no app release)**: regenerate and publish via the pipeline;
  the app picks up new tracks on next launch.
