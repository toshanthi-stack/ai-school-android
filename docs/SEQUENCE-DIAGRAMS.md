# AI School - Automotive Sequence Diagrams

Sequence diagrams for the three flows that matter in the in-vehicle build. They
use the real class and method names from `:app-automotive` and `:core:model`, so
the diagrams double as a map into the source. Rendered by GitHub natively
(Mermaid).

---

## 1. Window-open auto-pause (the anchor feature)

When any cabin window leaves the fully-closed position, the active lesson is
paused systemically through the media session, not by a local hack. Once every
cabin window is closed again, the lesson resumes automatically. The resume is
guarded: only a lesson the window paused resumes, never one the driver paused by
hand.

Source: `CabinWindowMonitor.kt`, `AISchoolMediaService.kt`.

```mermaid
sequenceDiagram
    autonumber
    actor Driver
    participant VHAL as Vehicle HAL<br/>(WINDOW_POS)
    participant CPM as CarPropertyManager
    participant Monitor as CabinWindowMonitor<br/>(propertyCallback)
    participant Service as AISchoolMediaService
    participant Session as MediaSessionCompat
    participant Player as MediaPlayer
    participant IVI as Media Center (IVI UI)

    Note over Service,Monitor: onCreate(): feature arms itself
    Service->>Monitor: start()
    Monitor->>CPM: subscribePropertyEvents(WINDOW_POS, callback)
    Note right of Monitor: needs CONTROL_CAR_WINDOWS<br/>(privileged), else disables cleanly

    Note over Player,IVI: a lesson is PLAYING

    Driver->>VHAL: opens a window (pos 0 to non-zero)
    VHAL-->>CPM: property change event
    CPM-->>Monitor: onChangeEvent(CarPropertyValue)
    Monitor->>Monitor: position not fully-closed, track areaId open
    Monitor->>Service: onCabinWindowOpened(areaId, position)
    Service->>Service: if mediaPlayer.isPlaying, set pausedByCabinWindow true
    Service->>Session: controller.transportControls.pause()
    Session-->>Service: sessionCallback.onPause()
    Service->>Player: pause()
    Service->>Session: setPlaybackState(STATE_PAUSED)
    Session-->>IVI: playback state is PAUSED
    Note over IVI: Now Playing, steering-wheel state,<br/>all surfaces update together

    Driver->>VHAL: closes the window (pos to 0)
    VHAL-->>CPM: property change event
    CPM-->>Monitor: onChangeEvent(CarPropertyValue)
    Monitor->>Monitor: last open window now closed, set empty
    Monitor->>Service: onAllWindowsClosed()
    Service->>Service: if pausedByCabinWindow, clear flag
    Service->>Session: controller.transportControls.play()
    Session-->>Service: sessionCallback.onPlay()
    Service->>Player: start()
    Service->>Session: setPlaybackState(STATE_PLAYING)
    Session-->>IVI: playback state is PLAYING
    Note over IVI: resumes only if the window paused it,<br/>never a manual pause
```

---

## 2. Lesson playback start

The automotive flavor draws no UI of its own. The car's Media Center binds to the
`MediaBrowserServiceCompat`, renders the browse tree, and drives playback through
the media session.

Source: `AISchoolMediaService.kt`, `BrowseTree.kt`.

```mermaid
sequenceDiagram
    autonumber
    actor Driver
    participant IVI as Media Center (IVI UI)
    participant Service as AISchoolMediaService<br/>(MediaBrowserServiceCompat)
    participant Tree as BrowseTree
    participant Session as MediaSessionCompat
    participant Player as MediaPlayer

    IVI->>Service: onGetRoot() / onLoadChildren(parentId)
    Service->>Tree: children(parentId)
    Tree-->>Service: MediaItems (pillars, courses, lessons)
    Service-->>IVI: browse results
    Driver->>IVI: taps a lesson
    IVI->>Session: transportControls.playFromMediaId(id)
    Session-->>Service: sessionCallback.onPlayFromMediaId(id)
    Service->>Service: playLesson(course, lesson)
    Service->>Player: setDataSource(audioUrl) + prepareAsync()
    Note right of Service: stream first,<br/>bundled narration fallback
    Player-->>Service: onPrepared()
    Service->>Service: requestAudioFocus()
    Service->>Player: start()
    Service->>Session: setPlaybackState(STATE_PLAYING)
    Session-->>IVI: Now Playing (transport controls)
```

---

## 3. Dual-payload content sanitization (safety in the data layer)

The same syllabus serves both surfaces. The moment content heads for the cabin,
the data layer strips every visual payload and drops anything with no audio, so
the browse tree the head unit receives is structurally incapable of presenting
distracting content. Safety is enforced by architecture, not by UI review.

Source: `core/model/AutomotiveSafety.kt`.

```mermaid
sequenceDiagram
    autonumber
    participant Service as AISchoolMediaService
    participant Repo as Catalog / Ktor (core:network)
    participant Safety as toAutomotiveSafeSyllabus()
    participant IVI as Media Center

    Service->>Repo: load syllabus (list of Course)
    Repo-->>Service: full catalog (visual + audio payloads)
    Service->>Safety: toAutomotiveSafeSyllabus() on the course list
    loop each lesson
        Safety->>Safety: if audioUrl blank, drop (null)
        Safety->>Safety: copy(visualContentUrl = null)
        Note right of Safety: audioSummary becomes the<br/>only permitted text surface
    end
    Safety-->>Service: audio-only syllabus
    Service-->>IVI: browse tree (audio + one-line summary only)
```

---

## How to exercise these on an emulator

See [`RUNNING-AUTOMOTIVE.md`](RUNNING-AUTOMOTIVE.md) and
[`SINGLE_SCREEN_DEMO.md`](SINGLE_SCREEN_DEMO.md) for setup. The window-pause flow
(diagram 1) is triggered with an injected VHAL event while a lesson plays:

```bash
# pause: open a window (any non-zero position)
adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 3
# resume: close the window again (auto-resumes the window-paused lesson)
adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 0
```

The debug automotive build is platform-signed (AOSP public test key), so
`CONTROL_CAR_WINDOWS` is granted and the feature runs live on the emulator.
