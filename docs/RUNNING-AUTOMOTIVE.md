# Running the AI School Automotive app in Android Studio

A step-by-step, click-by-click guide to opening the project, creating an
Android Automotive emulator, running the app, and demoing the window-open
auto-pause feature. No command line needed except for two small steps, which you
can run from the Terminal tab inside Android Studio.

- App id: `com.lillytech.aischool.automotive`
- It is a media app: the car's **Media Center** displays it (it has no normal
  home-screen launcher icon of its own).

---

## Step 1 · Open the project

1. Launch **Android Studio** (Ladybug 2024.2 or newer).
2. On the Welcome screen, click **Open** (or **File ▸ Open** if a project is
   already open).
3. Navigate to the project folder `ai-school-android` and click **Open**.
4. If asked "Trust the project?", click **Trust Project**.
5. Wait for the first **Gradle sync** to finish. You will see a progress bar at
   the bottom; it can take a few minutes the first time while it downloads
   dependencies. When it says "Gradle sync finished", you are ready.

> If a yellow banner appears at the top offering to install missing SDK
> components or a newer Android Gradle Plugin, click the suggested link and let
> it install, then let it sync again.

### Set the Gradle JDK

This project needs **JDK 17 or newer**. If your machine's default Java is older,
set the IDE's Gradle JDK explicitly:

1. Open **Settings** (macOS: `Cmd + ,`).
2. Go to **Build, Execution, Deployment ▸ Build Tools ▸ Gradle**.
3. In the **Gradle JDK** dropdown, choose **`jbr-21` (Embedded JDK)** (bundled
   with Android Studio and always available). If it is not listed, choose
   **Add JDK...** and pick a JDK 17 or 21 install.
4. Click **OK**, then click the **Sync Project with Gradle Files** button (the
   elephant icon), or **File ▸ Sync Project with Gradle Files**.

### Versions

The project tracks the current toolchain: **AGP 9.2.x · Gradle 9.4.x · AGP
built-in Kotlin · compileSdk 36**. Use a matching Android Studio (the 2026.1 /
AGP 9 generation). If you open it in an older Android Studio that predates AGP 9,
sync fails with `Task 'prepareKotlinBuildScriptModel' not found in project
':app-automotive'`; update Android Studio to the AGP 9 generation rather than
downgrading the project.

---

## Step 2 · Install the Android SDK components

You need an SDK platform and an **Automotive** system image.

1. Open **Settings** (macOS: **Android Studio ▸ Settings**; Windows/Linux:
   **File ▸ Settings**).
2. Go to **Languages & Frameworks ▸ Android SDK**.
3. On the **SDK Platforms** tab:
   - Tick **Android 15.0 ("VanillaIceCream")** (API 35) or **Android 16** (API 36).
   - At the bottom right, tick **Show Package Details**.
   - Expand the API level you ticked and check an
     **Automotive with Google APIs** system image. On an Intel Mac choose the
     **x86_64** variant; on an Apple Silicon Mac choose the **arm64-v8a**
     variant.
4. Switch to the **SDK Tools** tab and make sure these are checked:
   - **Android SDK Build-Tools**
   - **Android Emulator**
   - **Android SDK Platform-Tools**
5. Click **Apply**, accept the licenses, and let it download. Click **OK** when
   done.

---

## Step 3 · Create the Automotive emulator

1. Open the **Device Manager**: click the device icon in the right toolbar, or
   **Tools ▸ Device Manager**.
2. Click **＋ ▸ Create Virtual Device**.
3. In the category list on the left, select **Automotive**.
4. Pick a hardware profile, for example **Automotive (1080p landscape)**, then
   click **Next**.
5. On the **System Image** screen, select the **Automotive with Google APIs**
   image you installed in Step 2 (download it here if it shows a download icon),
   then click **Next**.
6. Give it a name, for example **AISchool_AAOS**, and click **Finish**.

The new virtual device now appears in the Device Manager list.

---

## Step 4 · Run the app

1. In the toolbar at the top, open the **run configuration** dropdown (it sits to
   the left of the green Run arrow) and select **app-automotive**.
2. In the **device** dropdown next to it, select your **AISchool_AAOS** emulator.
3. Click the green **Run** arrow (or press **Ctrl+R** on macOS, **Shift+F10** on
   Windows/Linux).

What happens:
- The emulator boots in a window (first cold boot takes 2 to 4 minutes; later
  boots are faster).
- Gradle builds the app and installs it.

> You may see a dialog that says something like "Nothing to run" or "Default
> Activity not found". That is expected, because this is a media service with no
> launcher activity. Choose **Do not launch Activity** (or just dismiss it). The
> app is still installed; you will open it from the car's Media Center in the
> next step.

> Optional, to confirm the install visually: open the run configuration dropdown
> ▸ **Edit Configurations**, set **Launch** to **Specified Activity**, and enter
> `com.lillytech.aischool.automotive.vw.VwCatalogActivity`. Running then opens
> the VW-styled preview screen directly.

---

## Step 5 · Open AI School on the head unit

On the emulator screen (this is the car's display):

1. Tap the **app grid** icon in the bottom bar, then open **Media Center** (or
   tap a media tile on the home screen).
2. In Media Center, tap the **source picker** (the grid icon at the top right).
3. Choose **AI School** (the dark "ai school" knowledge-graph icon).
4. You now see three pillar tabs across the top:
   - Generative AI Skills
   - AI Infrastructure & Hardware
   - Advanced LLM Tuning
5. Tap a course to open its lessons, then tap a lesson to **play** it. The
   **Now Playing** screen appears with play, pause, and skip controls.

> Audio works with no network: lessons stream first and fall back to bundled
> narration. Visually heavy lessons are reduced to audio plus a one-line summary
> before they ever reach the car.

---

## Step 6 · Demo the window-open auto-pause / window-closed auto-resume

This is the headline feature: the lesson pauses the instant a window opens, and
resumes once every window is closed again.

You will send two small commands. The easiest place to run them is the **Terminal
tab inside Android Studio**:

1. Open the Terminal: **View ▸ Tool Windows ▸ Terminal** (or click **Terminal**
   at the bottom of the window).
2. Make sure a lesson is **playing** on the emulator.
3. Paste this and press Enter to open the driver's window:

   ```bash
   adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 3
   ```

   The lesson pauses immediately on the Now Playing screen.
4. Close the window again and the lesson resumes (it only resumes a lesson the
   window paused, never one you paused by hand):

   ```bash
   adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 0
   ```

Window zone IDs you can use in place of `0x10`:

| Zone | Area ID |
|---|---|
| Driver (front-left) | `0x10` |
| Front passenger | `0x40` |
| Rear-left | `0x100` |
| Rear-right | `0x400` |

Any window leaving position `0` triggers the pause.

> Why this needs no extra setup: reading window position is a privileged
> permission. The debug build is signed with the public AOSP platform test key
> that matches the emulator image, so the permission is granted automatically and
> the feature runs live. On a normal install the app detects the missing
> permission and simply disables this monitor; everything else still works.

Optional, to prove it fired, in the same Terminal:

```bash
adb logcat -d -s CabinWindowMonitor | tail -2
adb shell dumpsys media_session | grep -A12 "AISchoolMediaSession com" | grep state=
# shows: state=PlaybackState {state=PAUSED(2), ...}
```

---

## Step 7 · Show the VW-styled design preview

In the Android Studio **Terminal**:

```bash
adb shell am start -n com.lillytech.aischool.automotive/.vw.VwCatalogActivity
```

A dark, VW-styled catalog screen appears: translucent rounded tiles, the Nunito
rounded typeface, and a per-pillar accent color. Tap the pill tabs to switch
pillars, and tap a course to open the VW-styled Now Playing screen (the back
arrow returns).

This screen demonstrates how AI School fits the VW MIB design language. In
production, the OEM themes the real in-car UI through car-ui-lib overlays.

---

## Step 8 · Stop and reset

- To stop the app: click the red **Stop** square in the toolbar.
- To shut the emulator: close its window, or in the Terminal run `adb emu kill`.
- To reinstall after code changes: just click **Run** again.

---

## Troubleshooting

| Symptom | What to do |
|---|---|
| Gradle sync fails on first open | Accept the prompt to update the Android Gradle Plugin / install missing SDK packages, then **File ▸ Sync Project with Gradle Files**. |
| `Task 'prepareKotlinBuildScriptModel' not found` on sync | Your Android Studio predates the AGP 9 generation. Update Android Studio to the current version (2026.1 / AGP 9 generation), which matches the project's AGP 9.2 / Gradle 9.4 toolchain. |
| No Automotive option in Device Manager | You have not installed an Automotive system image. Go back to Step 2 and tick one under **Show Package Details**. |
| "Default Activity not found" when running | Expected for a media service. Choose **Do not launch Activity**; the app still installs. Open it via Media Center (Step 5). |
| AI School is not in the source picker | Reopen Media Center, or in the Terminal: `adb shell am force-stop com.android.car.media`, then open Media Center again. Confirm install: `adb shell pm list packages \| grep lillytech`. |
| Window-pause does nothing | In the Terminal: `adb shell dumpsys package com.lillytech.aischool.automotive \| grep CONTROL_CAR_WINDOWS` should print `granted=true`. If not, you are not on a `test-keys` Automotive image; recreate the AVD from an **Automotive with Google APIs** image. |
| `cmd car_service` not recognized | You are on a phone emulator, not an Automotive one. Create the AVD from an Automotive system image (Step 3). |
| Playback stuck on "buffering" | The remote audio URLs are placeholders; on a slow network the fallback to bundled narration can lag a few seconds. Wait, or tap another lesson. |
| Emulator very slow to boot | Normal for a cold Automotive boot (2 to 4 minutes). Start it before you need it. |

---

## Command-line alternative (optional)

If you prefer the terminal end to end:

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# one-time
sdkmanager "platforms;android-36" "emulator" "platform-tools" \
           "system-images;android-35-ext15;android-automotive;x86_64"
avdmanager create avd -n AISchool_AAOS \
  -k "system-images;android-35-ext15;android-automotive;x86_64" \
  -d automotive_1080p_landscape

# each run
emulator -avd AISchool_AAOS &
adb wait-for-device && until [ "$(adb shell getprop sys.boot_completed)" = "1" ]; do sleep 3; done
./gradlew :app-automotive:assembleDebug
adb install -r app-automotive/build/outputs/apk/debug/app-automotive-debug.apk
# then: open Media Center, play a lesson, and:
adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 3   # open window -> pause
adb shell cmd car_service inject-vhal-event WINDOW_POS 0x10 0   # close window -> resume
adb shell am start -n com.lillytech.aischool.automotive/.vw.VwCatalogActivity
```
