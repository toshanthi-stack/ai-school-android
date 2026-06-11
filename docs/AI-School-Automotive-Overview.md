# AI School on Android Automotive OS
### A concept demo: driver-safe in-car learning

AI School is a learning platform for AI and software topics
(lillytechsystems.com/ai-school). This project explores how that content can
live safely inside a vehicle: a dual-flavor Android app that is rich and
interactive on a phone, and strictly audio-only and distraction-safe in the car.

It is a working build, not slideware. The notes below describe the architecture,
the in-vehicle design decisions, and the feature that anchors the demo: the audio
auto-pauses the moment a window is opened, driven directly off the Vehicle HAL.

---

## At a glance

| | Mobile (`:app-mobile`) | Automotive (`:app-automotive`) |
|---|---|---|
| Surface | Phone / tablet | Android Automotive OS head unit |
| UI | Full Jetpack Compose app | None of its own; rendered by the car's Media Center |
| Content | Video, code, interactive sandboxes, audio | Audio streams plus short spoken summaries only |
| Headline feature | Live interactive lessons from the production site | Window-open auto-pause via the Vehicle HAL |

Both flavors are driven by one shared domain model, so the catalog stays
consistent across surfaces and the in-car rules are enforced in data, not UI.

---

## Architecture: one syllabus, two regulatory surfaces

A small multi-module monorepo:

- `:core:model` · domain entities (Course, Lesson), the catalog, and the
  content-safety rules
- `:core:network` · Ktor client with dual-payload handling (a visual payload for
  mobile, an audio-only payload for the car)
- `:core:demoaudio` · bundled narration so the experience works with no network
- `:app-mobile` · the rich Compose app
- `:app-automotive` · the media service, the cabin-signal integration, and a
  VW-styled design preview

The guiding principle: the same lesson renders as a rich interactive unit on a
phone, but the moment it heads for a vehicle the **data layer** strips every
visual payload. Lessons that are visually dense (code, raw JSON, architecture
diagrams) survive in the cabin only as an audio stream plus a one-line summary.
Safety is enforced by architecture, not by UI review.

---

## In-vehicle design decisions

**1. The app brings content, not chrome.**
The automotive flavor declares no activities for the driving experience. It is a
`MediaBrowserServiceCompat`, so the vehicle's own distraction-optimized Media
Center renders the browse tree and the Now Playing screen. There is no
app-drawn text entry, clipboard, or free-form UI in the cabin.

**2. Content is sanitized in the data layer.**
The audio-only catalog is produced by a single transform: drop visual payloads,
keep the audio stream and the spoken summary, and drop anything with no audio.
The browse tree the head unit receives is structurally incapable of presenting
distracting content.

**3. The cabin is an input: window-open auto-pause, window-closed auto-resume.**
A `CarPropertyManager` callback subscribes to `WINDOW_POS` across every cabin
window zone. When any window leaves the fully-closed position, playback is paused
through the media session's transport controls, so the IVI, the steering-wheel
state, and any connected surface all update together. Once every cabin window is
closed again, the lesson resumes automatically. It is a systemic pause/resume,
not a local workaround. The resume is guarded: only a lesson the *window* paused
resumes, never one the driver paused by hand. Details:
- Uses the current `subscribePropertyEvents` API with a fallback to the legacy
  callback for older Car API levels.
- Reads `WINDOW_POS` (`0` = closed; any non-zero value = cracked or open) per
  zone (driver, front passenger, rear-left, rear-right).
- `CONTROL_CAR_WINDOWS` is a privileged permission. On an OEM or platform-signed
  build it is granted and the feature is live; on a regular install the monitor
  detects the missing permission and disables itself cleanly, so playback and
  browsing are unaffected.
- The callback is unregistered and the Car connection released on teardown.

**4. Offline-resilient.**
Streaming is attempted first; if it is unreachable, bundled narration plays
seamlessly. The catalog falls back to a built-in copy. The cabin experience never
depends on connectivity.

---

## What the demo shows

The 26-second walkthrough (`automotive-demo.mp4`) and the screenshot set follow
this flow on the head unit:

1. **AI School in the Media Center** · selectable as a native media source.
2. **Browse by pillar** · Generative AI Skills, AI Infrastructure & Hardware,
   Advanced LLM Tuning, each opening to its courses and lessons.
3. **Now Playing** · a real media session with standard transport controls.
4. **Window opens, audio auto-pauses; window closes, audio resumes** · the
   Vehicle HAL event pauses the lesson instantly, and closing every window resumes
   it. This is the anchor moment.
5. **VW-styled design direction** · see below.

---

## VW-styled design direction

In production, the driver-facing browse and Now Playing UI are rendered and
themed by the OEM, typically through Runtime Resource Overlays against
`car-ui-lib`. An app inherits the OEM design language with no app changes.

To show how AI School fits that language, the build includes a Compose preview
styled after the VW MIB direction: a dark canvas, translucent rounded tiles, a
rounded humanist typeface, and per-pillar accent colors. The point is the
separation: the app supplies its brand, the OEM supplies the chrome, and on a VW
unit the result speaks VW's design language for free.

---

## Technical stack

Kotlin, AGP 9.2, Jetpack Compose, Ktor 3, `MediaBrowserServiceCompat` /
`MediaSessionCompat`, and the `android.car` library. `compileSdk` 36; the
automotive flavor targets the AAOS baseline (API 29+).

---

## Running it

For an engineer who wants to try it:

1. Open the project in a current Android Studio with the Android 36 SDK.
2. Create an **Automotive** emulator (Android 33+ Automotive image) and run the
   `:app-automotive` configuration. AI School appears in the Media Center.
3. To exercise the window pause/resume on an emulator, inject window-position
   events while a lesson plays:
   ```
   adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 3   # open driver window -> pauses
   adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 0   # close it -> resumes
   ```
   On a Play-delivered install the privileged permission is not granted, so the
   monitor disables itself; an OEM or platform-signed build runs it live.

The mobile flavor runs on any phone or tablet emulator (API 26+).

---

## Why this matters beyond one platform

Android Automotive OS is used here as a representative software-defined-vehicle
surface. The decisions on display are platform-agnostic: a content-safety policy
enforced in the data layer, integration with live vehicle signals, an offline
strategy, and a clean boundary between app brand and OEM chrome. Those patterns
carry directly onto a CARIAD or MIB environment, or any modern software-defined
vehicle stack.

---

## Materials in this package

- `automotive-demo.mp4` · captioned walkthrough
- `screenshots/` · full screenshot set (mobile and automotive)
- `vw-reference/` · VW-styled integration mockup and MIB design reference
- `brand/` · brand assets (logo, graph mark, launcher master)
- `SEQUENCE-DIAGRAMS.md` · the three core flows
