# Smart Network Analyzer: Phased Build Prompts

This file is the complete build instruction set for the coding agent. Read the whole file first, then execute Phase 1 to Phase 15 in order, in one continuous run.

---

## 0. Global Instructions (apply to every phase)

### 0.1 Project and paths

Main folder: `C:\Users\srike\Desktop\WORK\CODE CURRENT\projects\Smart Network Analyzer`

```
Smart Network Analyzer/
├── BUILD_PROMPTS.md      (this file)
├── PROGRESS.md           (you create and maintain this)
├── README.md             (you create this in Phase 15)
├── Backend/              (Python FastAPI, you create everything inside)
└── Frontend/             (existing Android Studio project, Java)
```

- Backend code goes only inside `Backend/`.
- Android code goes only inside `Frontend/`. This is an already created Android Studio project (Gradle Kotlin DSL: `build.gradle.kts`, `settings.gradle.kts`). Inspect `Frontend/app/build.gradle.kts` first to learn the package name, `minSdk`, `compileSdk` and the language of the generated activity. **All app code must be written in Java.** If the template generated Kotlin files, convert or replace them with Java equivalents. Keep the existing package name.
- A website frontend will be built later and will consume the same API. Do NOT build it now. Just keep the API clean, CORS-enabled and consistently shaped.
- Do not modify anything outside the main folder.

### 0.2 Product summary

Smart Network Analyzer monitors Wi-Fi and internet performance, detects common issues and gives AI-based recommendations. Wi-Fi scanning, speed tests, ping and signal readings run **on the Android device** (a backend cannot see a user's local network). The app collects the data and POSTs it to the backend. The backend handles auth, storage, statistics, analysis, security rules and reports.

Out of scope (do not build): changing router settings, repairing infrastructure, guaranteeing speeds, penetration testing, port scanning.

### 0.3 Stack

**Backend:** Python 3.11+, FastAPI, SQLAlchemy 2.0, Alembic, Pydantic v2 (+ pydantic-settings), SQLite for dev (must work on PostgreSQL by changing only `DATABASE_URL`), `bcrypt` package directly for hashing (do not use passlib), PyJWT, httpx, ReportLab, pytest, uvicorn.

**Android:** Java, MVVM (ViewModel + LiveData), Retrofit2 + OkHttp3 + Gson, Material 3 components, AndroidX Navigation (bottom navigation), MPAndroidChart (needs the JitPack repo in `settings.gradle.kts`), `androidx.security:security-crypto` (EncryptedSharedPreferences), SwipeRefreshLayout, ViewBinding enabled.

### 0.4 Working rules

1. Work phase by phase in order. Do not ask questions or wait for approval between phases. If something is ambiguous, pick the most sensible option, note it in `PROGRESS.md`, and continue.
2. After each phase, verify it: for backend phases run the app and the tests; for Android phases run `gradlew assembleDebug` from `Frontend/` and fix every compile error before moving on.
3. Maintain `PROGRESS.md` at the main folder: after each phase append what was built, files added, assumptions made, and anything left incomplete.
4. Write production-quality code: type hints, small focused modules, no dead code, no placeholder TODOs, no fake or hard-coded data in the app (demo data only through the seed script).
5. Never hard-code secrets. All secrets and settings come from `Backend/.env` (provide `.env.example`).
6. Every phase must leave the project in a working, compiling state.
7. Match the API contract in section 0.5 exactly, on both sides. The backend and the Android DTOs must agree field for field.

### 0.5 Shared API contract

Base path: `/api/v1`. JSON keys are `snake_case`. All timestamps are ISO 8601 UTC strings. IDs are integers. All endpoints except signup, login, refresh, forgot-password, reset-password and health require `Authorization: Bearer <access_token>`. Every record is scoped to the authenticated user; accessing another user's record returns 404.

**Conventions**
- Success: the resource JSON directly (no wrapper). Create returns 201. Delete returns 204.
- Lists: `{ "items": [...], "total": int, "page": int, "page_size": int }`, query params `page` (default 1), `page_size` (default 20, max 100), plus `from_date` and `to_date` filters where noted, newest first.
- Errors: `{ "error": { "code": "string_code", "message": "human readable", "details": null | object } }` with proper HTTP status (400, 401, 403, 404, 409, 422, 429, 500). Validation errors map to code `validation_error` with details listing the fields.

**Auth and users**
- `POST /auth/signup` body `{name, email, password}` (password min 8 chars) returns 201 `{user, access_token, refresh_token, token_type:"bearer"}`. Duplicate email returns 409 `email_taken`.
- `POST /auth/login` body `{email, password}` returns same shape as signup. Bad credentials return 401 `invalid_credentials`.
- `POST /auth/refresh` body `{refresh_token}` returns `{access_token, refresh_token, token_type}` (rotate the refresh token, revoke the old one).
- `POST /auth/logout` body `{refresh_token}` returns 204.
- `POST /auth/forgot-password` body `{email}` always returns 200 `{message, reset_token}`. `reset_token` is only included when `APP_ENV=development`; otherwise omitted. Do not reveal whether the email exists.
- `POST /auth/reset-password` body `{token, new_password}` returns 200 `{message}`. Tokens are single-use and expire in 30 minutes. Reset revokes all refresh tokens.
- `GET /users/me` returns `{id, name, email, created_at}`.
- `PUT /users/me` body `{name?, email?}` returns the user.
- `PUT /users/me/password` body `{current_password, new_password}` returns 200 `{message}`.
- `DELETE /users/me` returns 204 and cascades all user data.

**Wi-Fi scans**
- `POST /wifi/scans` body `{scanned_at?, location_label?, networks:[{ssid, bssid?, signal_dbm, frequency_mhz, channel, security, is_connected}]}`. `security` is one of `OPEN, WEP, WPA, WPA2, WPA3, WPA2_WPA3, UNKNOWN`. Returns 201 scan `{id, scanned_at, location_label, network_count, networks:[...]}`.
- `GET /wifi/scans` paginated list. Items omit `networks` and include `network_count`, `strongest_signal_dbm`, `connected_ssid`.
- `GET /wifi/scans/{id}` full scan. `DELETE /wifi/scans/{id}`.

**Speed tests**
- `POST /speed-tests` body `{download_mbps, upload_mbps, ping_ms?, server?, network_type, ssid?, tested_at?}` where `network_type` is `wifi` or `mobile`. Returns 201 record with `id`.
- `GET /speed-tests` paginated, with `from_date`/`to_date`. `GET /speed-tests/{id}`. `DELETE /speed-tests/{id}`.
- `GET /speed-tests/stats` (optional `from_date`, `to_date`) returns `{count, avg_download_mbps, avg_upload_mbps, max_download_mbps, min_download_mbps, max_upload_mbps, min_upload_mbps, best_test_id, worst_test_id, trend:[{date, avg_download_mbps, avg_upload_mbps}]}`. Define the route so `/stats` is matched before `/{id}`.

**Latency tests**
- `POST /latency-tests` body `{target_host, avg_ms, min_ms, max_ms, jitter_ms, packet_loss_percent, packets_sent, packets_received, ssid?, tested_at?}`. Returns 201.
- `GET /latency-tests` paginated with date filters. `GET /latency-tests/{id}`.
- `GET /latency-tests/stats` returns `{count, avg_ms, min_ms, max_ms, avg_jitter_ms, avg_packet_loss_percent, rating, trend:[{date, avg_ms}]}`. `rating` is `excellent` (avg < 30 ms and loss < 1 %), `good` (< 60 ms, loss < 2 %), `fair` (< 100 ms, loss < 5 %), else `poor`.

**Signal maps**
- `POST /signal-maps` body `{name, description?, ssid?}` returns 201 map `{id, name, description, ssid, point_count, created_at}`.
- `GET /signal-maps` paginated list of maps with `point_count`, `avg_rssi_dbm`.
- `GET /signal-maps/{id}` returns the map plus `points:[{id, label, x, y, rssi_dbm, ssid, recorded_at}]`. `x` and `y` are optional floats normalised 0 to 1 for grid position.
- `POST /signal-maps/{id}/points` body `{label, x?, y?, rssi_dbm, ssid?, recorded_at?}` returns 201 point.
- `DELETE /signal-maps/{id}/points/{point_id}`, `DELETE /signal-maps/{id}` return 204.
- `GET /signal-maps/{id}/summary` returns `{total_points, avg_rssi_dbm, strongest_point, weakest_point, zone_counts:{strong, fair, weak}, points:[{id, label, rssi_dbm, zone}], verdict}`. Zone thresholds (single shared constants): `strong` rssi >= -60, `fair` -75 <= rssi < -60, `weak` rssi < -75.

**Data usage**
- `POST /data-usage` body `{period_start, period_end, wifi_rx_bytes, wifi_tx_bytes, mobile_rx_bytes, mobile_tx_bytes}`. Idempotent per user and `period_start`/`period_end` pair (upsert). Returns 201 or 200.
- `GET /data-usage` paginated history with date filters.
- `GET /data-usage/summary?period=daily|weekly|monthly` returns `{period, buckets:[{label, start, end, wifi_bytes, mobile_bytes, total_bytes}], totals:{wifi_bytes, mobile_bytes, total_bytes}, avg_daily_bytes, peak_bucket_label}`.

**Security scans**
- `POST /security-scans` body `{ssid, bssid?, security_type, signal_dbm?, gateway_ip?, local_ip?, devices:[{ip, hostname?, mac?}], scanned_at?}`. The server runs the security rules engine and returns 201 `{id, ssid, security_type, risk_level, security_score, issues:[{severity, title, description}], recommendations:[{priority, title, description}], devices, scanned_at}`. `risk_level` is `low|medium|high|critical`, `security_score` is 0 to 100. `severity` is `info|low|medium|high|critical`.
- `GET /security-scans` paginated summary list. `GET /security-scans/{id}` full detail.

**AI analysis**
- `POST /analysis` body `{days?: 7}` (1 to 90) returns 201 `{id, source:"ai"|"rules", health_score, summary, issues:[{severity, category, title, description}], recommendations:[{priority, title, description}], metrics_snapshot:{...}, created_at}`. `category` is one of `speed, latency, signal, interference, security, usage, general`. `priority` is `high|medium|low`. If there is no data in the window return 422 `no_data` with a helpful message.
- `GET /analysis` paginated list (omit full issue text, include `issue_count`). `GET /analysis/{id}` full.

**Reports**
- `POST /reports` body `{title?, start_date, end_date}` returns 201 report `{id, title, start_date, end_date, created_at, sections:{speed, latency, signal, data_usage, security, issues, recommendations}, health_score, source}`. The report stores a snapshot, so later data changes do not alter it.
- `GET /reports` paginated list. `GET /reports/{id}`. `GET /reports/{id}/export` returns `application/pdf` with `Content-Disposition: attachment`. `DELETE /reports/{id}` returns 204.

**Dashboard and utility**
- `GET /dashboard` returns `{latest_speed_test, latest_latency_test, latest_wifi_scan_summary, current_signal_dbm, latest_analysis:{id, health_score, summary, issues, source, created_at}, counts:{speed_tests, latency_tests, wifi_scans, signal_maps, reports}}`. Any missing item is `null`.
- `GET /health` (no auth) returns `{status:"ok", version, time}`.

---

# BACKEND

## PHASE 1: Backend Foundation

**Prompt:**

Create the backend in `Backend/`. Set up a Python virtual environment instruction in the README (do not commit it) and a `requirements.txt` with pinned versions.

Structure:
```
Backend/
├── app/
│   ├── main.py
│   ├── core/        (config.py, database.py, security.py, errors.py, logging.py, pagination.py)
│   ├── models/      (SQLAlchemy models, one file per domain)
│   ├── schemas/     (Pydantic schemas per domain)
│   ├── routers/     (one router per domain)
│   ├── services/    (business logic, analysis, pdf, llm)
│   └── deps.py
├── alembic/  + alembic.ini
├── tests/
├── scripts/
├── .env.example
├── requirements.txt
└── README.md
```

Implement:
- Settings via pydantic-settings from `.env`: `APP_ENV`, `DATABASE_URL` (default `sqlite:///./smart_network.db`), `SECRET_KEY`, `ACCESS_TOKEN_EXPIRE_MINUTES` (30), `REFRESH_TOKEN_EXPIRE_DAYS` (30), `CORS_ORIGINS` (comma list, default `*` in development), `GROQ_API_KEY`, `GROQ_MODEL` (default `llama-3.3-70b-versatile`), `GROQ_TIMEOUT_SECONDS`.
- SQLAlchemy 2.0 engine, session dependency, declarative base. Enable SQLite foreign keys via pragma.
- Alembic configured against the app models with `render_as_batch=True` (SQLite). Use autogenerate migrations from this phase on and keep them applied (`alembic upgrade head`).
- Global exception handlers producing the error format from section 0.5, including validation errors.
- CORS middleware, request logging middleware (method, path, status, duration).
- Generic pagination helper returning the list envelope.
- `GET /api/v1/health`.
- API mounted under `/api/v1`, interactive docs at `/docs`.

**Done when:** `uvicorn app.main:app --reload` starts, `/api/v1/health` returns the contract response, and a basic test for health and the error format passes.

## PHASE 2: Auth and Users

**Prompt:**

Implement APIs for auth and users exactly as in the contract.

- Models: `User` (id, name, email unique and lowercased, password_hash, created_at, updated_at), `RefreshToken` (id, user_id, token_hash, expires_at, revoked, created_at), `PasswordResetToken` (id, user_id, token_hash, expires_at, used, created_at).
- Passwords: bcrypt. Access tokens: JWT with `sub`, `exp`, `type=access`. Refresh tokens: random secure strings, stored only as SHA-256 hashes, rotated on every refresh, revocable.
- `get_current_user` dependency that validates the bearer token and loads the user; reuse it everywhere later.
- Forgot password: generate a single-use token, log it to the console, and return it in the response only if `APP_ENV=development`. No email sending. Write the email step as a clearly isolated function `send_password_reset(email, token)` that currently logs, so a real provider can be added later.
- Deleting a user cascades all their data (set up `ondelete="CASCADE"` and ORM cascades so every later table is covered; add each new table to this cascade as you build it).
- Basic rate limiting on login and forgot-password (simple in-memory limiter, 10 requests per minute per IP, returning 429 `rate_limited`).
- Validate email format and password strength (min 8 characters).

**Done when:** pytest covers signup, duplicate email, login success and failure, refresh rotation (old token rejected), logout, reset flow including expired/used token, profile update, password change, delete account. All pass.

## PHASE 3: Wi-Fi Scans, Speed Tests, Latency Tests

**Prompt:**

Implement the Wi-Fi scan, speed test and latency test APIs exactly as in the contract (APIs for `/wifi/scans`, `/speed-tests`, `/latency-tests` including both `/stats` endpoints).

- Models: `WifiScan` + child `WifiNetwork`, `SpeedTest`, `LatencyTest`, all with `user_id` foreign keys and indexes on `(user_id, created/tested_at)`.
- Server-side validation: signal between -120 and 0 dBm, speeds and latencies non-negative, packet loss 0 to 100, `security` enum, channel 1 to 233.
- `tested_at`/`scanned_at` default to server time if omitted.
- Stats computed in SQL or efficient Python; trend is grouped by UTC date.
- Put the latency rating thresholds and any shared thresholds in `app/services/thresholds.py` as named constants so analysis, reports and stats all reuse them.
- Register `/speed-tests/stats` and `/latency-tests/stats` before the `/{id}` routes.

**Done when:** tests cover create, list pagination, date filter, get, delete, ownership isolation (user B cannot read user A's records) and stats correctness with known numbers.

## PHASE 4: Signal Maps

**Prompt:**

Implement all signal map APIs exactly as in the contract.

- Models: `SignalMap` and `SignalPoint` (cascade delete points with the map).
- Zone classification lives in one function `classify_rssi(rssi) -> "strong"|"fair"|"weak"` using the thresholds from `thresholds.py` (strong >= -60, fair >= -75, weak below).
- Summary endpoint computes totals, average, strongest and weakest points, zone counts and per-point zones. `verdict` is a plain-English one-sentence statement (for example "Most of this area has fair coverage, but 2 spots are weak: Bedroom and Balcony").
- Return an empty-but-valid summary when a map has no points.

**Done when:** tests cover map CRUD, point add/remove, summary correctness at threshold boundaries (-60, -75), empty map, and ownership isolation.

## PHASE 5: Data Usage and Security Scans

**Prompt:**

Implement the data usage and security scan APIs exactly as in the contract.

Data usage:
- Model `DataUsageRecord`. Upsert on `(user_id, period_start, period_end)`.
- Summary groups records into daily, weekly (ISO week) or monthly buckets over a sensible default window (last 14 days for daily, 8 weeks for weekly, 6 months for monthly), with totals, average daily usage and the peak bucket.

Security scans:
- Models `SecurityScan` and `ConnectedDevice` (cascade delete).
- Rules engine in `app/services/security_rules.py`, pure functions, fully unit-tested:
  - OPEN network: critical, "Unsecured network".
  - WEP: critical, broken encryption.
  - WPA (original): high, outdated.
  - WPA2: low or info, acceptable, suggest WPA3 if the router supports it.
  - WPA3 and WPA2_WPA3: info or no issue.
  - UNKNOWN: medium, could not determine security.
  - Very weak signal (< -80 dBm) on an open network: add a note about rogue access point risk.
  - Many connected devices (> 15): info, recommend reviewing the device list.
  - Gateway IP in a default-looking range is NOT a vulnerability, do not flag it.
- Output `risk_level`, `security_score` (0 to 100, start 100 and subtract by severity) and a deduplicated, prioritised recommendation list (for example: enable WPA2/WPA3, change default router admin password, disable WPS, keep router firmware updated, use a VPN on open networks, avoid sensitive logins on public Wi-Fi).
- Do not perform any network probing server-side.

**Done when:** tests cover every security type, the scoring, device-count rule, and ownership isolation; usage upsert and all three summary periods are tested.

## PHASE 6: AI Analysis Engine

**Prompt:**

Implement `/analysis` APIs exactly as in the contract, built in three layers inside `app/services/analysis/`:

1. **`metrics.py`: metrics aggregator.** For the last N days compute in code: speed averages/min/max, download and upload consistency (coefficient of variation), latency avg/jitter/packet loss, wifi scan stats (connected network signal, number of networks on the same channel as the connected one, nearby networks count, channel congestion on 2.4 GHz channels 1/6/11 and 5 GHz), signal map weak-zone ratio, latest security scan score/risk, data usage totals and trend. Return one typed dict `metrics_snapshot` (pure numbers and flags, no prose). Missing categories are simply absent.
2. **`rules.py`: rules engine.** Deterministic issues and recommendations from thresholds, reusing `thresholds.py`. Examples: download < 10 Mbps = slow; large gap between download and upload; high or unstable latency (avg > 100 ms, jitter > 30 ms); packet loss > 2 %; weak signal (< -75 dBm); channel congestion (3 or more networks on the same channel); weak-zone ratio > 30 % of mapped points; poor security score; abnormal data usage spike; Wi-Fi vs mobile speed differences. Each issue has severity, category, title and description, and each recommendation has priority, title and description with practical, concrete steps (move the router, switch to 5 GHz, change channel, restart router, use Ethernet for critical devices, add a mesh node, contact ISP with evidence, etc). Compute `health_score` 0 to 100 from weighted penalties. Always works, no network needed.
3. **`llm.py`: Groq provider.** One small provider class with a single method taking the metrics snapshot plus the rules output and returning a validated Pydantic model `{summary, issues[], recommendations[]}`. Call Groq's OpenAI-compatible endpoint `https://api.groq.com/openai/v1/chat/completions` through httpx with `response_format={"type":"json_object"}`, low temperature (0.2), the model and key from settings, and a timeout. The system prompt must instruct the model to: only use the numbers provided, never invent measurements, keep the output to the exact JSON schema, write for a non-technical user, and give specific actionable steps. Validate the response with Pydantic; on invalid JSON retry once, then fail.
   - **Fallback:** if there is no `GROQ_API_KEY`, or Groq errors, times out, rate-limits, or returns invalid output, return the rules-engine result with `source="rules"`. When Groq succeeds use `source="ai"`. The AI result may add, merge and reword, but the rules-based issues for critical findings must not be dropped. The numeric `health_score` always comes from code.
   - Keep the provider behind an interface so another provider can replace it by editing one file.

Persist each analysis in an `AnalysisResult` table (JSON columns for issues, recommendations and `metrics_snapshot`). Return 422 `no_data` when the window contains no measurements at all.

**Done when:** unit tests cover the aggregator with known data, each rule, health score bounds, Groq success (mock httpx), Groq failure leading to rules fallback, invalid JSON leading to retry then fallback, missing key leading to rules. The app must run fully with no Groq key.

## PHASE 7: Reports and Dashboard

**Prompt:**

Implement the reports and dashboard APIs exactly as in the contract.

- `POST /reports`: validate `start_date <= end_date` and a max range of 365 days. Build the sections from the stored data in that range (speed stats, latency stats, signal summary from wifi scans and maps, data usage totals, latest security result), run the Phase 6 analysis over the same window (reuse the services, do not duplicate logic), and store everything as a JSON snapshot in a `Report` table. Default title: "Network Report, <start> to <end>". If the range has no data return 422 `no_data`.
- `GET /reports`, `GET /reports/{id}`, `DELETE /reports/{id}`.
- `GET /reports/{id}/export`: generate a clean, professional PDF with ReportLab: title block, date range, overall health score, then sections (Speed, Latency, Signal Strength, Data Usage, Security, Detected Issues, AI Recommendations) with tables, severity wording, and a footer with generation time. Include a note whether recommendations came from AI or rules. Stream it with the right headers. Handle long text wrapping and page breaks.
- `GET /dashboard`: one efficient call returning the latest items as in the contract (`null` for missing). Counts via COUNT queries.

**Done when:** tests cover report creation, snapshot immutability, the PDF endpoint returning a valid PDF (starts with `%PDF`), dashboard with data and with an empty account, and ownership isolation.

## PHASE 8: Backend Hardening

**Prompt:**

- Ensure every table is covered by account deletion cascade (write a test that creates data in every table and deletes the user).
- Write `scripts/seed_demo.py` creating a demo user (`demo@example.com` / `Demo@1234`) with about 3 weeks of realistic data across all features (varied speeds, a few bad-latency days, wifi scans with congested channels, a signal map with strong/fair/weak points, usage records, security scans) so the dashboard and reports look rich.
- Add `Backend/api.http` (or a Postman collection) covering every endpoint in order.
- Add consistent indexes, `updated_at` where useful, and confirm no endpoint leaks another user's data.
- Run the entire test suite and fix failures. Run `alembic upgrade head` on a fresh database to confirm migrations are clean.
- Write `Backend/README.md`: setup (venv, install, `.env`), migrate, run (`uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload`), test, seed, env variables table, how the Groq key is set, and how to switch to PostgreSQL. Mention that a physical Android device needs the PC's LAN IP and that the Windows firewall must allow port 8000.

**Done when:** the full suite passes, a fresh clone setup following the README works, and all 46 endpoints appear at `/docs`.

---

# ANDROID

General Android rules for Phases 9 to 15:
- Java only, MVVM, ViewBinding. One Activity (`MainActivity`) hosting a NavHost for the main app, plus a small `AuthActivity` (or an auth nav graph) for login, signup and forgot password. Splash decides which one to open.
- Package structure under the existing package: `ui/` (per feature subpackages), `viewmodel/`, `data/` (`model/`, `repository/`, `local/`), `network/` (`ApiClient`, `ApiService`, interceptors), `utils/`.
- Models mirror the contract with `@SerializedName` snake_case fields.
- Every network call goes through repositories returning a small `Resource<T>` (loading, success, error) via LiveData. Errors parse the contract error body and show the `message` to the user. Never crash on network failure.
- Every screen has loading, empty and error states with a retry action.
- Material 3 theme with a consistent colour palette suited to a network tool (deep blue primary, teal accent), light and dark support, and a proper vector app icon.
- Run `gradlew assembleDebug` after each phase.

## PHASE 9: Android Foundation and Auth

**Prompt:**

In `Frontend/`:
- Inspect and update Gradle: add dependencies (Retrofit, Gson converter, OkHttp + logging interceptor, Material, Navigation, Lifecycle ViewModel/LiveData, security-crypto, SwipeRefreshLayout, MPAndroidChart with the JitPack repository in `settings.gradle.kts`). Enable ViewBinding and `buildConfig`. Use Java 17 compatibility. Adjust to whatever dependency mechanism the project uses (version catalog or direct strings).
- Permissions in the manifest: `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE`, `ACCESS_FINE_LOCATION`, `NEARBY_WIFI_DEVICES`, `PACKAGE_USAGE_STATS` (with the tools ignore attribute). Add a `network_security_config` that allows cleartext traffic for `10.0.0.0/8`-style local dev hosts and `10.0.2.2` (development only, document it).
- Base URL via `BuildConfig` field, default `http://10.0.2.2:8000/api/v1/` for the emulator, with a clearly commented place to change to a PC LAN IP for a physical device. Provide a hidden or settings-level override stored in preferences if easy.
- `ApiClient` singleton: OkHttp with an auth interceptor adding the bearer token and an authenticator that on 401 calls `/auth/refresh` once, saves the rotated tokens, retries the request, and on refresh failure clears the session and sends the user to login. Reasonable timeouts. Logging interceptor in debug only.
- `TokenManager` using EncryptedSharedPreferences for access token, refresh token and basic user info.
- Full `ApiService` interface for the whole contract now (all endpoints), plus all DTO model classes, so later phases only add UI and repositories.
- Screens: Splash, Login, Signup, Forgot Password (including a second step to enter the token and new password; in development the token is returned by the API, so show a small dev helper that pre-fills it when present). Input validation, password show/hide, progress indicators, error messages.
- Main shell: `MainActivity` with bottom navigation (Home, Wi-Fi, Tests, Map, More) and placeholder fragments for later features, plus the navigation graph. "More" will later hold Data Usage, Security, Analysis, Reports, Profile.

**Done when:** it builds, and signup, login, logout and automatic session restore work against the running backend.

## PHASE 10: Dashboard and Wi-Fi Scanner

**Prompt:**

- Dashboard (Home) fragment wired to `GET /dashboard`: cards for latest download/upload, latest ping/jitter/loss, current signal, overall health score ring with the latest analysis summary, quick-action buttons (Scan Wi-Fi, Run Speed Test, Analyze), pull-to-refresh, empty state for new users.
- Permission flow: a reusable `PermissionHelper` handling `ACCESS_FINE_LOCATION` (and `NEARBY_WIFI_DEVICES` on Android 13+) with a rationale dialog and a "go to settings" path after permanent denial. Also detect location services being off (required for scan results on many versions) and guide the user to enable them.
- Wi-Fi scanner using `WifiManager`: trigger a scan, register a receiver for results, fall back to the cached `getScanResults()` if throttled, and show a cooldown message and countdown instead of failing (Android throttles scans). Derive per network: SSID (show "Hidden network" if empty), BSSID, signal in dBm and a 4-level signal bar, frequency, band (2.4 / 5 / 6 GHz), channel (computed from frequency), and security parsed from the capabilities string into `OPEN, WEP, WPA, WPA2, WPA3, WPA2_WPA3, UNKNOWN`. Mark the currently connected network via `WifiInfo`.
- UI: RecyclerView with sort (signal, name) and filter (band), a header showing connected network details, a channel overview (simple bar chart of networks per channel using MPAndroidChart), and a "Save scan" action that posts to `/wifi/scans` (also offer auto-save after scan, toggle in settings later).
- Scan history screen listing saved scans with delete (swipe or button) and a detail view.

**Done when:** it builds; on a real device scan results display and a scan can be saved and seen in history.

## PHASE 11: Speed Test and Latency

**Prompt:**

Speed test (all measuring happens on the device):
- Implement in `utils/` a `SpeedTester` that measures download by fetching from a public speed endpoint (default Cloudflare `https://speed.cloudflare.com/__down?bytes=<n>`) using several parallel connections for a fixed duration (about 8 to 10 seconds), computing live Mbps from bytes over elapsed time (ignore a short warm-up). Upload by POSTing generated random data to `https://speed.cloudflare.com/__up` the same way. Report progress callbacks for the live gauge and cancel support. Make endpoints configurable constants with a fallback endpoint if the first fails.
- A custom `SpeedGaugeView` (drawn with Canvas) showing live Mbps, with phases "Testing download", "Testing upload", and a final result summary. Show the connection type (Wi-Fi or mobile) and SSID where available. Run in a background executor, survive rotation via ViewModel, handle no-internet gracefully.
- On completion, run a quick ping to include `ping_ms` and POST to `/speed-tests`.

Latency / ping:
- `LatencyTester`: send 20 probes to a configurable target (default `8.8.8.8`; offer 1.1.1.1 and a custom host). Prefer executing the system `ping` binary and parsing results; if that is unavailable, fall back to timing TCP connects to port 53 or 443. Compute avg, min, max, jitter (mean absolute difference between consecutive RTTs), packet loss percent, sent and received counts. Show live results, a small line chart of per-probe RTT and a plain rating label (excellent/good/fair/poor using the same thresholds as the backend). POST to `/latency-tests`.

Screens: a "Tests" tab with two sub-tabs (Speed, Ping), each with a Start button and last result; history lists with delete (speed) and detail; a stats screen for each using `/speed-tests/stats` and `/latency-tests/stats` with MPAndroidChart line charts of trends and summary cards (average, best, worst).

**Done when:** it builds; a real speed test and ping run end to end and appear in history and stats.

## PHASE 12: Signal Strength Map

**Prompt:**

- Maps list screen (create map with name, optional description; delete) using `/signal-maps`.
- Map detail screen: a "Record point" flow. The user enters or picks a label ("Living room", "Bedroom door"...), optionally taps a position on a simple grid or floor canvas (stores normalised x,y), the app reads the current RSSI from `WifiManager.getConnectionInfo().getRssi()` (and the SSID), shows it live before saving, then posts to `/signal-maps/{id}/points`. Deleting a point is supported.
- Visual: a custom `SignalHeatView` canvas that draws recorded points on the grid as colour-coded blobs (green strong, amber fair, red weak) with soft radial gradients blending between them, labels, and a legend with dBm thresholds matching the backend (-60 and -75). Also a list view of points sorted by strength.
- Summary panel from `/signal-maps/{id}/summary`: average, strongest and weakest spots, zone counts, and the verdict sentence.
- If a point has no position, show it in the list only and note that in the UI.

**Done when:** it builds; a map can be created, points recorded and visualised, and the summary displays correctly.

## PHASE 13: Data Usage and Security

**Prompt:**

Data usage:
- Use `NetworkStatsManager` to read Wi-Fi and mobile rx/tx bytes for today, the last 7 days and the last 30 days (query per day buckets).
- Usage Access permission: check via `AppOpsManager`; if not granted, show an explanation screen with a button opening `Settings.ACTION_USAGE_ACCESS_SETTINGS`, and recheck on resume. Handle the denied state without crashing.
- Sync the daily snapshots to `/data-usage` (upsert, so repeated syncs are safe).
- Screen: summary cards (today, week, month), a Daily / Weekly / Monthly toggle driving `/data-usage/summary`, a stacked bar chart (Wi-Fi vs mobile) with MPAndroidChart, and the peak day and average per day. Format bytes into human-readable sizes.

Security:
- Security check screen: take the currently connected network (SSID, BSSID, security type found by matching the connected network in a fresh scan result's capabilities, signal), local IP and gateway IP from `ConnectivityManager`/`LinkProperties`, and the list of any other nearby information available. Be honest in the UI: on Android 10+ other devices' MAC addresses are not accessible, so "connected devices" shows only what is available (this device and the gateway) with a short explanatory note. Do not implement port scanning.
- POST to `/security-scans`, then show the result: a risk badge, the security score, issues with severity colours, and recommendations. Also a history list and detail screen.
- Also show a quick "Nearby networks risk" notice if the last Wi-Fi scan contains open networks.

**Done when:** it builds; usage appears after granting Usage Access, and a security scan returns issues and recommendations from the backend.

## PHASE 14: AI Analysis and Reports

**Prompt:**

Analysis:
- "Analyze my network" screen: day-range selector (7, 14, 30), an Analyze button calling `POST /analysis`, an animated loading state, and the result: health score ring, summary text, issues grouped by severity (colour-coded with category icons), prioritised recommendations as expandable cards, and a badge showing "AI-powered" or "Rule-based" based on `source`. Handle the `no_data` error with a friendly message and shortcuts to run a speed test, ping or scan. Include a past analyses list and detail view.

Reports:
- Reports list with a "Generate report" dialog (title optional, date range via Material date range picker with quick presets: last 7 days, 30 days). `POST /reports`, then a detail screen with a section per category (speed, latency, signal, data usage, security, issues, recommendations) and charts or tables where natural.
- PDF: from the detail screen, download `GET /reports/{id}/export` with the auth header into the app's cache or Downloads using `MediaStore` (scoped storage, no legacy storage permission) and offer **Open** and **Share** via a `FileProvider` and an intent. Show progress and handle errors.
- Delete report with confirmation.

Make these reachable from the "More" tab, and add an "Analyze" shortcut on the dashboard.

**Done when:** it builds; analysis and report generation work end to end and a PDF opens or shares.

## PHASE 15: Polish and Documentation

**Prompt:**

Android:
- Profile and Settings screen: show name and email, edit profile, change password, logout (revokes the refresh token on the server and clears the local session), delete account with a strong confirmation, a dark mode choice (system, light, dark), auto-save-scan toggle, ping target choice, and server URL override with a "Test connection" button hitting `/health`.
- Consistent loading, empty and error states everywhere, offline detection with a banner, and no crashes on rotation or process death in key screens.
- Review all screens for consistent spacing, typography, icons, content descriptions and sensible touch targets. Add the final app icon and app name "Smart Network Analyzer".
- Remove unused code and resources, fix lint errors that are real problems, ensure `gradlew assembleDebug` passes cleanly.

Documentation:
- Root `README.md` in the main folder: project overview, the feature list mapped to the 8 scope areas, architecture (device collects, backend analyses), folder layout, how to run the backend, how to run the Android app on an emulator and on a real device (base URL steps, firewall), how to set the Groq key, the demo account, known limitations (Android Wi-Fi scan throttling, no MAC address access on Android 10+, Usage Access needs manual grant, results vary by device/OS/permissions), and the scope limitations from the product description.
- Update `PROGRESS.md` with a final summary and any open issues.
- Do a final full backend test run and a clean Android build and record the results in `PROGRESS.md`.

**Done when:** everything builds and runs, the README lets a new developer set up both parts, and `PROGRESS.md` lists the final state and any known gaps.

---

## Final Handoff

When all phases are complete, output a short summary containing: the exact commands to start the backend, how to open and run the Android project, the demo login, and the list of anything you could not verify automatically (for example features needing a physical device) so the developer can test those manually.
