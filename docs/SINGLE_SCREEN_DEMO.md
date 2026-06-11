# Running the AAOS demo on a single screen

## Background

The automotive emulator (Android Automotive Google-APIs image) renders a second
"instrument cluster" panel (`EMU_display_1`, 528x792, portrait) alongside the
main 1080x600 infotainment display. The cluster is part of the system image at
the vendor/HWC level, not generated from the AVD config, so a standalone emulator
window always shows both displays and no `config.ini` or `advancedFeatures.ini`
change removes it.

## Recommended: headless emulator plus scrcpy

Run the emulator headless and mirror only display 0 with
[scrcpy](https://github.com/Genymobile/scrcpy). scrcpy mirrors a single display
by id, so the cluster never appears and you get a clean, resizable, interactive
window.

One-time prerequisite: `brew install scrcpy`.

```bash
# 1. boot the emulator headless (no dual-pane window)
~/Library/Android/sdk/emulator/emulator -avd <your_aaos_avd> -no-snapshot -no-window &

# 2. once booted (~30s), launch the VW preview on display 0
adb shell am start -n com.lillytech.aischool.automotive/.vw.VwCatalogActivity

# 3. mirror display 0 only
ADB=~/Library/Android/sdk/platform-tools/adb \
  scrcpy --display-id=0 --window-title="AI School - VW Infotainment"
```

The window-open pause / window-closed resume still works underneath:

```bash
adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 3   # open  -> pause
adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 0   # close -> resume
```

## Notes

- The VW preview activity loads instantly. The green "No maps application
  installed" panel on the AAOS home screen is normal, not an error.
- If the emulator refuses to boot with "Running multiple emulators with the same
  AVD", clear stale processes and locks:
  ```bash
  adb emu kill; pkill -9 -f qemu-system; rm -f ~/.android/avd/<your_aaos_avd>.avd/*.lock
  ```
