# Aegis Mesh — Backend Dispatch Integration Changelog

Summary of changes made to wire the emergency dispatch flow (Android → FastAPI backend)
so that the victim's medical profile is sent with each SOS, and the AI first-aid
instructions + hospital routing decision come back and reach `SOSService`.

---

## 1. `models/Emergency.java`

**Changed:** `toBackendJsonString()`

- **Before:** `public String toBackendJsonString() throws JSONException` — sent
  `emergencyId`, `userId`, `type`, `latitude`, `longitude`, `timestamp`, `trigger`,
  with coordinates/timestamp coerced to `String`.
- **After:** `public String toBackendJsonString(User victim) throws JSONException`

  Builds a JSON payload matching the backend schema:
  ```json
  {
    "victim_name": "...",
    "condition": "...",
    "latitude": ...,
    "longitude": ...,
    "profile": { ... }
  }
  ```
  - `victim_name` ← `victim.getFullName()`
  - `condition` ← `emergencyType`
  - `latitude` / `longitude` ← raw `double` (no more string coercion)
  - `profile` ← nested object from `victim.toProfileJsonObject()`

- All other methods (`toJsonString()`, `fromJsonString()`, constructors, getters/setters)
  are untouched.

---

## 2. `models/User.java`

**Changed:** `toProfileJsonObject()`

- Null-safety fix: `age`, `allergies`, and `chronicConditions` are coalesced to `""`
  before `JSONObject.put()`. `org.json.JSONObject.put()` silently **omits** a key
  when given `null`, which would otherwise let an incomplete profile (e.g. a user
  who triggers SOS before finishing onboarding) send a payload missing fields the
  backend may require.

---

## 3. `models/DispatchResult.java` — **new file**

Small `Serializable` model returned by `ApiClient.sendEmergency()`, carrying the
backend's dispatch response:

| Field                    | Type     |
|---------------------------|----------|
| `aiFirstAidInstructions`  | `String` |
| `recommendedHospital`     | `Hospital` |

Standard constructor + getters/setters, same style as `Emergency`/`Hospital`.

---

## 4. `network/ApiClient.java`

- **Fixed missing imports** that would have blocked compilation: `User`, `Hospital`,
  `DispatchResult`.
- **Changed:** `sendEmergency(Emergency, User)` return type: `void` → `DispatchResult`.
  Previously the AI first-aid instructions and recommended hospital were parsed from
  the backend response and only logged, then discarded. Now they're packaged into a
  `DispatchResult` and returned to the caller.
- Everything else (endpoint construction, retry-free single-attempt HTTP logic,
  error handling) is unchanged.

---

## 5. `services/SOSService.java`

- **New imports:** `com.aegismesh.models.User`, `com.aegismesh.models.DispatchResult`,
  `com.aegismesh.session.UserSession` *(assumed package/class name — confirm against
  your actual singleton)*.
- **`processSos(...)`:** now resolves the logged-in victim via
  `UserSession.getInstance().getCurrentUser()` and passes it to `sendViaInternet()`.
- **`sendViaInternet(Emergency, User)`:** signature gained a `victim` parameter.
  - If `victim` is `null` (session cache empty), logs an error, persists the
    emergency locally, and falls back to mesh delivery instead of attempting a
    backend call that can't succeed.
  - Captures the `DispatchResult` from `ApiClient.sendEmergency(...)`, logs the AI
    first-aid instructions, and shows the routed hospital's name in the ongoing
    notification (falls back to "nearest facility" if unavailable).
  - Left a `TODO` marking where `dispatchResult` should eventually be passed to
    `EmergencyActivity` for on-screen display once that UI exists.
- **`triggerImmediateResend()`:** also resolves the current `User` before resending
  queued alerts; skips the resend pass entirely (leaving alerts queued) if no
  session is cached, rather than resending with an incomplete payload. Captures and
  logs the `DispatchResult` per resent alert.

---

## 6. `models/Hospital.java` — no changes

Already compatible: `getName()`, `getDistance()`, and `fromBackendJson()` match
everything `ApiClient` and `SOSService` call.

**Note (not yet acted on):** `inventory` is parsed in `fromBackendJson()` but has no
getter, so it isn't accessible anywhere downstream. Worth adding `getInventory()` if
the app is meant to surface hospital supply availability during triage.

---

## Open items / assumptions to verify

1. **`UserSession`** — assumed `com.aegismesh.session.UserSession` with a static
   `getInstance().getCurrentUser()` method. Rename in `SOSService.java` if your
   actual singleton differs.
2. **`Hospital.getInventory()`** — not yet added; flagged above.
3. **`dispatchResult` → UI** — currently only reaches a log line and the
   notification text. Wiring it into `EmergencyActivity` (or wherever first-aid
   instructions should be displayed to the victim) is still open.