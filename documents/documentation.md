
Base URL (local): `http://localhost:7070`. All HTTP endpoints are under `/api`. The WebSocket endpoint is `/ws/notifications`.

## Conventions

### Authentication

Everything except `/api/v1/auth/*` requires a token:

```
Authorization: Bearer <token>
```

The token comes from `POST /api/v1/auth/login`. It expires after 24 hours. The WebSocket takes the token as a query parameter instead (see [WebSocket](#websocket-live-notifications)).

### Data ownership

Every resource below belongs to the logged-in user. You only ever see and change your own data. Another user's data answers `404`, not `403`, so its existence is not revealed.

**Exception: devlogs are shared.** Every logged-in user can read every devlog. Only admins can create, update or delete them.

### Errors

Almost all errors have this format:

```json
{ "status": 404, "error": "Memory not found with ID: 5" }
```

Some validation errors on the two `/api/v1/auth/*` endpoints return `{ "error": "..." }` without `status`.

| Status | Meaning |
|---|---|
| 400 | Invalid input (blank title, bad id, validation failed) |
| 401 | Missing, invalid or expired token (or wrong login) |
| 403 | Authenticated but not allowed (admin-only endpoint) |
| 404 | Not found, or belongs to another user |
| 405 | Operation not supported for this resource |
| 409 | Conflict (the email is already in use) |
| 429 | Too many requests (AI prompts) |
| 500 | Unexpected server error |
| 502 / 503 / 504 | The AI service failed, is not configured, or timed out |

### Standard CRUD endpoints

Most resources share the same five endpoints. `{resource}` is the path from the table below.

| Method | URL | Request body | Success | Errors |
|---|---|---|---|---|
| POST | `/api/v1/{resource}` | `<Resource>RequestDTO` | `201` + response DTO | 400 |
| GET | `/api/v1/{resource}` | | `200` + list of response DTOs (only yours) | |
| GET | `/api/v1/{resource}/{id}` | | `200` + response DTO | 400, 404 |
| PUT | `/api/v1/{resource}/{id}` | `<Resource>RequestDTO` | `200` + updated response DTO | 400, 404 |
| DELETE | `/api/v1/{resource}/{id}` | | `200` + `{ "message": "Deleted successfully" }` | 400, 404 |
| GET | `/api/v1/{resource}/random` | | `200` + one random response DTO of yours | 404 if you have none |

Not every resource has every endpoint:

| Resource | Path | Notes |
|---|---|---|
| Chapters | `/api/v1/chapters` | |
| Stories | `/api/v1/stories` | |
| Memories | `/api/v1/memories` | |
| Artifacts | `/api/v1/artifacts` | |
| People | `/api/v1/persons` | |
| Places | `/api/v1/places` | |
| Lists | `/api/v1/entity-lists` | |
| Fragments | `/api/v1/fragments` | no `random` |
| Devlogs | `/api/v1/devlogs` | no `random`. Read: all users. Write: **admin only** |
| Notifications | `/api/v1/notifications` | no `random` |
| Time capsules | `/api/v1/time-capsules` | no `PUT` (405), no `random` (405) |
| AI prompts | `/api/v1/gemini-prompts` | no `PUT` (405), no `random` (405) |

---

## Auth

| Method | URL | Request body | Success | Errors |
|---|---|---|---|---|
| POST | `/api/v1/auth/register` | `RegisterRequestDTO` | `201` + `UserResponseDTO` | 400 invalid input, 409 email already in use |
| POST | `/api/v1/auth/login` | `LoginRequestDTO` | `200` + `LoginResponseDTO` | 401 wrong email or password |

Emails are trimmed and lower-cased. Passwords need at least 8 characters, one digit and one special character.

**RegisterRequestDTO**

```json
{ "name": "Anna", "email": "anna@example.dk", "password": "Secret123!", "passwordCheck": "Secret123!" }
```

**LoginRequestDTO**

```json
{ "email": "anna@example.dk", "password": "Secret123!" }
```
**LoginResponseDTO**

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

---

## Users

| Method | URL | Access | Request body | Success | Errors |
|---|---|---|---|---|---|
| GET | `/api/v1/users/current` | Any user | | `200` + `UserResponseDTO` | 401 |
| GET | `/api/v1/users` | **Admin only** | | `200` + list of `UserResponseDTO` | 403 |
| GET | `/api/v1/users/{id}` | **Admin only** | | `200` + `UserResponseDTO` | 403, 404 |
| PUT | `/api/v1/users/{id}` | Your own account | `UserUpdateDTO` | `200` + `UserResponseDTO` | 400 (also when `{id}` is not you), 404, 409 email already in use |
| DELETE | `/api/v1/users/{id}` | Your own account | | `204` | 400 (also when `{id}` is not you), 404 |

**UserResponseDTO**

```json
{ "id": 1, "name": "Anna", "email": "anna@example.dk", "role": "USER" }
```

**UserUpdateDTO** (both fields optional; blank values are ignored)

```json
{ "name": "Anna", "email": "anna@example.dk" }
```

The password hash is never returned. Nobody can become admin through the API. Promote the user directly in the database.

---
## Pages

Fields marked **required** are validated by the controller (`400` if blank). `visibility` is `PRIVATE` or `PUBLIC`. `PUBLIC` is stored but not used yet. `createdAt` and `updatedAt` are set by the server (dates, `yyyy-MM-dd`). Titles do not have to be unique.

### Memories: `/api/v1/memories`

Request: `title` (**required**), `subtitle` (**required**), `content`, `date`, `visibility`

Response: `id`, `title`, `subtitle`, `content`, `date`, `createdAt`, `updatedAt`, `visibility`

### Chapters: `/api/v1/chapters` and Stories: `/api/v1/stories`

Request: `title` (**required**), `subtitle` (**required**), `content`, `startDate`, `endDate`, `visibility`. `endDate` must not be before `startDate`.

Response: `id`, `title`, `subtitle`, `content`, `startDate`, `endDate`, `createdAt`, `updatedAt`, `visibility`

### Artifacts: `/api/v1/artifacts`

Request: `title` (**required**), `subtitle` (**required**), `content`, `visibility`, `artifactType`

Response: `id`, `title`, `subtitle`, `content`, `createdAt`, `updatedAt`, `visibility`, `artifactType`

`artifactType` is a free-text category, e.g. `{ "name": "Clothing" }`. It is created for you the first time and reused afterwards. Matching ignores case and surrounding spaces. Categories are private to each user.

| Method | URL | Success |
|---|---|---|
| GET | `/api/v1/artifact-types` | `200` + list of `{ "id": 1, "name": "Clothing" }` (your own categories) |

### People: `/api/v1/persons`

Request: `name` (**required**), `subtitle`, `content`, `relation`, `visibility`

Response: `id`, `name`, `subtitle`, `content`, `relation`, `visibility`, `createdAt`, `updatedAt`

### Places: `/api/v1/places`

Request: `name` (**required**), `subtitle`, `content`, `latitude`, `longitude`, `address`, `city`, `country`, `visibility`

Response: the same fields plus `id`, `createdAt`, `updatedAt`

### Lists: `/api/v1/entity-lists`

Request: `title` (**required**), `subtitle`, `visibility`, `items` (list of strings)

```json
{ "title": "Books to read", "subtitle": "2026", "visibility": "PRIVATE", "items": ["Dune", "Emma"] }
```
Response: `id`, `title`, `subtitle`, `createdAt`, `updatedAt`, `itemAmount`, `visibility`, `items` (list of `{ "id", "text" }`)

### Fragments: `/api/v1/fragments`

Request: `title`, `subtitle`, `content` (**required**)

Response: `id`, `title`, `subtitle`, `content`, `createdAt`, `updatedAt`, `wordCount`

### Time capsules: `/api/v1/time-capsules`

Request: `title` (**required**), `subtitle`, `content`, `unlockDate` (**required**, must be in the future)

Response: `id`, `title`, `subtitle`, `content`, `unlockDate`, `lockStatus`

While `unlockDate` is in the future the response has `lockStatus: true` and `content: null`. A time capsule cannot be edited after it is created. A background job checks every minute for capsules that have reached their unlock date. The owner then receives one `TIME_CAPSULE` notification (see below).

---

## Devlogs (changelog)

Devlogs are announcements written by admins, for example when a new feature is released. **Everyone can read them. Only admins can write them.**

| Method | URL | Access | Request body | Success | Errors |
|---|---|---|---|---|---|
| GET | `/api/v1/devlogs` | Any user | | `200` + list of **all** devlogs | |
| GET | `/api/v1/devlogs/{id}` | Any user | | `200` + `DevlogResponseDTO` | 400, 404 |
| POST | `/api/v1/devlogs` | **Admin only** | `DevlogRequestDTO` | `201` + `DevlogResponseDTO` | 400, 403 |
| PUT | `/api/v1/devlogs/{id}` | **Admin only** | `DevlogRequestDTO` | `200` + `DevlogResponseDTO` | 400, 403, 404 |
| DELETE | `/api/v1/devlogs/{id}` | **Admin only** | | `200` + `{ "message": "Deleted successfully" }` | 400, 403, 404 |

Request: `title`, `subtitle`, `content` (all **required**). The creation date is set by the server.

Response: `id`, `title`, `subtitle`, `content`, `createdAt`

**When a devlog is created, every user receives a `DEVLOG` notification.** The notifications are created on a background thread, so the admin gets the `201` response immediately.

---

## Notifications

| Method | URL | Request body | Success | Errors |
|---|---|---|---|---|
| GET | `/api/v1/notifications` | | `200` + list of your notifications | |
| GET | `/api/v1/notifications/{id}` | | `200` + `NotificationResponseDTO` | 400, 404 |
| POST | `/api/v1/notifications` | `NotificationRequestDTO` | `201` + `NotificationResponseDTO` | 400 |
| PUT | `/api/v1/notifications/{id}` | `NotificationRequestDTO` | `200` + `NotificationResponseDTO` | 400, 404 |
| DELETE | `/api/v1/notifications/{id}` | | `200` + `{ "message": "Deleted successfully" }` | 400, 404 |

Request: `title` (**required**), `notificationType` (**required**), `targetId`, `targetType`, `read`, `createdAt`

Response: `id`, `title`, `notificationType`, `targetId`, `targetType`, `read`, `createdAt` (date and time)

`notificationType` is one of `DEVLOG`, `FRIEND_REQUEST`, `MENTION`, `TIME_CAPSULE`, `SYSTEM`. The system creates these automatically:

| Type | Created when | `targetType` / `targetId` |
|---|---|---|
| `DEVLOG` | An admin creates a devlog (one per user) | `"DEVLOG"` / the devlog id |
| `TIME_CAPSULE` | A time capsule reaches its unlock date (once per capsule) | `"TIME_CAPSULE"` / the capsule id |

---

## WebSocket: live notifications

Instead of polling `GET /api/v1/notifications`, a client can keep a WebSocket open and receive each notification the moment it is created.

```
ws://localhost:7070/ws/notifications?token=<JWT>
```

| | |
|---|---|
| Authentication | The JWT from `/api/v1/auth/login`, passed as the `token` query parameter (browsers cannot set an `Authorization` header on a WebSocket) |
| Messages from the server | One JSON text message per notification, in the `NotificationResponseDTO` format |
| Messages from the client | None are needed (anything sent is ignored) |
| Heartbeat | The server sends a ping every 20 seconds. Browsers answer automatically. Without traffic the connection would be closed after about 30 seconds |
| Several tabs | Every open connection of a user receives the notification |

If the token is missing, invalid or expired, or the user no longer exists, the server closes the connection with code `4401` (`Missing token` / `Invalid token`).

Example in a browser console:

```js
const ws = new WebSocket("ws://localhost:7070/ws/notifications?token=" + token);
ws.onopen = () => console.log("connected");
ws.onclose = e => console.log("closed", e.code, e.reason);
ws.onmessage = e => console.log("notification:", JSON.parse(e.data));
```

The notification is always saved first, so a client that was offline can fetch what it missed with `GET /api/v1/notifications`.

---

## Mentions (links between pages)

A mention says: *the text between `startIndex` and `endIndex` in page A links to page B.*

| Method | URL | Request body | Success | Errors |
|---|---|---|---|---|
| POST | `/api/v1/mentions` | `MentionRequestDTO` | `201` + `MentionResponseDTO` | 400, 404 if either page is missing or not yours |
| GET | `/api/v1/mentions/owner/{ownerType}/{ownerId}` | | `200` + list: links that start from a page | 400 |
| GET | `/api/v1/mentions/target/{targetType}/{targetId}` | | `200` + list: backlinks, what links **to** a page | 400 |
| DELETE | `/api/v1/mentions/{id}` | | `204` | 400, 404 |

`ownerType` and `targetType` are one of `CHAPTER`, `STORY`, `MEMORY`, `ARTIFACT`, `PERSON`, `PLACE`. Deleting a page also deletes its mentions.

**MentionRequestDTO**

```json
{
  "ownerId": 5, "ownerType": "MEMORY",
  "startIndex": 10, "endIndex": 17, "selectedText": "Grandma",
  "targetId": 2, "targetType": "PERSON"
}
```

All fields are required, ids must be positive and `startIndex >= 0` and `< endIndex`. `MentionResponseDTO` has the same fields plus `id`.

---

## AI prompts (Google Gemini)

| Method | URL | Request body | Success | Errors |
|---|---|---|---|---|
| POST | `/api/v1/gemini-prompts/generate` | `GeminiPromptRequestDTO` | `200` + `{ "promptText": "..." }` | 400, 429, 502, 503, 504 |
| POST | `/api/v1/gemini-prompts` | `GeminiPromptSaveDTO` | `201` + `GeminiPromptResponseDTO` | 400 |
| GET | `/api/v1/gemini-prompts` | | `200` + list | |
| GET | `/api/v1/gemini-prompts/{id}` | | `200` + `GeminiPromptResponseDTO` | 404 |
| DELETE | `/api/v1/gemini-prompts/{id}` | | `200` + `{ "message": "Deleted successfully" }` | 404 |

`generate` does **not** save anything. It sends the prompt to Gemini and returns the text. The saved history is a separate call (`POST /api/v1/gemini-prompts`).

Limits: `prompt` must not be blank and at most 500 characters. Each user may call `generate` 10 times per hour (`429` afterwards). `503` means the server has no Gemini key configured.

```json
// GeminiPromptRequestDTO
{ "prompt": "Write a journaling prompt about my first job" }
// GeminiPromptSaveDTO
{ "content": "What did your first day at work feel like?" }
// GeminiPromptResponseDTO
{ "id": 3, "content": "What did your first day at work feel like?" }
```



