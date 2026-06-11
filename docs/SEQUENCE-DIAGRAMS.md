# AI School - Automotive Sequence Diagrams

Three simple flows for the in-vehicle build, in the order they happen: content is
made safe, a lesson plays, then the cabin reacts. Rendered by GitHub natively
(Mermaid).

---

## 1. Content made safe for the cabin

Before content reaches the car, the data layer strips every visual payload and
keeps audio plus a one-line summary.

Source: `core/model/AutomotiveSafety.kt`.

```mermaid
sequenceDiagram
    participant App as AI School app
    participant Data as Content layer
    participant IVI as Media Center

    App->>Data: load syllabus
    Data->>Data: strip visuals, keep audio + summary
    Data->>App: audio-only catalog
    App->>IVI: safe browse tree
```

---

## 2. Lesson playback

The car's Media Center drives playback; the app supplies the audio.

Source: `AISchoolMediaService.kt`, `BrowseTree.kt`.

```mermaid
sequenceDiagram
    actor Driver
    participant IVI as Media Center
    participant App as AI School app

    Driver->>IVI: picks a lesson
    IVI->>App: play request
    App->>App: load audio (stream, else bundled)
    App->>IVI: now playing
```

---

## 3. Window-open pause, window-closed resume (the anchor feature)

A lesson pauses the instant a window opens, and resumes once every window is
closed again.

Source: `CabinWindowMonitor.kt`, `AISchoolMediaService.kt`.

```mermaid
sequenceDiagram
    actor Driver
    participant Car as Vehicle HAL
    participant App as AI School app
    participant IVI as Media Center

    Note over App,IVI: a lesson is playing
    Driver->>Car: opens a window
    Car->>App: window-position changed
    App->>IVI: pause
    Driver->>Car: closes the window
    Car->>App: window-position changed
    App->>IVI: resume
```

---

## Try the anchor feature on an emulator

While a lesson plays:

```bash
adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 3   # open window -> pause
adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 0   # close window -> resume
```
