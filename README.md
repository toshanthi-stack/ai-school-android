<div align="center">

# AI School · Android

**A dual-flavor Android app for the AI School learning platform: rich and interactive on a phone, strictly audio-only and distraction-safe in the car.**

[Overview](docs/AI-School-Automotive-Overview.md) · [Sequence diagrams](docs/SEQUENCE-DIAGRAMS.md) · [Demo video](docs/automotive-demo.mp4) · [Screenshots](#screenshots) · [Build](#build)

</div>

---

AI School is a learning platform for AI and software topics
(lillytechsystems.com/ai-school). This project explores how that content can live
safely inside a vehicle. The same domain model renders as a full interactive app
on mobile, but the moment a lesson heads for a vehicle the **data layer** strips
every visual payload, so the cabin sees audio plus a short spoken summary only.
Safety is enforced by architecture, not by UI review.

The anchor feature: **the lesson auto-pauses the instant a window is opened**,
driven directly off the Vehicle HAL.

## Highlights

- **Driver-distraction safe by design** · the automotive flavor is a
  `MediaBrowserService` with no UI of its own; the car's Media Center renders
  everything, and visually dense lessons are sanitized to audio in the data layer.
- **Window-open auto-pause / window-closed auto-resume (VHAL)** · a
  `CarPropertyManager` callback on `WINDOW_POS` pauses playback through the media
  session the moment any window leaves the closed position, and resumes once every
  window is closed again (only if the window paused it). Systemic, not a local hack.
- **Offline-resilient** · streaming first, bundled-narration fallback, seeded
  catalog. The cabin never depends on connectivity.
- **OEM-themable + a VW-styled preview** · the production in-car UI is OEM-themed
  via car-ui-lib; a Compose preview shows AI School in the VW MIB design language.
- **Live interactive lessons on mobile** · code, sandboxes, and the real
  published site rendered in-app.

## Screenshots

### Android Automotive OS
![Automotive overview](docs/screenshots/automotive-vw-1-home-genai.png)

| Source picker | Browse | Now Playing | Window-open auto-pause |
|---|---|---|---|
| ![](docs/screenshots/automotive-2-source-picker-new-icon.png) | ![](docs/screenshots/automotive-3-browse-pillars.png) | ![](docs/screenshots/automotive-5-now-playing.png) | ![](docs/screenshots/automotive-6-paused-after-window.png) |

### Mobile
| Catalog | Course | Audio lesson | Interactive lesson |
|---|---|---|---|
| ![](docs/screenshots/mobile-1-courses.png) | ![](docs/screenshots/mobile-2-course-detail.png) | ![](docs/screenshots/mobile-3-lesson-audio.png) | ![](docs/screenshots/mobile-4-lesson-interactive.png) |

A 26-second captioned walkthrough is at
[`docs/automotive-demo.mp4`](docs/automotive-demo.mp4).

## Architecture

A multi-module Gradle (Kotlin DSL) monorepo:

| Module | Role |
|---|---|
| `:core:model` | Domain entities, catalog, and the content-safety rules |
| `:core:network` | Ktor client with dual-payload handling (visual vs audio-only) |
| `:core:demoaudio` | Bundled narration + branded artwork for offline use |
| `:app-mobile` | Jetpack Compose app (catalog, audio + interactive lessons) |
| `:app-automotive` | Media browser service, VHAL cabin monitor, VW-styled preview |

The full design write-up is in
[docs/AI-School-Automotive-Overview.md](docs/AI-School-Automotive-Overview.md);
branding and theming notes are in
[docs/BRANDING_AND_THEMING.md](docs/BRANDING_AND_THEMING.md).

## Build

Requires a current Android Studio with the Android 36 SDK and JDK 17+.

```bash
# Mobile (phone/tablet emulator, API 26+)
./gradlew :app-mobile:assembleDebug

# Automotive (Android Automotive emulator, API 29+)
./gradlew :app-automotive:assembleDebug
```

Pre-built debug APKs are in [`release/`](release/). Run `:app-automotive` on an
Automotive emulator; AI School then appears in the Media Center. To exercise the
window-pause on an emulator while a lesson plays:

```bash
adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 4   # open driver window
adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 0   # close it
```

`CONTROL_CAR_WINDOWS` is privileged: on an OEM or platform-signed build it is
granted and the feature is live; otherwise the monitor disables itself cleanly.
The automotive debug build is signed with the **public AOSP platform test key**
(`app-automotive/keystore/`) so the feature runs on `test-keys` emulator images.

## Tech stack

Kotlin · AGP 9.2 · Jetpack Compose · Ktor 3 · `MediaBrowserServiceCompat` /
`MediaSessionCompat` · `android.car` · compileSdk 36.

Fonts: Inter and Nunito (SIL Open Font License). The AI School knowledge-graph
mark and brand assets belong to Lilly Tech Systems.

## Note on platform fit

Android Automotive OS is used here as a representative software-defined-vehicle
surface. The decisions on display are platform-agnostic: a content-safety policy
in the data layer, live vehicle-signal integration, an offline strategy, and a
clean boundary between app brand and OEM chrome. Those patterns carry onto a
CARIAD or MIB environment and onto a Rivian-derived stack.

## License

See [LICENSE](LICENSE). Demo / evaluation use; brand assets are property of
Lilly Tech Systems.
