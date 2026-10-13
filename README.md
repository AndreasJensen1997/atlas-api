# Atlas

**Atlas** is a personal memory and life-organisation application. It helps users collect, connect and explore the important moments of their lives, like a small, private encyclopedia of their own life.

Instead of treating memories as isolated entries, Atlas is built around the idea that a life is made up of **people, places, events and experiences that are connected to one another**. A user writes pages (memories, chapters, stories, people, places, objects), highlights text in a page and links it to another page. Every page can then show what links to it (backlinks).

This repository contains the **backend API**.

---

## Status

Atlas is under active development. The API is feature-complete for the core idea; some of the originally planned features are still future work.

|Area|Status|
|---|---|
|Registration, login (JWT), roles (`USER`, `ADMIN`)|Done|
|Chapters, stories, memories, fragments|Done|
|People, places|Done|
|Artifacts (physical objects) with user-defined categories|Done|
|Lists|Done|
|Time capsules (content hidden until the unlock date)|Done|
|Mentions: highlight text and link to another page, plus backlinks|Done|
|Notifications, devlog|Done|
|AI journaling prompts (Google Gemini), rate limited|Done|
|Search across all pages|Planned|
|Timeline|Planned|
|Images on pages|Planned|
|Friends and sharing (`Visibility.PUBLIC` is stored but not used yet)|Planned|
|Themes and language support|Planned (frontend)|

---

## Technologies

**Backend**

- Java 25, Maven
- [Javalin 6](https://javalin.io/) (HTTP server and routing)
- Hibernate 7 / JPA with HikariCP
- PostgreSQL
- JWT (HS256) with Nimbus JOSE, passwords hashed with BCrypt
- Jackson (JSON), Lombok, SLF4J + Logback

**Tests**

- JUnit, Hamcrest, REST Assured
- Testcontainers (a real PostgreSQL 16 in Docker), so **Docker must be running** when you run the tests

**External services**

- Google Gemini API (optional, only for `POST /api/gemini-prompts/generate`)

---

## Architecture

A request passes through these layers:

```
Client -> SecurityFilter (JWT, role) -> Controller -> Service -> DAO -> PostgreSQL                                           |            |                                        DTOs        Mapper (DTO <-> entity)
```

- **Controllers** parse and validate the request and choose the HTTP status. Most extend `AbstractController`, which implements the shared CRUD flow.
- **Services** contain the rules: ownership checks, unique titles, category lookup, rate limiting.
- **DAOs** talk to the database. `GenericDAO` gives every entity create/read/update/delete. `UserOwnedDAO` adds queries filtered by user.
- **Mappers and DTOs** keep the database entities out of the API.
- **Errors** are thrown as `ApiException` and turned into JSON by the global handlers in `ApplicationConfig`.

Diagrams (layers, request flow, data model) are in `diagrams.md`.



## API overview

All endpoints are under `/api`. Everything except `/api/auth/*` requires the header

```
Authorization: Bearer <token>
```

### Authentication

|Method|Path|Description|
|---|---|---|
|POST|`/api/auth/register`|Create an account. Body: `name`, `email`, `password`, `passwordCheck`. Returns `201`|
|POST|`/api/auth/login`|Body: `email`, `password`. Returns `{ "token": "..." }`|

Emails are trimmed and lower-cased, so `Anna@Example.dk` and `anna@example.dk` are the same account. Passwords need at least 8 characters, one digit and one special character.

### Users

|Method|Path|Access|
|---|---|---|
|GET|`/api/users/current`|The logged-in user|
|PUT|`/api/users/{id}`|Your own account only (name, email)|
|DELETE|`/api/users/{id}`|Your own account only|
|GET|`/api/users`, `/api/users/{id}`|**Admin only**|

### Pages and other resources

Every resource below belongs to the logged-in user. You only ever see and change your own data.

|Resource|Path|Operations|
|---|---|---|
|Chapters|`/api/chapters`|list, `random`, get, create, update, delete|
|Stories|`/api/stories`|list, `random`, get, create, update, delete|
|Memories|`/api/memories`|list, `random`, get, create, update, delete|
|Artifacts|`/api/artifacts`|list, `random`, get, create, update, delete|
|People|`/api/persons`|list, `random`, get, create, update, delete|
|Places|`/api/places`|list, `random`, get, create, update, delete|
|Lists|`/api/entity-lists`|list, `random`, get, create, update, delete|
|Fragments|`/api/fragments`|list, get, create, update, delete|
|Devlogs|`/api/devlogs`|list, get, create, update, delete|
|Notifications|`/api/notifications`|list, get, create, update, delete|
|Time capsules|`/api/time-capsules`|list, get, create, delete (no update)|
|Artifact types|`/api/artifact-types`|list|
|AI prompts|`/api/gemini-prompts`|list, get, save, delete, and `POST /generate`|

Typical calls: `GET /api/memories` (list), `GET /api/memories/random`, `GET /api/memories/{id}`, `POST /api/memories`, `PUT /api/memories/{id}`, `DELETE /api/memories/{id}`.

### Mentions (links between pages)

A mention says: _the text between `startIndex` and `endIndex` in page A links to page B._

|Method|Path|Description|
|---|---|---|
|POST|`/api/mentions`|Create a mention|
|GET|`/api/mentions/owner/{ownerType}/{ownerId}`|Links that start from a page|
|GET|`/api/mentions/target/{targetType}/{targetId}`|Backlinks: what links **to** a page|
|DELETE|`/api/mentions/{id}`|Delete a mention|

`ownerType` and `targetType` are one of `CHAPTER`, `STORY`, `MEMORY`, `ARTIFACT`, `PERSON`, `PLACE`. **Both pages must belong to you**, and `startIndex` must be smaller than `endIndex`. Deleting a page also deletes its mentions.

### Things worth knowing

- **Artifact categories are free text.** When you create or update an artifact you send a category name (e.g. "Clothing"). It is created for you the first time, and reused afterwards. Matching ignores case and surrounding spaces, and categories are private to each user.
- **Time capsules are sealed.** While `unlockDate` is in the future the API returns `content: null` and `lockStatus: true`. The capsule opens on the unlock date itself.
- **AI prompts.** `POST /api/gemini-prompts/generate` sends a prompt (max 500 characters) to Gemini and returns the generated text. Each user may make 10 requests per hour. Upstream problems are reported as `429`, `502`, `503` or `504`. Saving a generated prompt is a separate call.

### Errors

Errors are JSON:

```
{ "status": 404, "error": "Memory not found with ID: 5" }
```

|Status|Meaning|
|---|---|
|400|Invalid input|
|401|Missing, invalid or expired token|
|403|Authenticated, but not allowed (e.g. admin-only endpoint)|
|404|Not found. Other users' data also answers `404`, so its existence is not revealed|
|409|Conflict, e.g. the email is already in use|
|429|Too many requests|
|502 / 503 / 504|The AI service failed, is not configured, or timed out|

---

### Project structure

```
src/main/java/app  config/        Javalin setup, Hibernate setup, dependency wiring  controllers/   HTTP endpoints (+ generics/AbstractController)  services/      Business rules  daos/          Database access (+ generics/GenericDAO, UserOwnedDAO)  entities/      JPA entities  dtos/          Request and response objects  mappers/       Entity <-> DTO  enums/         Role, Visibility, TargetType, NotificationType, Relation  exceptions/    ApiException  utils/         Security (JWT, filter, rate limiter), validation, helperssrc/test/java/app  daos/          Database tests per entity  endpoints/     End-to-end HTTP tests per resource (REST Assured)  testUtils/     TestPopulator (seed data)
```

---

## Security

- Passwords are hashed with BCrypt. The hash is never returned.
- Tokens are signed JWTs (HS256) that expire after 24 hours. The token only carries the **user id**. Roles are read from the database on every request, so a role change takes effect immediately.
- The user id always comes from the token, never from the request body.
- Every query on user data is filtered by the owner. Another user's data answers `404`.
- Admin-only endpoints are declared with `Role.ADMIN` on the route and checked in one place.
- Secrets live in `config.properties` (ignored by git) or environment variables.

---

## Known limitations

- No search, timeline, images or sharing yet (see Status).
- No pagination on list endpoints.
- `createdAt` and `updatedAt` are dates without a time.
- The AI rate limit is kept in memory: it resets when the server restarts and works for a single server only.
- A time capsule opens at midnight in the server's time zone.
- Titles must be unique per user, and a duplicate answers `400` rather than `409`.
- The auth endpoints return errors as `{ "error": "..." }` without the `status` field.
- The schema is managed by Hibernate (`update`), not by migrations.