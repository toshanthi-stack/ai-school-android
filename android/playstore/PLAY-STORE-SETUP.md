# AI School - Google Play setup & submission (step by step)

Everything you need to publish the **mobile** app, with the exact answers inline.
The Android Automotive flavor is a separate gift to VW, not published here.

Publisher: **Lilly Tech Systems LLC** (Organization account).

---

## 0. Files you'll upload (already built, in this repo)
- **App bundle (AAB):** `android/app-mobile/build/outputs/bundle/release/app-mobile-release.aab`
- **App icon (512x512):** `android/playstore/icon-512.png`
- **Feature graphic (1024x500):** `android/playstore/feature-graphic.png`
- **Phone screenshots:** `android/playstore/screenshots/01-home.png … 04-lesson.png`
- **Privacy policy URL (live):** https://toshanthi-stack.github.io/ai-school-feed/privacy.html

---

## 1. Account verification (one-time, may still be processing)
On the Play Console home you may see "Finish setting up your developer account":
- **Google is verifying your identity** - wait for Google's email (a few days).
- **Verify your organization's website** and **phone** - these unlock after
  identity verification. Do them when prompted.

You can prepare the app and store listing while this processes; **publishing to
production needs verification complete.**

## 2. Back up the upload key (IMPORTANT - do once)
Already done if you saved these. The release is signed with a key on your Mac;
lose it (without Play App Signing recovery) and you can't update the app.
- Keystore file: `android/keystore/upload-keystore.jks`
- Password / alias: in `android/keystore.properties` (gitignored, only on your Mac)
Back up BOTH off the machine (you saved the .jks to OneDrive and the password to
an iCloud note).

---

## 3. On the Play Console Dashboard: "Set up your app"
Click **Set up your app** and work through each task. Exact answers:

### App access
- Choose **"All functionality is available without special access"** (the app
  has no login or gated areas).

### Ads
- **"No, my app does not contain ads."**

### Content rating
- Start the questionnaire. Category: **Reference / Education**.
- Answer **No** to everything: violence, sexual content, profanity, controlled
  substances, gambling, user-to-user communication, user-generated content,
  data sharing for the rating.
- Result: an **Everyone** rating.

### Target audience and content
- Target age groups: **13+** (select 13-15, 16-17, 18+). Do **not** include
  under-13.
- "Is your app designed for children / appealing to children?" -> **No**.
- This keeps you out of the Families policy program.

### Data safety
- "Does your app collect or share any required user data types?" -> **No.**
  (v1 has no login, no analytics SDK, no ads SDK; it only fetches/streams content.)
- "Is all of the user data encrypted in transit?" -> **Yes** (all HTTPS).
- "Do you provide a way for users to request that their data is deleted?" ->
  N/A / not applicable (no account, no data collected).
- Caveat: the app opens lillytechsystems.com lesson pages in an in-app browser.
  That's the website's own domain; if you want to be conservative you could
  later disclose "Web browsing" as collected by the embedded site. For v1 with
  no in-app SDKs, **No data collected** is the honest answer.

### Other declarations (answer No / as noted)
- **News app?** No.
- **COVID-19 contact tracing / status app?** No.
- **Government app?** No.
- **Financial features?** No.
- **Health?** No.

### Store listing (the public page)
Paste these (also in `listing.md`):

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
- **Phone screenshots:** upload the four in `android/playstore/screenshots/`
  (Play needs at least 2).
- **App category:** Education
- **Tags:** pick AI / education / developer-tools related tags Play offers.
- **Contact email:** an inbox you monitor (e.g. support@lillytechsystems.com).
- **Privacy policy:** paste the live URL from section 0.

### Store settings
- **App category:** Education. **Contact details:** your email/website.
- **Pricing:** Free (under Monetization setup or the pricing page).

Work down the checklist until every "Set up your app" task shows a green check.

---

## 4. Create a release
The release tracks are locked until the setup tasks above are done. Then:

### 4a. Internal testing first (recommended - instant, no review)
1. Left menu -> **Testing -> Internal testing -> Create new release**.
2. When prompted about signing, **use Google Play App Signing** (let Google
   manage the app signing key; your upload key stays the upload key).
3. **Upload** `app-mobile-release.aab`.
4. Add a release name + short notes, **Save -> Review release -> Start rollout
   to Internal testing**.
5. Under **Testers**, add your own email (and anyone else, up to 100). Copy the
   **opt-in link**, open it on an Android phone, install, and confirm the app
   runs and **streams the lessons** (needs internet).

### 4b. Promote to Production
1. Once internal testing looks good: **Production -> Create new release** (or
   **Promote** the internal release to Production).
2. Confirm the same AAB, add release notes, **Review -> Start rollout to
   Production**.
3. Google reviews it (typically a few days). Organization accounts publish
   straight to production once account verification is complete.

---

## 5. After launch: adding more lessons (no new release needed)
Content streams from the hosted feed, so adding tracks does NOT require a Play
update:
```bash
cd pipeline
export ANTHROPIC_API_KEY=sk-ant-...
make feed TTS_BACKEND=say MODEL=claude-haiku-4-5 MAX_PATHS=10 MAX_TOPICS=5 \
  AUDIO_URL_BASE=https://toshanthi-stack.github.io/ai-school-feed/audio
.venv/bin/python publish_to_feed.py
```
The app picks up new tracks on next launch. You only upload a new AAB for app
code changes (new features, fixes).

---

## 6. iOS (separate track, later)
The App Store needs the Apple Developer Program ($99/yr). Ship the iOS phone app
first; CarPlay needs Apple to grant the `com.apple.developer.carplay-audio`
entitlement. Bundle ID: `com.lillytech.aischool`. See `ios/README.md`.
