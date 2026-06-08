# Branding & Head-Unit Theming

This doc covers two distinct layers — what the **app** brands (we control) and
what the **OEM** themes (VW controls) — because the boundary between them is
itself a strong talking point for an automotive platform role.

## 1. App branding (we control)

### Brand system (from `lillytechsystems.com/ai-school/css/style.css`)

| Token | Value | Role |
|---|---|---|
| Primary | `#6C63FF` indigo/violet | Brand primary, active states, CTAs |
| Secondary | `#FF6584` coral-pink | Accent (the one coral graph node) |
| Accent | `#43E97B` green | Highlights |
| Background | `#13131A` near-black | App/icon background |
| Font | **Inter** (300–900) + **JetBrains Mono** | Wordmark + code |

> Earlier drafts branded off the coral *product icon* and used coral as the
> primary — corrected here: the true primary is indigo `#6C63FF` on dark
> `#13131A`, and the wordmark is **Inter**.

### The launcher icon (shipped)

Faithful to the website hero lockup: the knowledge-graph mark (white nodes +
one coral `#FF6584` node) over the lowercase **ai** in Inter Bold and
letter-spaced **school**, all on the brand-dark `#13131A` background.

- Adaptive icon: `mipmap-anydpi-v26/ic_launcher.xml` → `@color/ic_launcher_background`
  (`#13131A`) + foreground lockup + `<monochrome>` layer for themed icons.
- Foreground content sits at ~72% so it fills a **squircle** while staying
  inside the safe zone; legacy `ic_launcher.png` is a pre-masked squircle.
- Verified on a device (squircle shape): `docs/screenshots/mobile-5-app-icon-squircle.png`.
- Source + master: `docs/brand/` (official logo, isolated graph mark, final master).

| Asset | Where it shows |
|---|---|
| Adaptive launcher icon | All-apps grid, media source picker, Now Playing badge, home media card |
| Per-pillar artwork | Browse grid + Now Playing album art (see note below) |

Verified on the emulator: the coral logo renders correctly in the all-apps
launcher (`docs/screenshots/10`), the media source picker (`docs/screenshots/8`),
and as the Now Playing badge (`docs/screenshots/9`).

### Per-pillar album art — and an honest AAOS note

Album art is served to the system Media Center via **`ArtworkProvider`**, a
read-only `ContentProvider` exposing `content://…/category/<pillar>`. This is
the correct AAOS pattern: the Media Center is a *separate system process* and
its image loader can read `content://` but **not** a cross-package
`android.resource://` URI.

Verified working end-to-end: the provider serves valid image bytes and the
Media Center's process (`uid 1010160`) calls `openFile`/`openAssetFile` and
reads them (confirmed in logcat). **However**, the *stock AOSP reference media
app on the emulator* paints browse/Now-Playing art behind a heavy scrim, so the
tiles read as a flat tint there regardless of the image. Production OEM media
UIs (including VW's) render content-provider album art normally. This is a known
emulator-app limitation, not a defect in the provider — a good detail to be able
to explain.

> If you'd rather the **live emulator demo** show the Media Center's own vivid
> auto-generated tiles instead of the scrimmed brand art, remove the two
> `.setIconUri(...)` calls in `BrowseTree.kt` and the art URIs in
> `publishMetadata(...)`. The launcher-icon branding is unaffected either way.

## 2. Head-unit theming (the OEM / VW controls)

This is the key architectural point: **an AAOS app does not theme the head
unit.** The vehicle's entire chrome — colors, fonts, toolbar placement, system
bars, rounded-corner radii — is owned by the OEM and applied through
**Runtime Resource Overlays (RROs)** against `car-ui-lib`. VW ships its own RRO
set so every app (yours included) automatically inherits the VW design language
with zero app changes.

Demonstrated live on the emulator by enabling the bundled sample OEM theme:

```bash
adb root
# Re-skin the whole head unit with a sample OEM theme:
adb shell cmd overlay enable android.googlecarui.theme.orange.rro
adb shell cmd overlay enable com.android.systemui.googlecarui.theme.orange.rro
# Revert to the default theme:
adb shell cmd overlay disable android.googlecarui.theme.orange.rro
adb shell cmd overlay disable com.android.systemui.googlecarui.theme.orange.rro
```

`docs/screenshots/7-home-card-oem-orange-theme.png` shows the system chrome
re-themed (nav bar, app-grid button) while the AI School app and its coral logo
sit inside it unchanged — exactly how it works on a real VW head unit.

### The talking point

> My app brings its **brand** — the coral AI School mark on the launcher icon
> and its album art — but it deliberately brings **no chrome**. The head unit's
> look is the OEM's to own through RROs against car-ui-lib, so the moment this
> runs on a VW unit it speaks VW's design language for free. Brand where it's
> mine to brand; inherit where it's the platform's. That separation is the
> whole point of the AAOS app/OEM contract.

## Regenerating brand assets

Source art and the generation approach live in `docs/brand/`. Launcher icons
are adaptive (`mipmap-anydpi-v26/ic_launcher.xml` → coral background +
white-graph foreground) across all density buckets in both app modules.
