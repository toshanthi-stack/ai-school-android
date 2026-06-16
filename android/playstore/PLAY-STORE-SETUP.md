# AI School - Google Play setup & submission guide

Step-by-step to get the **mobile** app on Google Play. (The Android Automotive
flavor is a separate gift to VW, not published here.)

Publisher: **Lilly Tech Systems LLC** (Organization account).

---

## What I (the build side) have already prepared
- Signed release **AAB** (~13 MB - streams content from the hosted feed):
  `android/app-mobile/build/outputs/bundle/release/app-mobile-release.aab`
- Hi-res **icon** (512): `android/playstore/icon-512.png`
- **Feature graphic** (1024x500): `android/playstore/feature-graphic.png`
- **Screenshots** (1080x2160): `android/playstore/screenshots/01-04`
- **Listing copy** (title, descriptions, data-safety/content-rating notes): `android/playstore/listing.md`
- **Privacy policy** text to host: `android/playstore/privacy-policy.md`

---

## Step 0 - Account type (decided): Organization
An Organization account avoids Google's "20 testers for 14 days" closed-test
gate that applies to new *personal* accounts, so you can go straight to
production. It needs business verification (incl. a free D-U-N-S number).

## Step 1 - Create the Play Developer account (DONE)
Account created. Finish any remaining **business/identity verification** Google
asks for (Organization accounts need it before publishing).

### (original instructions, for reference)
1. Go to **play.google.com/console**, sign in with the Google account you want
   to own it.
2. Choose **Organization** as the account type.
3. Pay the **$25 one-time** registration fee.
4. Complete identity + business verification (D-U-N-S number for Lilly Tech
   Systems LLC; get one free at dnb.com if you don't have it). Verification can
   take a few days.

## Step 2 - Back up the upload key (you) - IMPORTANT
The release is signed with an upload key on your Mac. Lose it and (without Play
App Signing recovery) you can't update the app. Back up BOTH, somewhere safe
(password manager / secure cloud):
- Keystore file: `android/keystore/upload-keystore.jks`
- Its password: in `android/keystore.properties` (gitignored - it is only on
  your Mac, never committed)

## Step 3 - Privacy policy URL (DONE - hosted for you)
Already published and live (free, on the content host):
**https://toshanthi-stack.github.io/ai-school-feed/privacy.html**
Paste that into the Play listing's Privacy policy field. (If you later want it on
lillytechsystems.com, the text is in `privacy-policy.md`.)

## Step 4 - Create the app in Play Console (you)
1. **Create app** -> name "AI School: Listen & Learn AI", Education, Free, App.
2. Paste the **listing** from `android/playstore/listing.md`: short + full
   description.
3. Upload graphics: **icon-512.png**, **feature-graphic.png**, and the 4
   **screenshots**.
4. Add the **privacy policy URL** from Step 3.

## Step 5 - Upload the build (you)
1. **Release > Production > Create new release** (or Internal testing first to
   smoke-test).
2. When prompted, **enable Google Play App Signing** (recommended - Google
   holds the app signing key; your upload key stays your upload key and is
   recoverable).
3. Upload `app-mobile-release.aab`.

## Step 6 - Policy forms (you)
- **Data safety**: see the notes in `listing.md` (v1 collects no data itself;
  confirm based on what your in-app web view loads).
- **Content rating** questionnaire -> expected "Everyone".
- **Target audience**, **ads = No** (for v1), countries, pricing = Free.

## Step 7 - Submit (you)
Submit for review. Google typically reviews in a few days. Organization
accounts publish straight to production (no closed-test gate).

---

## Notes
- If the app's content (lessons/audio) changes, I rebuild the AAB and refresh
  the screenshots; you upload the new AAB as a new release.
- iOS App Store is a separate track ($99/yr; ship phone-only first since CarPlay
  needs Apple to grant the CarPlay audio entitlement).
