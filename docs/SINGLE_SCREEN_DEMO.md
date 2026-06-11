# Single-Screen AAOS Demo (cluster-display workaround)

## The problem
The `AISchool_AAOS` emulator (android-35-ext15 automotive Google APIs) renders a
second "instrument cluster" panel (`EMU_display_1`, 528x792, portrait) crammed
beside the main 1080x600 infotainment display.

## What does NOT fix it (exhaustively tested 2026-06-10)
- Removing `hw.display6` from `config.ini`: PROVEN inert. After removing all four
  `hw.display6` lines + `-wipe-data` cold boot, `hardware-qemu.ini` showed zero
  secondary displays (`hw.display1/2/3` all 0), yet `dumpsys SurfaceFlinger` still
  listed `port=1 EMU_display_1`. The `hw.display6` entry was a 400x600 ghost, a
  different size than the real 528x792 cluster.
- `MultiDisplay = off` in `~/.android/advancedFeatures.ini`: emulator log confirms
  the feature is disabled and only configures display 0, BUT the standalone window
  still shows the cluster panel (visually confirmed). Also breaks fast-boot
  (snapshot requires feature 65). Reverted.
- Device-profile swaps, fresh API-33 image, `-feature -MultiDisplay`,
  `hw.multi_display_window=yes`: all failed (see project memory).

## Root cause
The cluster display is baked into the AAOS Google-APIs system image at the
vendor/HWC level. It is NOT generated from any AVD config, so no `config.ini` /
`advancedFeatures.ini` / hardware-profile change can remove it. A standalone CLI
emulator window always renders every guest physical display.

## The working single-screen path: headless emulator + scrcpy (CONFIRMED 2026-06-10)
Run the emulator with NO window and mirror only display 0 with scrcpy. The
standalone emulator window is what crams both displays together; scrcpy mirrors
a single display by ID, so the cluster never appears. User visually confirmed.

Prereq (one-time): `brew install scrcpy` (installed; v4.0).

    # 1. boot emulator headless (no dual-pane window ever appears)
    ~/Library/Android/sdk/emulator/emulator -avd AISchool_AAOS -no-snapshot -no-window &

    # 2. once booted (sys.boot_completed=1, ~30s), launch the app on display 0
    adb shell am start -n com.lillytech.aischool.automotive/.vw.VwCatalogActivity

    # 3. mirror display 0 ONLY - clean, resizable, interactive window
    ADB=~/Library/Android/sdk/platform-tools/adb \
      scrcpy --display-id=0 --window-title="AI School - VW Infotainment"

The window-open -> audio-pause VHAL demo still works underneath:
    adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 3   # open -> PAUSE
    adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 0   # close -> PLAY

NOTE: a NON-working idea that was tried and failed - "Android Studio Running
Devices panel display selector" does NOT exist (the embedded panel has no
per-display dropdown). Use scrcpy.

(The app's VwCatalogActivity loads instantly, no spinner. The green "No maps
application installed" panel on the AAOS home is normal, not an error. If the
emulator refuses to boot with "Running multiple emulators with the same AVD",
kill stale procs: `adb emu kill; pkill -9 -f qemu-system; rm -f
~/.android/avd/AISchool_AAOS.avd/*.lock`.)
