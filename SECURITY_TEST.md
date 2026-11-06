# Security Vulnerability Test — RCY-RPI Android v1.1.0

**Scope:** full static audit of the Android client against the live web
backend (`Invokeil/rcyrpi` — https://rcyrpi.pages.dev), plus an API-contract
verification of every endpoint the app calls.
**Method:** manual code review + automated secret-pattern scan +
web-API/response shape cross-check (route-by-route).

---

## 1. CRITICAL — API contract mismatches (fixed)

The app *compiled fine but silently failed at runtime* against the current
website. All of these were verified against the web repo's route code and
fixed in this release:

| # | Endpoint | App sent | Website expects | Effect before fix | Fix |
|---|---|---|---|---|---|
| 1 | `POST /api/notifications` | `{ids: ["notice:x", …], action}` | `{notice_ids?/app_ids?}` — raw ids, no `notice:`/`app:` prefix | "mark as read" silently did nothing → unread badge never cleared | ids split by prefix in `AppViewModel.markAllRead()` + `NotificationsScreen`; body = `MarkNotifsBody` |
| 2 | `PATCH /api/profile` | **POST** + partial body | **PATCH** + strict zod body (`full_name` ≥3, `email`, `phone` ≥10, `address.district/upazila/thana` ≥2) | profile edit → **405 Method Not Allowed** | `@PATCH` in `Api.kt` + full client-side validation in `ProfileEditScreen` |
| 3 | `POST /api/profile/password` | `{current, password}` | `{current_password, new_password}` | change password → **400 invalid_input** | `@SerialName` mapping in `ChangePasswordBody` |
| 4 | `POST /api/auth/forgot` | `{id}` | `{email}` (optional `lang`) | reset mail never sent → **400 invalid_email** | `ForgotBody(email=…)` |
| 5 | `POST /api/auth/signup` | no consent field | `agree_terms: z.literal(true)` | signup → **400 invalid_input** | consent checkbox added to the review step + `agree_terms` in `SignupBody` |
| 6 | `GET /api/certificates` | no query | member scope needs `?mine=1` (without it: admin-only) | member certificates → **401 unauthorized** | `certificates(mine = "1")` in both screens |

Verified-correct (no change needed): login `{id,password,role}` · me
`{user,impersonator}` · rsvp `{going}` → `{ok,mine}` · reset `{token,password}`
· leaderboard `{rows,rules}` · public-stats `{stats}` · checkout `{item_id}` ·
emergency request kinds/urgency enums · verify `{code}`.

Also hardened: emergency-request submit now caps every field to the
website's zod limits (phone 20, location 120, note 600, …) so long input can
never produce a 400.

## 2. Security posture — PASS

| Check | Result |
|---|---|
| Hardcoded secrets (API keys, JWTs, passwords — pattern scan over `*.kt/*.kts/*.xml/*.toml/*.properties/*.json`) | ✅ none — all secrets stay server-side |
| Cleartext traffic | ✅ `usesCleartextTraffic="false"`, HTTPS-only endpoints, no `network_security_config` exceptions |
| Exported components | ✅ only `MainActivity` exported (launcher + 2 allow-listed deep-link hosts); `FileProvider` `exported=false` + `grantUriPermissions` |
| WebView exposure | ✅ no WebView in the app |
| TLS | ✅ OkHttp defaults (modern TLS), no custom trust manager / hostname verifier overrides |
| Logging leaks | ✅ `HttpLoggingInterceptor` = BASIC in debug, **NONE in release** — cookies/headers never logged |
| Session storage | ✅ `rcy_session` cookie in private-app DataStore; server-side HMAC-signed, 7-day expiry, token-version revocation (server kills sessions on password change/suspend) |
| Deep links | ✅ restricted to `rcy.invokeil.bond` + `rcyrpi.pages.dev`; URL parsed only into internal routes (`/verify`, `/n/[id]`, `/certificate/[id]`) — anything else ignored |
| Backup / data extraction | ⚠️→✅ was `allowBackup="true"` (session cookie could be lifted via cloud/device backup) → **now `false`** |
| Least privilege | ⚠️→✅ `CALL_PHONE` was declared but the app only uses `ACTION_DIAL` (dialer pre-fill) → **permission removed** |
| Dependencies | ✅ okhttp 4.12.0 · retrofit 2.11.0 · room 2.6.1 · kotlinx-serialization 1.7.3 — no known CVEs in use; minSdk 29, targetSdk 35 |
| Release hardening | ✅ R8 minify + resource shrinking, serialization rules preserved |

## 3. Residual risks (accepted / recommended)

1. **Session cookie in plain DataStore** — readable only on rooted devices.
   Acceptable for a community app; if hardened later, wrap the DataStore
   value in a Keystore AES-GCM cipher.
2. **No certificate pinning** — impractical here (backend runs on
   `pages.dev` + a custom domain, pins would break on rotation). HTTPS +
   HSTS on the origin covers the realistic threat model.
3. **FCM is stubbed out** — when enabled, add the `POST_NOTIFICATIONS`
   runtime permission flow and keep the token server-side only.
4. Rate limiting / abuse protection lives **server-side** (login 10/5 min,
   requests 3/10 min, forgot 3/5 min, rsvp 12/5 min) — the client must never
   be the enforcement point. ✅ already the case.

## 4. Verdict

**v1.1.0 — PASS.** All 6 contract breaks fixed, 2 manifest-level issues
(backup, unused permission) resolved, no hardcoded secrets, no high/critical
vulnerabilities found in the client code path.
