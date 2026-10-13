
# Atlas API documentation

Base URL (local): `http://localhost:7070`. All endpoints are under `/api`.

## Conventions

### Authentication

Everything except `/api/auth/*` requires a token:

```
Authorization: Bearer <token>
```

The token comes from `POST /api/auth/login`. It expires after 24 hours.

### Data ownership

Every resource below belongs to the logged-in user. You only ever see and change your own data. Another user's data answers `404`, not `403`, so its existence is not revealed.

### Errors

Almost all errors have this format:

```
{ "status": 404, "error": "Memory not found with ID: 5" }
```

The two `/api/auth/*` endpoints return `{ "error": "..." }` without `status`.

|Status|Meaning|
|---|---|
|400|Invalid input (blank title, bad id, validation failed)|
|401|Missing, invalid or expired token (or wrong login)|
|403|Authenticated but not allowed (admin-only endpoint)|
|404|Not found, or belongs to another user|
|405|Operation not supported for this resource|
|429|Too many requests (AI prompts)|
|500|Unexpected server error|
|502 / 503 / 504|The AI service failed, is not configured, or timed out|

### Standard CRUD endpoints

Most resources share the same five endpoints. `{resource}` is the path from the table below.

|Method|URL|Request body|Success|Errors|
|---|---|---|---|---|
|POST|`/api/{resource}`|`<Resource>RequestDTO`|`201` + response DTO|400|
|GET|`/api/{resource}`||`200` + list of response DTOs (only yours)||
|GET|`/api/{resource}/{id}`||`200` + response DTO|400, 404|
|PUT|`/api/{resource}/{id}`|`<Resource>RequestDTO`|`200` + updated response DTO|400, 404|
|DELETE|`/api/{resource}/{id}`||`200` + `{ "message": "Deleted successfully" }`|400, 404|
|GET|`/api/{resource}/random`||`200` + one random response DTO of yours|404 if you have none (see note)|

**Note:** `random` with no saved pages is not covered by a test yet. Check what the service returns before documenting a status. Not every resource has every endpoint. See the table below.

|Resource|Path|Not available|
|---|---|---|
|Chapters|`/api/chapters`||
|Stories|`/api/stories`||
|Memories|`/api/memories`||
|Artifacts|`/api/artifacts`||
|People|`/api/persons`||
|Places|`/api/places`||
|Lists|`/api/entity-lists`||
|Fragments|`/api/fragments`|`random`|
|Devlogs|`/api/devlogs`|`random`|
|Notifications|`/api/notifications`|`random`|
|Time capsules|`/api/time-capsules`|`PUT` (405), `random` (405)|
|AI prompts|`/api/gemini-prompts`|`PUT` (405), `random` (405)|

---

## Auth

|Method|URL|Request body|Success|Errors|
|---|---|---|---|---|
|POST|`/api/auth/register`|`RegisterRequestDTO`|`201` + `UserResponseDTO`|400 invalid input or email already in use|
|POST|`/api/auth/login`|`LoginRequestDTO`|`200` + `LoginResponseDTO`|401 wrong email or password|

Emails are trimmed and lower-cased. Passwords need at least 8 characters, one digit and one special character.

**RegisterRequestDTO**

```
{ "name": "Anna", "email": "anna@example.dk", "password": "Secret123!", "passwordCheck": "Secret123!" }
```

**LoginRequestDTO**

```
{ "email": "anna@example.dk", "password": "Secret123!" }
```

**LoginResponseDTO**

```
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

---

## Users

|Method|URL|Access|Request body|Success|Errors|
|---|---|---|---|---|---|
|GET|`/api/users/current`|Any user||`200` + `UserResponseDTO`|401|
|GET|`/api/users`|**Admin only**||`200` + list of `UserResponseDTO`|403|
|GET|`/api/users/{id}`|**Admin only**||`200` + `UserResponseDTO`|403, 404|
|PUT|`/api/users/{id}`|Your own account|`UserUpdateDTO`|`200` + `UserResponseDTO`|400 (also when `{id}` is not you), 404, 409 email already in use|
|DELETE|`/api/users/{id}`|Your own account||`204`|400 (also when `{id}` is not you), 404|

**UserResponseDTO**

```
{ "id": 1, "name": "Anna", "email": "anna@example.dk", "role": "USER" }
```

**UserUpdateDTO**

```
{ "name": "Anna", "email": "anna@example.dk" }
```

The password hash is never returned. Nobody can become admin through the API. Promote the user directly in the database.

---

## Pages

Fields marked **required** are validated by the controller (`400` if blank). `visibility` is `PRIVATE` or `PUBLIC`. `PUBLIC` is stored but not used yet. `createdAt` and `updatedAt` are set by the server (dates, `yyyy-MM-dd`).

### Memories: `/api/memories`

Request: `title` (**required**), `subtitle` (**required**), `content`, `date`, `visibility`

Response: `id`, `title`, `subtitle`, `content`, `date`, `createdAt`, `updatedAt`, `visibility`

### Chapters: `/api/chapters` and Stories: `/api/stories`

Request: `title` (**required**), `subtitle` (**required**), `content`, `startDate`, `endDate`, `visibility`. `endDate` must not be before `startDate`.

Response: `id`, `title`, `subtitle`, `content`, `startDate`, `endDate`, `createdAt`, `updatedAt`, `visibility`

### Artifacts: `/api/artifacts`

Request: `title` (**required**), `subtitle` (**required**), `content`, `visibility`, `artifactType`

Response: `id`, `title`, `subtitle`, `content`, `createdAt`, `updatedAt`, `visibility`, `artifactType`

`artifactType` is a free-text category, e.g. `{ "name": "Clothing" }`. It is created for you the first time and reused afterwards. Matching ignores case and surrounding spaces. Categories are private to each user.

|Method|URL|Success|
|---|---|---|
|GET|`/api/artifact-types`|`200` + list of `{ "id": 1, "name": "Clothing" }` (your own categories)|

### People: `/api/persons`

Request: `name` (**required**), `content`, `relation`, `visibility`

Response: `id`, `name`, `content`, `relation`, `visibility`, `createdAt`, `updatedAt`

### Places: `/api/places`

Request: `name` (**required**), `content`, `latitude`, `longitude`, `address`, `city`, `country`, `visibility`

Response: the same fields plus `id`, `createdAt`, `updatedAt`

### Lists: `/api/entity-lists`

Request: `title` (**required**), `subtitle`, `visibility`, `items` (list of strings)

```
{ "title": "Books to read", "subtitle": "2026", "visibility": "PRIVATE", "items": ["Dune", "Emma"] }
```

Response: `id`, `title`, `subtitle`, `createdAt`, `updatedAt`, `itemAmount`, `visibility`, `items` (list of `{ "id", "text" }`)

### Fragments: `/api/fragments`

Request: `title`, `subtitle`, `content` (**required**)

Response: `id`, `title`, `subtitle`, `content`, `createdAt`, `updatedAt`, `wordCount`

### Devlogs: `/api/devlogs`

Request: `title`, `subtitle`, `content` (all **required**), `createdAt`

Response: `id`, `title`, `subtitle`, `content`, `createdAt`

### Notifications: `/api/notifications`

Request: `title` (**required**), `notificationType` (**required**), `targetId`, `targetType`, `read`, `createdAt`

Response: `id`, `title`, `notificationType`, `targetId`, `targetType`, `read`, `createdAt` (date and time)

### Time capsules: `/api/time-capsules`

Request: `title` (**required**), `subtitle`, `content`, `unlockDate` (**required**, must be in the future)

Response: `id`, `title`, `subtitle`, `content`, `unlockDate`, `lockStatus`

While `unlockDate` is in the future the response has `lockStatus: true` and `content: null`. A time capsule cannot be edited after it is created.

---

## Mentions (links between pages)

A mention says: _the text between `startIndex` and `endIndex` in page A links to page B._

|Method|URL|Request body|Success|Errors|
|---|---|---|---|---|
|POST|`/api/mentions`|`MentionRequestDTO`|`201` + `MentionResponseDTO`|400, 404 if either page is missing or not yours|
|GET|`/api/mentions/owner/{ownerType}/{ownerId}`||`200` + list: links that start from a page|400|
|GET|`/api/mentions/target/{targetType}/{targetId}`||`200` + list: backlinks, what links **to** a page|400|
|DELETE|`/api/mentions/{id}`||`204`|400, 404|

`ownerType` and `targetType` are one of `CHAPTER`, `STORY`, `MEMORY`, `ARTIFACT`, `PERSON`, `PLACE`. Deleting a page also deletes its mentions.

**MentionRequestDTO**

```
{  "ownerId": 5, "ownerType": "MEMORY",  "startIndex": 10, "endIndex": 17, "selectedText": "Grandma",  "targetId": 2, "targetType": "PERSON"}
```

All fields are required, ids must be positive and `startIndex >= 0` and `< endIndex`. `MentionResponseDTO` has the same fields plus `id`.

---

## AI prompts (Google Gemini)

|Method|URL|Request body|Success|Errors|
|---|---|---|---|---|
|POST|`/api/gemini-prompts/generate`|`GeminiPromptRequestDTO`|`200` + `{ "promptText": "..." }`|400, 429, 502, 503, 504|
|POST|`/api/gemini-prompts`|`GeminiPromptSaveDTO`|`201` + `GeminiPromptResponseDTO`|400|
|GET|`/api/gemini-prompts`||`200` + list||
|GET|`/api/gemini-prompts/{id}`||`200` + `GeminiPromptResponseDTO`|404|
|DELETE|`/api/gemini-prompts/{id}`||`200` + `{ "message": "Deleted successfully" }`|404|

`generate` does **not** save anything. It sends the prompt to Gemini and returns the text. The saved history is a separate call (`POST /api/gemini-prompts`).

Limits: `prompt` must not be blank and at most 500 characters. Each user may call `generate` 10 times per hour (`429` afterwards). `503` means the server has no Gemini key configured.

```
// GeminiPromptRequestDTO{ "prompt": "Write a journaling prompt about my first job" }// GeminiPromptSaveDTO{ "content": "What did your first day at work feel like?" }// GeminiPromptResponseDTO{ "id": 3, "content": "What did your first day at work feel like?" }
```