# Atlas: user stories

Every story below describes something the API can do today. Each has acceptance criteria that can be checked with a request, and a note on where in the code it lives.

**Actors:** a *user* (registered, logged in), an *admin*, and a *visitor* (not logged in).

---

## US-1: Register and log in
**As a** visitor, **I want** to create an account and log in, **so that** I get a private journal that only I can read.

Acceptance criteria
- `POST /api/v1/auth/register` with name, email, password and passwordCheck returns `201`.
- The password needs at least 8 characters, one digit and one special character, and must equal passwordCheck. Otherwise `400`.
- An email that is already registered is rejected. `Anna@Example.dk` and `anna@example.dk` count as the same email.
- `POST /api/v1/auth/login` with correct credentials returns a JWT valid for 24 hours. Wrong credentials return `401`.
- The password is stored as a BCrypt hash and never returned.

Code: `AuthController` → `UserService.registerUser/login` → `UserDAO`, `JWTToken`.

---

## US-2: Write and manage a memory
**As a** user, **I want** to create, read, edit and delete memories, **so that** I can keep the moments I want to remember.

Acceptance criteria
- `POST /api/v1/memories` with title and subtitle returns `201` and the memory with `createdAt`.
- A blank title or subtitle returns `400` and nothing is saved.
- `GET /api/v1/memories` lists only my memories.
- `PUT /api/v1/memories/{id}` changes the memory and updates `updatedAt`.
- `DELETE /api/v1/memories/{id}` removes it. Afterwards `GET` on the same id returns `404`.

Code: `MemoryController` (extends `AbstractController`) → `MemoryService` → `MemoryDAO`. The same flow serves chapters, stories, people, places, lists, fragments and artifacts.

---

## US-3: Keep my data private
**As a** user, **I want** nobody else to be able to see or change my pages, **so that** my journal stays private.

Acceptance criteria
- Every request except register and login without a valid token returns `401`.
- If user B asks for user A's memory by id, the answer is `404`, the same as for an id that does not exist.
- User B cannot edit or delete it either.
- The user id comes from the token, never from the request body.

Code: `SecurityFilter` sets `userId` on the request, and the services pass it to queries filtered by owner (`UserOwnedDAO`).

---

## US-4: Link a highlighted text to another page (mention)
**As a** user, **I want** to highlight text in a page and link it to another page, **so that** my memories, people and places are connected.

Acceptance criteria
- `POST /api/v1/mentions` with the owner page, `startIndex`, `endIndex`, `selectedText` and the target page returns `201`.
- `startIndex` must be at least 0 and less than `endIndex`, otherwise `400`.
- Both pages must exist and belong to me, otherwise `404`. I cannot link to somebody else's page.
- `GET /api/v1/mentions/owner/MEMORY/5` lists the links that start from memory 5.
- Deleting a page also deletes the mentions that point from or to it.

Code: `MentionController` → `MentionService.createMention` (checks both pages with `existsByIdAndUserId`, using a map from `TargetType` to DAO) → `MentionDAO`.

---

## US-5: See what links to a page (backlinks)
**As a** user, **I want** to see which pages mention a person or place, **so that** I can find everything related to them.

Acceptance criteria
- `GET /api/v1/mentions/target/PERSON/2` returns all my mentions that point to person 2.
- Mentions made by other users never appear.

Code: `MentionController.getIncoming` → `MentionService.getIncomingMentions` → `MentionDAO` (query filtered on target and user).

---

## US-6: Categorise my artifacts
**As a** user, **I want** to give each artifact my own category (e.g. "Clothing"), **so that** I can organise my objects my way.

Acceptance criteria
- When I create an artifact I send a category name. If it does not exist for me, it is created. Otherwise it is reused.
- "clothing", " Clothing " and "Clothing" are the same category.
- Another user's categories are separate from mine.
- `GET /api/v1/artifact-types` returns only my categories.

Code: `ArtifactService` (find or create the type by normalised name and user) → `ArtifactTypeDAO`.

---

## US-7: Seal a message in a time capsule
**As a** user, **I want** to write a message that stays hidden until a future date, **so that** I can surprise my future self.

Acceptance criteria
- `POST /api/v1/time-capsules` requires an `unlockDate` in the future, otherwise `400`.
- While the date has not arrived, the response has `lockStatus: true` and `content: null`.
- On the unlock date the content is returned and `lockStatus` is `false`.
- A time capsule cannot be edited (`PUT` is not supported).

Code: `TimeCapsuleMapper.toResponse` decides whether to include the content. The database always holds it, the API hides it.

---

## US-8: Get a journaling prompt from AI
**As a** user, **I want** to ask for a writing prompt and save the ones I like, **so that** I find it easier to start writing.

Acceptance criteria
- `POST /api/v1/gemini-prompts/generate` with a prompt (not blank, at most 500 characters) returns generated text.
- I can make at most 10 requests per hour. After that `429`.
- If the AI service is down or slow I get a clear error (`502`, `503` or `504`), not a hanging request.
- `POST /api/v1/gemini-prompts` saves a text to my history, and `GET /api/v1/gemini-prompts` lists it.

Code: `GeminiPromptController` → `GeminiPromptService` (`RateLimiter`, HTTP call with timeouts to Gemini).

---

## US-9: Administer users
**As an** admin, **I want** to list and look up all users, **so that** I can manage the system.

Acceptance criteria
- `GET /api/v1/users` and `GET /api/v1/users/{id}` work for an admin.
- The same request from a normal user returns `403`.
- A role change in the database takes effect on the next request, without the user logging in again.

Code: routes are marked `Role.ADMIN` in `UserController`. The role check in `ApplicationConfig` compares it with the role `SecurityFilter` loaded from the database.

---

## US-10: Rediscover an old memory
**As a** user, **I want** to be shown a random memory, **so that** I can revisit moments I had forgotten.

Acceptance criteria
- `GET /api/v1/memories/random` returns one of my memories.
- It never returns somebody else's memory.

Code: `AbstractController.randomByUserId` → `GenericDAO.getRandomByUserId`.

---

## US-11: Publish a changelog to everyone
**As an** admin, **I want** to publish a devlog about a new feature, **so that** every user is told about it and can read how to use it.

Acceptance criteria
- Only an admin can `POST`, `PUT` or `DELETE` `/api/v1/devlogs`. A normal user gets `403`.
- Every logged-in user can read all devlogs with `GET /api/v1/devlogs` and `GET /api/v1/devlogs/{id}`, whoever wrote them.
- When an admin creates a devlog, the response is `201` immediately, and every user receives one `DEVLOG` notification.
- A failure while notifying one user does not stop the others.

Code: `DevlogController` (admin roles on the write routes) -> `DevlogService.createDevlog` -> `ExecutorService` -> `NotificationService.notifyAllUsers`.

---

## US-12: Be told when a time capsule opens
**As a** user, **I want** a notification when my time capsule is unlocked, **so that** I remember to open it.

Acceptance criteria
- A background job checks every minute for capsules whose `unlockDate` has been reached and that have not been notified.
- The owner gets exactly one `TIME_CAPSULE` notification per capsule, even if the job runs many times.
- Capsules that are still locked, or already notified, are ignored.

Code: `NotificationScheduler` (`ScheduledExecutorService`) -> `TimeCapsuleService.notifyUnlockedCapsules` -> `TimeCapsuleDAO.findUnlockedAndNotNotified...`. The `unlockNotified` flag prevents duplicates.

---

## US-13: See notifications live
**As a** user, **I want** new notifications to appear without refreshing, **so that** I notice them straight away.

Acceptance criteria
- I can open `ws://.../ws/notifications?token=<JWT>` and stay connected.
- A missing or invalid token closes the connection with code `4401`.
- When a notification is created for me, I receive it as JSON on every open connection (for example several tabs).
- The connection stays open (the server pings every 20 seconds).
- Notifications are saved first, so I can always fetch what I missed with `GET /api/v1/notifications`.

Code: `NotificationSocketController` (token check) -> `ConnectionRegistry` (thread-safe map of connections) <- `NotificationService.sendNotification`.
