# AI School - Google Play setup & submission (step by step)

Everything to publish the **mobile** app, with exact answers, in the order your
Play Console dashboard lists them. The Android Automotive flavor is a separate
gift to VW, not published here. Publisher: **Lilly Tech Systems LLC** (Org account).

---

## 0. Files you'll upload (already built, in this repo)
- **App bundle (AAB):** `android/app-mobile/build/outputs/bundle/release/app-mobile-release.aab`
- **App icon (512x512):** `android/playstore/icon-512.png`
- **Feature graphic (1024x500):** `android/playstore/feature-graphic.png`
- **Phone screenshots:** `android/playstore/screenshots/01-home.png … 04-lesson.png`
- **Privacy policy URL (live):** https://toshanthi-stack.github.io/ai-school-feed/privacy.html

## 0b. Back up the upload key (do once - already done)
Keystore `android/keystore/upload-keystore.jks` + password in
`android/keystore.properties` (gitignored). You saved the .jks to OneDrive and
the password to an iCloud note. Without this (and Play App Signing) you can't
update the app later.

---

# "Set up your app" - do these top to bottom (matches your dashboard)

## 1. Set privacy policy
Paste this URL and Save:
`https://toshanthi-stack.github.io/ai-school-feed/privacy.html`

## 2. Sign in details
The app has **no login**. Choose **"All functionality is available without
special access"** (i.e. no sign-in required) -> Save. No test credentials needed.

## 3. Ads
"Does your app contain ads?" -> **No** -> Save.

## 4. Content rating
1. Enter your **contact email**.
2. Category: choose **Reference / Education** (a "Utility, Productivity,
   Communication, or Other" / Reference style category - not Game).
3. Questionnaire: answer **No** to everything - violence, sexual content,
   profanity, controlled substances, gambling, user-to-user communication,
   user-generated content, sharing location, digital purchases.
4. Submit -> you get an **Everyone** rating -> Save.

## 5. Target audience
1. Target age groups: tick **13-15, 16-17, and 18 and over**. Do **not** tick
   any under-13 group.
2. "Is your app appealing to children?" / unintended-appeal -> **No**.
3. Save. (This keeps you out of the Families/children policy program.)

## 6. Data safety
1. "Does your app collect or share any of the required user data types?" -> **No.**
   (v1 has no login, no analytics SDK, no ads SDK; it only fetches/streams content.)
2. "Is all of the user data encrypted in transit?" -> **Yes** (all HTTPS).
3. "Do you provide a way for users to request that their data is deleted?" ->
   not applicable (no data collected).
4. Review -> Submit.
   - Note: the app opens lillytechsystems.com lessons in an in-app browser (your
     own site). For v1 with no in-app SDKs, **No data collected** is the honest
     answer. If that site later adds analytics/ads, revisit this.

## 7. Government apps
"Is your app a government app?" -> **No** -> Save.

## 8. Financial features
"Does your app provide financial features?" -> **No, my app doesn't provide any
financial features** -> Save.

## 9. Health
"Does your app have health features?" (Health Connect, health content, etc.) ->
**No** -> Save.

## 10. Select an app category and provide contact details
- **App category:** Education
- **Tags:** pick the AI / education / developer-tools tags Play offers.
- **Contact details:** an **email** you monitor (e.g. support@lillytechsystems.com);
  optionally website `https://www.lillytechsystems.com/ai-school`.
- Save.

## 11. Set up your store listing
- **App name (<=30):**
  `AI School: Listen & Learn AI`
- **Short description (<=80):**
  `Learn AI by ear: audio lessons on models, tools & APIs, phone or car.`
- **Full description:**
  ```
  AI School turns the fast-moving world of AI into lessons you can listen to.
  Every lesson is audio-first, rewritten into a clear spoken script, so you can
  learn the way you listen to a podcast: hands-free, eyes-up, on the go.

  LEARN BY EAR
  - Focused, few-minute lessons you can listen to anywhere.
  - Real, current topics: large language models, AI coding tools, and the APIs
    that power them.
  - A growing catalog that updates with new lessons - no app update needed.

  BUILT FOR THE PHONE AND THE CAR
  - On your phone: listen first, then open the full written lesson to read the
    details and code when you have a screen.
  - In the car: a distraction-safe, audio-only experience designed for driving.

  WHAT YOU WILL LEARN
  - Frontier models: GPT-5, GPT-4o, Claude Opus, Sonnet and Haiku, Gemini.
  - AI coding tools: Claude Code, Cursor, GitHub Copilot, Aider, Continue, Cline.
  - How to choose models, manage context and cost, and ship AI in production.

  The catalog grows over time as new lessons are published.

  AI School is a product of Lilly Tech Systems. Learn more at
  lillytechsystems.com/ai-school.
  ```
- **App icon:** upload `android/playstore/icon-512.png`
- **Feature graphic:** upload `android/playstore/feature-graphic.png`
- **Phone screenshots:** upload the four in `android/playstore/screenshots/` (min 2).
- Save.

Each task shows a green check when saved. When all 11 are done, the release
tracks unlock.

---

# Release the app

## 12. Internal testing first (instant, no review - recommended)
1. Left menu -> **Test and release -> Testing -> Internal testing -> Create new release**.
2. If prompted about signing, **use Google Play App Signing** (Google holds the
   app signing key; your upload key stays the upload key).
3. **Upload** `app-mobile-release.aab`.
4. Add release notes -> **Save -> Review release -> Start rollout to Internal testing**.
5. **Testers** tab: add your email (up to 100). Copy the **opt-in link**, open it
   on an Android phone, install, and confirm the app runs and **streams the
   lessons** (needs internet).

## 13. Promote to Production
1. When internal testing looks good: **Production -> Create new release** (or
   **Promote** the internal release).
2. Same AAB, add release notes -> **Review -> Start rollout to Production**.
3. Google reviews (typically a few days). Production publishing also needs your
   **account verification** complete (identity + org website + phone).

---

## 14. After launch: add more lessons with no new release
Content streams from the hosted feed, so adding tracks needs no Play update:
```bash
cd pipeline
export ANTHROPIC_API_KEY=sk-ant-...
make feed TTS_BACKEND=say MODEL=claude-haiku-4-5 MAX_PATHS=10 MAX_TOPICS=5 \
  AUDIO_URL_BASE=https://toshanthi-stack.github.io/ai-school-feed/audio
.venv/bin/python publish_to_feed.py
```
The app picks up new tracks on next launch. Upload a new AAB only for app code
changes (new features / fixes).

## 15. iOS (separate track, later)
Needs the Apple Developer Program ($99/yr). Ship the iOS phone app first; CarPlay
needs Apple to grant the `com.apple.developer.carplay-audio` entitlement.
Bundle ID: `com.lillytech.aischool`. See `ios/README.md`.
