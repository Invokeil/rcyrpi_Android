<div align="center">

# RCY-RPI — Android App

**Official member app for the Youth Red Crescent Team, Rangpur Govt. Polytechnic Institute**

Kotlin · Jetpack Compose · Material 3 — mirrors the live website
[rcy.invokeil.bond](https://rcy.invokeil.bond) ([source](https://github.com/Invokeil/rcyrpi))

</div>

---

## Features

**Same UI & UX as the website, adapted for Android:**

- 🔴 **Exact brand system** — brand red `#e30613` / green `#00734a`, 135° gradients, 12px radius + pill buttons, Inter + Noto Sans Bengali
- 🌐 **Bilingual BN/EN** — the website's full 455-key dictionary, instant toggle from the header
- 🌓 **Light + Dark theme** — site token-exact dark palette (`#121217`), system/light/dark switch
- 🏠 **Home** — all 10 sections: hero, live stats, emergency board, activities, events, leaderboard, weekly schedule, principles, about, join CTA
- 📢 **Notices** — search, category tabs, pinned + severity badges, share (WhatsApp/Facebook/Messenger/Telegram/copy)
- 📅 **Events & RSVP** — upcoming/past tabs, "আমি যাবো" one-tap RSVP (login-gated), map links
- 🏆 **Leaderboard** — bloods/streaks/hours tabs, badges, donor eligibility calculator (120/180-day rule)
- 💳 **Payments** — funds & my-payments, PipraPay checkout (opens gateway), mock-checkout success flow
- 🆘 **Emergency Request** — all 7 request kinds, urgency levels, BD geo pickers (63 districts / 481 upazilas / thanas), validation, national-999 shortcut
- 🚨 **LIVE ticker + emergency popup** — rotating urgent headlines under the sticky header, global popup with one-tap Call/SMS response (instant cancel on mobile, like the site)
- 👤 **Profile & digital ID card** — gradient member card with **verification QR** (`/verify?code=…`), PNG share, badges, donation log, certificates
- 🔐 **Auth** — login (member/admin tabs), 4-step signup wizard, forgot/reset password; the same HMAC `rcy_session` cookie as the web
- 🔔 **Notification center** — unread badge, mark-all-read, tap-through links
- 📴 **Offline-first** — Room snapshot cache: last-seen notices/events/stats still open without network; offline banner
- ☁️ **Server switcher** — main site ↔ `pages.dev` mirror ↔ custom URL, with **Cloudflare-challenge detection** and a friendly hint
- 🚀 **FCM-ready** — google-services plugin + service stub are pre-wired but commented (see below)

## Project layout

```
app/src/main/java/cfd/invokeil/himadri/rcy/
  core/
    i18n/      Lang.kt · Strings.kt (generated, 455 keys) · MobileStrings.kt
    network/   ApiClient (cookie jar + CF-challenge interceptor) · Api.kt · Models.kt
    storage/   SettingsStore (DataStore) · CacheDb (Room offline cache)
    theme/     Color · Type (Inter + Noto Sans Bengali) · Theme (light/dark)
    util/      DateFmt · Geo (assets) · EligibilityCalc · QrCode · ShareUtil · Biometric · Repos
  ui/
    components/  Brand (buttons/cards/fields) · States (skeleton/error/empty/api) · ShareRow · AppIcon
    scaffold/    MainScaffold (header + LIVE ticker + drawer + bottom bar) · ChildGraph · Drawer · EmergencyPopup
    nav/         RCYNavGraph (splash → onboarding → shell; deep links)
  screens/     home · notices · events · leaderboard · payments · emergency · auth · profile · misc · onboarding
  vm/          AppViewModel (session, ticker, notifications, settings)
```

Package: `cfd.invokeil.himadri.rcy` · minSdk 29 (Android 10+) · targetSdk 35

## Build

```bash
git clone https://github.com/Invokeil/rcyrpi_Android.git
cd rcyrpi_Android
# open in Android Studio (Ladybug+ recommended), or:
./gradlew assembleDebug          # debug APK
./gradlew assembleRelease        # unsigned release APK (R8 on)
```

- **Server** defaults to `https://rcy.invokeil.bond`. If Cloudflare's browser challenge blocks native requests, switch to the mirror `https://rcyrpi.pages.dev` in **Settings → Server** (in-app, no rebuild).
- The geo dataset, fonts and logos ship inside the APK (`assets/`, `res/font`).

## CI releases (GitHub Actions)

`.github/workflows/release.yml` builds **debug + release APKs** on every pushed
`v*` tag and publishes a GitHub Release automatically:

```bash
git tag v1.0.0 && git push origin v1.0.0
```

To sign release APKs in CI, add repo secrets `SIGNING_KEYSTORE_B64`,
`SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD` and
uncomment the signing block in the workflow.

## Enabling push notifications (FCM) later

The scaffold is pre-wired; to switch it on:

1. Create a Firebase project → add Android app with package `cfd.invokeil.himadri.rcy`
2. Download `google-services.json` → `app/google-services.json` (already git-ignored)
3. Uncomment in `gradle/libs.versions.toml`: the `google-services` plugin line + `firebase-bom`/`firebase-messaging` libraries
4. Uncomment in root `build.gradle.kts` and `app/build.gradle.kts`: the `google.services` plugin lines + FCM dependencies
5. Uncomment `POST_NOTIFICATIONS` permission + `.push.RCYFirebaseMessagingService` in `AndroidManifest.xml`
6. Implement `push/RCYFirebaseMessagingService.kt` (registration scaffold included in `RCYApp`)

## API

The app talks to the same server APIs as the website (~21 routes: `auth/*`,
`notices`, `items`, `leaderboard`, `public-stats`, `emergency/feed`,
`requests`, `events/[id]/rsvp`, `profile`, `payments/checkout`,
`certificates`, `verify/[code]`, `notifications`, `schedule`, `contacts`,
`templates`, `content`). Deploy the website backend from
[Invokeil/rcyrpi](https://github.com/Invokeil/rcyrpi) (Cloudflare Pages +
Supabase SQL scripts 01→11).

## License

[MIT](./LICENSE) © 2026 Youth Red Crescent Team, Rangpur Govt. Polytechnic Institute
