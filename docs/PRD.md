 # Paperless World: Redesigned PRD
**Date:** Oct 9, 2026  
**Author:** @Prabhjot singh  
**Tagline:** *Nothing you thought of stays lost, and nothing stays open forever.*

---

## 1. Critical Analysis of the Current PRD
The PRD has one good instinct buried under a Keep, Evernote and Day One feature list. It also contradicts itself in ways that would surface in the first viva question. Assumptions: a team of 2 to 3, MySQL 8, Tomcat 10.1, no external APIs.

### A. What is Genuinely Unique
- The idea that a thought has a lifecycle and old thoughts should come back. This is the only differentiator in the whole document.
- A deterministic resurfacing score with no AI. Defensible in a viva.
- Optimistic locking with `version_number` for concurrent edits. Real, testable, and better than adding threads for show.
- 'Task conversion keeps the original note.' A good rule, but it breaks the model (see contradictions).

### B. What is Generic
- Notes, tags, collections, folders, search, archive, trash, attachments: the standard Evernote feature list.
- Journal timeline with mood: Day One.
- Tasks with `TODO`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`: Todoist.
- Daily review as a list of counts: every habit app.
- Personas 'Student, Creator, Professional' describe everyone, so they describe no one.

### C. What Feels Like a Notion, Keep or Evernote Clone
- Rich-text editor with highlights, quotes and code blocks.
- Collections plus tags plus type plus status: Notion properties.
- Personal knowledge graph: Obsidian.
- Version history with restore: Notion and Google Docs.

### D. Unnecessary
- Rich-text editor. Large effort, a stored-XSS risk that needs HTML sanitising, and no link to the central idea.
- Collections. They duplicate tags.
- Attachments in Review 1. File-upload security work for zero idea value.
- Mood. It only exists to feed charts nobody asked for.
- Separate `ideas`, `journal_entries` and `reminders` tables.

### E. Buzzwords Without Substance
- 'Personal knowledge graph': no mechanism defined, only a picture.
- 'Smart classification': shown as Detected: ACTIONABLE with no rules written down.
- 'UNDERSTAND' as a lifecycle stage. The system cannot understand anything, so nobody can implement or test this stage.
- The resurfacing formula lists five terms with no scales or weights, so it cannot be implemented as written.

### F. Excessive Complexity
- 15 tables, three background workers, rich text, attachments, graph and versioning before Review 1 proves anything.
- Rich text plus attachments plus graph is three separate mini-projects.

### G. Real Opportunities to Show Java, JDBC and Servlets
- A Thought hierarchy with real behavioural differences.
- A rule engine as the Strategy pattern.
- Optimistic locking and transactions on conversion and restore.
- A scheduler with graceful shutdown.
- Ownership checks on every query.

### H. Memorable Demo Candidates
- Turning one messy sentence into an idea with a dated action.
- A forgotten idea coming back. This is currently undemonstrable in a live review because it needs 18 days of waiting, and the PRD has no answer for that.

### I, J, K. Remove, Merge, Centre
- **Remove:** rich-text editor, collections, attachments (to future), mood, graph visual, daily_reviews and reminders tables.
- **Merge:** notes, ideas, journal and references into one `thoughts` table with a type; reminders into an action's due date; archive into 'closed'.
- **Centre:** make lifecycle and resurfacing one mechanism, an open loop that stays on the radar until closed.

### Contradictions that Must be Fixed
1. **OOP model versus data model:** Section 27 makes Task a subclass of Content. Section 25 stores notes, tasks, journal entries and ideas in separate tables. That duplicates columns and forces a four-table UNION for any cross-type search.
2. **Task conversion:** The business rule says converting a note to a task keeps the note. A task that is both a type of content and a child of a note is impossible. Tasks are actions attached to thoughts, not thoughts.
3. **Capture first, organise later:** Review 1 must-haves include collections, tags and a rich-text interface, which is exactly organising first.
4. **Three overlapping state systems:** Lifecycle stage, active/archive/trash, and task status are never reconciled.
5. **Two kinds of 'version':** `notes.version_number` for locking and a `note_versions` table for history are not distinguished.
6. **Weak Review 1 showpiece:** Quick Capture plus a timeline is a plain notes app. The differentiator (resurfacing) is deferred to Phase 3, so the first review shows a clone.
7. **Chat leakage:** 'I recommend', 'This is the distinctive feature', 'The core idea I would lock in', 'We should'. A PRD must not speak to its reader like an assistant.
8. **Workers:** Three separate workers, while the PRD itself says not to add threads for the rubric.

### What Will Earn Marks (Keep)
- Abstract class hierarchy, generic DAO, optimistic locking, transactions, rule engine as Strategy, scheduler with shutdown, ownership checks, PBKDF2, CSRF, PreparedStatement.

---

## 2. Five Distinct Directions
Each direction keeps the same stack and replaces 'a place to store thoughts' with one specific mechanism. They differ in what the system does to a thought after it is saved.

| # | Direction | Central Mechanism | Java Depth | Demoable in 60 s | Main Risk |
|---|---|---|---|---|---|
| **1** | **Open Loops** | **Lifecycle state machine + resurfacing rules** | **High: hierarchy, Strategy, scheduler** | **Yes, with a demo clock** | **Needs a clock trick to show time passing** |
| 2 | Capture to Commit | Forced clarify step | Medium | Yes | Reads as a to-do app |
| 3 | Idea Garden | Growth and decay score | Low to medium | Yes | Arbitrary scoring, gamification |
| 4 | Thought Threads | TF-IDF related thoughts | High: algorithm | Weak with little data | Poor results on small data |
| 5 | Say It Messy | Free-text parser | Medium to high | Yes | Edge-case sink, narrow product |

---

## 3. Selected Direction and Why
**Selected: Direction 1, Open Loops.** Paperless World becomes a system where every thought is an open loop until the user deliberately closes it. It borrows the 'cooling' signal from the Idea Garden and a very small version of the capture parser from Say It Messy.

### Why It Wins
1. It matches the PRD's own best idea.
2. It has a real user problem.
3. It is explainable in one sentence: *"A thought stays open until you close it, and the app brings back the ones you are neglecting."*
4. It maps cleanly onto the rubric.
5. It works on small data.
6. It is deterministic.

---

## 4. Product Identity and Central Mechanism
- **Name:** Paperless World
- **Tagline:** *Nothing you thought of stays lost, and nothing stays open forever.*
- **One sentence:** Paperless World is a personal thinking inbox where every thought is an open loop until you close it, and a deterministic engine brings back the loops you are neglecting.
- **Target user:** One person with more ideas and half-intentions than they can track, such as a student with project ideas, reading notes and 'I should look into this' items.

### The Central Mechanism: The Open Loop
| Element | Definition |
|---|---|
| **Thought** | One captured piece of text with a type (`IDEA`, `NOTE`, `QUESTION`, `JOURNAL`, `REFERENCE`). |
| **State** | `INBOX` (just captured), `OPEN` (acknowledged, still alive), `CLOSED` (finished with). |
| **Closure reason** | `DONE` (acted on), `LET_GO` (deliberately dropped), `RECORDED` (kept as reference, no action needed). |
| **Action** | An optional dated next step attached to a thought, separate from the thought itself. |
| **Resurfacing** | A rule fires, a reason is stored, and the thought appears in 'Back for another look'. |

---

## 5. Core Loop
The product repeats one loop: capture, decide, revisit, close.
1. **Capture:** Enters `INBOX`.
2. **Decide:** Keeps it (`OPEN`), attaches an action with due date, or closes with reason.
3. **Cool:** Time passes.
4. **Resurface:** A rule fires. Appears in 'Back for another look' with stored reason.
5. **Revisit:** Edits, adds action, or closes. Touching it resets cooling clock.
6. **Close:** Leaves open set with `DONE`, `LET_GO`, or `RECORDED`.

### Allowed State Transitions
| From | To | Trigger | Rule |
|---|---|---|---|
| (new) | `INBOX` | Capture | Always allowed |
| `INBOX` | `OPEN` | Keep | Allowed |
| `INBOX` | `CLOSED` | Close with reason | Reason required |
| `OPEN` | `CLOSED` | Close with reason | Reason required |
| `CLOSED` | `OPEN` | Reopen | Clears closure reason, writes event row |
| `OPEN` | `INBOX` | (none) | **Not allowed** |
| `CLOSED` | `INBOX` | (none) | **Not allowed** |

Any other transition throws `IllegalStateTransitionException`.

---

## 6. Feature Triage
Review 1 line: Capture, the three-state lifecycle, closure reasons, actions, three resurfacing rules with reasons, the home page, optimistic locking, and the demo clock. Everything else waits.

---

## 7. User Journeys
There is one role, the signed-in user. Four journeys cover the product.

### Journey 1: First capture (under 30 seconds)
1. Register and sign in.
2. The home page shows an empty state and a single capture box.
3. Type *"Look into how connection pooling actually works"* and press Enter.
4. The thought appears in `INBOX` with type `IDEA` suggested and editable.

### Journey 2: Triage the inbox
1. Open the inbox. Each card has three buttons: **Keep**, **Add action**, **Close**.
2. **Keep** moves it to `OPEN`. **Add action** asks for a short text and a due date. **Close** asks for a reason (`DONE`, `LET_GO`, `RECORDED`).
3. The inbox count drops, and the home page reflects it.

### Journey 3: A thought comes back
1. Two weeks later, the user opens the home page.
2. 'Back for another look' lists the connection-pooling thought with *"Open for 14 days, never edited"*.
3. The user edits it into a concrete plan and adds an action due Friday. The cooling clock resets, and the thought leaves the list.

### Journey 4: Letting go and reopening
1. A stale thought is closed as `LET_GO`. It leaves every active list.
2. Weeks later, a search finds it under Closed. The user reopens it. The closure reason is cleared and an event row records the reopening.

**Failure path worth showing:** Two browser tabs edit the same thought. The second save is rejected with *"This thought changed in another tab. Reload to see the latest version."* No text is silently overwritten.

---

## 8. The 60-Second Wow Demo
The moment: a forgotten thought comes back on its own, and the screen tells you exactly why. The demo uses a controllable clock, and says so out loud.

### Setup (before the panel arrives)
Seed one demo user with five thoughts, created with back-dated timestamps through `db/seed.sql`:
- an idea untouched for 20 days (`OPEN`),
- a question with an action overdue by 3 days,
- a note edited 2 days ago (should not resurface),
- an inbox item 9 days old,
- a closed thought, to show it stays out.

### The Script
| Time | What you do | What the panel sees |
|---|---|---|
| **0:00** | Type a messy thought into the capture box and press Enter | It lands in the inbox instantly |
| **0:10** | Press Keep | State changes to `OPEN`, the count updates |
| **0:20** | Open the demo panel and move the clock forward 15 days | A banner reads *'Demo clock: +15 days'* |
| **0:30** | Click 'Run resurfacing now' | The home page fills with *'Back for another look'* cards |
| **0:40** | Point at one card | *Open for 15 days, never edited* is shown as the reason |
| **0:50** | Edit the thought and add an action due Friday | The card leaves the list, because touching it reset the clock |
| **0:58** | Try to close it without a reason | Rejected with a clear message, showing closing is a decision |

**Closing line:** *"Nothing disappears silently. It stays open until you decide, and the app asks again."*

---

## 9. Architecture
A classic layered Jakarta EE web application on Tomcat 10.1. Requests flow down one layer at a time, and no layer skips another.

```
Browser (JSP pages, plain CSS, small vanilla JS)
    │  HTTP
    ▼
Filters:  SecurityHeadersFilter -> AuthFilter -> CsrfFilter
    │
    ▼
Servlets (controllers): validate input, call a service, forward to a JSP
    │
    ▼
Services: business rules, state machine, transactions (TransactionManager)
    │                                   ▲
    ▼                                   │
DAOs (JDBC, PreparedStatement)     ResurfacingScheduler
    │                              (one background thread, rule engine)
    ▼
MySQL 8 (connections from Tomcat's pooled DataSource)
```

### Layer Responsibilities
| Layer | Package | Does | Never Does |
|---|---|---|---|
| Web | `web.servlet`, `web.filter` | Parse request, check session, call service, choose view | SQL, business rules |
| Service | `service` | Rules, transitions, transactions, events | HTTP types, raw SQL |
| Rules | `service.resurfacing` | Evaluate each rule, return reasons | Write to the database directly |
| DAO | `dao` | One method per query, takes a Connection | Decide business outcomes |
| Model | `model` | Thought hierarchy, enums, value objects | I/O |
| Util | `util` | TimeSource, PasswordHasher, validators | State |

### Key Architectural Decisions
1. Server-rendered JSP, no front-end framework.
2. Connection pool via Tomcat DataSource (JNDI) in `context.xml`.
3. `TransactionManager.inTransaction(Function<Connection,R>)`.
4. One scheduler thread (`ScheduledExecutorService.scheduleWithFixedDelay`).
5. `TimeSource` interface for deterministic testing and virtual demo clock.

---

## 10. Java Design

### 10.1 Thought Hierarchy (Inheritance and Polymorphism)
Subclasses differ in behaviour (cooling threshold Duration, polymorphic rules), not only in a label.
- `IdeaThought`: cooling threshold = 14 days
- `QuestionThought`: cooling threshold = 7 days
- `NoteThought`: cooling threshold = 30 days
- `ThoughtFactory.fromRow(ResultSet)` maps rows without `instanceof` elsewhere.

### 10.2 State Machine in an Enum
`ThoughtState` holds the allowed transition table and exposes `requireTransitionTo(target)`.

### 10.3 Resurfacing Rules (Strategy Pattern)
`ResurfacingRule` interface evaluates `(Thought, List<Action>, Instant)` and returns `Optional<ResurfacingReason>`.
- `NEVER_REVISITED`: `OPEN`, never edited since creation, age $\ge$ cooling threshold (*"Open for N days, never edited"*)
- `COOLING`: `OPEN`, last touched more than threshold ago (*"Not touched for N days"*)
- `OVERDUE_ACTION`: Action is past due and not done (*"Action overdue by N days"*)
- `STALE_INBOX`: `INBOX` for more than 7 days (*"In the inbox for N days"*)
- `QUESTION_UNANSWERED`: `QUESTION`, `OPEN` more than 7 days (*"Question still open after N days"*)
- `HIGH_IMPORTANCE_QUIET`: Importance 4 or 5 and not touched for 10 days (*"Important and quiet for N days"*)

### 10.4 Shape Suggester (Pattern Detector)
`ShapeSuggester` checks text patterns: '?' suggests `QUESTION`, 'build'/'try' suggests `IDEA`, date words suggest actions.

### 10.5 Core Java Features Used
- Records: `ResurfacingReason(String ruleCode, String text, int daysNeglected)`
- `java.time`: `Instant`, `Duration`, `LocalDate` (never legacy `java.util.Date`)
- Generics in `TransactionManager`
- Custom unchecked business exceptions: `IllegalStateTransitionException`, `InvalidClosureException`, `StaleThoughtException`, `NotFoundException`

---

## 11. Multithreading

### 11.1 The Resurfacing Scheduler (One Background Thread)
- Managed by `AppLifecycleListener` (`@WebListener`).
- `Executors.newSingleThreadScheduledExecutor` with daemon thread named `resurfacing-worker`.
- `scheduleWithFixedDelay(resurfacingJob, 1, 15, TimeUnit.MINUTES)`.
- Graceful shutdown in `contextDestroyed()` with `awaitTermination(10, TimeUnit.SECONDS)`.
- Idempotent writes via `UNIQUE(thought_id, surfaced_on)` and `INSERT IGNORE`.
- Exposes `runNow(userId)` for the live demo button.

### 11.2 Concurrent Edits
- Optimistic locking with `version_no`. Failed update throws `StaleThoughtException`.
- Idempotent capture via `client_token` with `UNIQUE(user_id, client_token)`.

### 11.3 Concurrency Test for Viva
A JUnit test spawns 20 threads attempting to update the same thought with the identical starting `version_no`. Exactly 1 succeeds and 19 receive `StaleThoughtException`.

---

## 12. Database Design (MySQL 8 Schema)

Seven tables with InnoDB, `utf8mb4`, and `DATETIME(3)` in UTC:
1. `users`: user accounts, PBKDF2 hash, `demo_offset_days`, lockout counters.
2. `thoughts`: single unified table with `client_token`, `importance`, `version_no`, and `ck_thoughts_closure` check constraint.
3. `actions`: dated next steps.
4. `tags`: user tags.
5. `thought_tags`: many-to-many relationship.
6. `thought_events`: append-only state transition audit log.
7. `resurfacing_events`: recorded daily resurfacings with unique daily constraint.

---

## 13. JDBC Design

- Pooled DataSource looked up via JNDI (`jdbc/paperless`) or environment fallback.
- `TransactionManager.inTransaction(Function<Connection, R>)` handles commit, rollback, and auto-commit cleanup.
- Optimistic update checks `executeUpdate() == 1`.
- Closing a thought atomically executes 3 writes in 1 transaction:
  1. Update `thoughts`
  2. Insert `thought_events`
  3. Update `resurfacing_events` (`dismissed_at = ?`)
- `PreparedStatement` on every query with try-with-resources.

---

## 14. Servlets and Web Integration

### URL Map
- `/register`, `/login`, `/logout`
- `/home` (Capture box, 'Back for another look', counts)
- `/inbox` (List inbox thoughts)
- `/thoughts` (List by state, tag, or search)
- `/thoughts/new` (Idempotent capture)
- `/thoughts/view`, `/thoughts/edit`
- `/thoughts/keep`, `/thoughts/close`, `/thoughts/reopen`
- `/actions/add`, `/actions/done`
- `/demo/clock` (Move demo clock)
- `/demo/run` (Run resurfacing now)

### Filter Chain
1. `SecurityHeadersFilter`
2. `AuthFilter`
3. `CsrfFilter`

---

## 15. Security

- PBKDF2WithHmacSHA256 with 600,000 iterations, 16-byte random salt, stored as `iterations:salt:hash`.
- Brute-force protection: 5 failed logins locks account for 10 minutes.
- Stored XSS defense: `<c:out>` and Content-Security-Policy headers.
- Broken access control defense: `user_id` on all queries; 404 returned for unowned resources.
- Demo endpoints guarded by `paperless.demo=true` or demo flag check.

---

## 16. UI and UX

- Clean server-rendered JSP layout.
- Visible, persistent "Demo clock: +15 days (simulated)" banner.
- Plain-text resurfacing reasons on all cards.
- Three-way deliberate closure: `DONE`, `LET_GO`, `RECORDED`.
- 360px+ responsive design with accessible focus rings and semantic markup.

---

## 17. Review 1 Scope

Review 1 must show the central idea working end to end, with enough rubric coverage to score well, and nothing that cannot be finished. The scope is deliberately smaller than the old PRD's Review 1.

### In Scope

| Area | Deliverable | Rubric Area |
| :--- | :--- | :--- |
| **Accounts** | Register, login, logout, PBKDF2, session, lockout | Servlets, Security |
| **Capture** | One-box capture, idempotent by `client_token` | Servlets, JDBC |
| **Lifecycle** | `INBOX`, `OPEN`, `CLOSED` with enforced transitions and closure reasons | Core Java |
| **Thought Model** | Abstract `Thought`, concrete subclasses, `ThoughtFactory` | Core Java |
| **Actions** | Add an action with due date, mark done | JDBC |
| **Resurfacing** | Engine with 3 rules (`NEVER_REVISITED`, `COOLING`, `OVERDUE_ACTION`), stored reasons | Core Java, Design |
| **Scheduler** | One `ScheduledExecutorService` with clean shutdown, plus Run now | Core Java |
| **Demo Clock** | Offset per user, demo-mode only | Design |
| **Editing** | Optimistic locking with `version_no` | JDBC |
| **Close Transaction** | Three writes in one transaction | JDBC |
| **Views** | Home, inbox, detail, edit pages (JSP with JSTL) | Servlets |
| **Security Basics** | PreparedStatement, escaping, ownership, CSRF filter | Servlets, JDBC |
| **Seed Data** | Demo user with five back-dated thoughts | Demo |
| **Tests** | State machine, each rule, optimistic-lock race, ownership | All |
| **README** | Install, run, demo script | Documentation |

### Out of Scope for Review 1
- Tags and full-text search (Review 2).
- Remaining three rules, snooze, ShapeSuggester, weekly review (Review 2).
- Rich text, collections, attachments, mood, graph, version history (not planned).

### Definition of Done for Review 1
1. A new user can capture, keep, close and reopen a thought.
2. Moving the demo clock forward 15 days and pressing Run now shows at least two resurfaced cards, each with a reason.
3. Closing without a reason is rejected by the service and by the database check.
4. The 20-thread edit test passes: one winner, nineteen `StaleThoughtException`.
5. User B cannot read user A's thought by guessing an id.
6. `mvn clean package` / `build.bat` builds the WAR with no failing tests, and the README works from a fresh clone.

*Scope guard*: Anything not in the table above needs a written reason that ties it to the open-loop mechanism. If it has none, it waits.

---

## 18. Roadmap

Five phases, each ending in something runnable.

| Phase | Weeks | Goal | Exit Gate |
| :--- | :--- | :--- | :--- |
| **0. Foundation** | 1 | Repo, Maven WAR, Tomcat running, schema and seed load, DataSource works | A JSP page reads one row from MySQL |
| **1. Capture & Lifecycle** (Review 1 core) | 2 | Accounts, capture, Thought hierarchy, state machine, closure with transaction, home and inbox pages | Journeys 1, 2 and 4 work end to end |
| **2. Resurfacing & Demo** (Review 1 finish) | 1 to 2 | Rule engine with 3 rules, scheduler, demo clock, optimistic locking, security filters, tests | Definition of done in section 17 passes |
| **3. Depth** (Review 2) | 2 | Tags, search, remaining 3 rules, snooze, weekly review from `thought_events`, ShapeSuggester | Rules 4 to 6 each have a unit test and a seeded example |
| **4. Polish & Viva** | 1 | Accessibility pass, README, screenshots, PPT, viva rehearsal, demo rehearsed three times | Fresh-clone run succeeds on a second laptop |

### Build Order Inside Phases 1 and 2
1. `ThoughtState` enum and its unit test.
2. `Thought` hierarchy, `ThoughtFactory`, `TimeSource`.
3. `TransactionManager`, DAOs, schema.
4. `ThoughtService` with capture, keep, close, reopen, edit.
5. Servlets and JSP for capture, inbox, detail.
6. `ResurfacingRule` implementations and `ResurfacingEngine`, tested with a fake `TimeSource`.
7. Scheduler, `runNow`, demo panel.
8. Filters, CSRF, lockout.
9. Concurrency test, ownership test, seed data, README.

### Risks and Responses
- **Time slips**: Cut order: `COOLING` rule last, then lockout, then demo panel polish. Never cut the state machine, the transaction or the locking test.
- **One person blocks the others**: Interfaces for DAOs and `TimeSource` let the service layer be built with in-memory fakes.
- **Demo fails live**: A recorded 60-second screen capture is kept as backup, and the seed script resets the demo user in one command.
- **Panel says 'it is just notes'**: Open with the closing line from Section 8 and show the stored reason in the database.

---

## 19. Testing

Testing follows the same layers as the code. Unit tests need no database, integration tests use a test schema, and one manual script covers the demo. Tools: JUnit 5, Maven Surefire, hand-written fakes preferred.

### 19.1 Unit Tests (No Database)
- **`ThoughtState`**: Every allowed transition passes. Every other pair throws `IllegalStateTransitionException`.
- **`Thought.close`**: Null reason throws. Valid reason sets state, reason and touch time.
- **Each `ResurfacingRule`**: Fires at the threshold, does not fire one day before, uses the right reason text, ignores `CLOSED` thoughts.
- **`ResurfacingEngine`**: Returns one reason per thought, orders by importance then days neglected, empty list for no thoughts.
- **`TimeSource` fake**: Moving the clock by 15 days changes rule outcomes with no sleeping.
- **`Validators`**: Null, blank, over-length, non-numeric id, unknown enum value.
- **`PasswordHasher`**: Verify succeeds for the right password, fails for a wrong one, two hashes of one password differ.

### 19.2 Integration Tests (Real MySQL, Test Schema)
- **Capture twice with the same `client_token`**: One row.
- **Close with reason**: `thoughts`, `thought_events` updated together.
- **Force the event insert to fail**: The thought update rolls back, state unchanged.
- **Insert `CLOSED` without a reason by raw SQL**: Rejected by `ck_thoughts_closure`.
- **Run resurfacing twice on the same day**: One `resurfacing_events` row per thought and rule (`uq_re_day`).
- **Query as user B for user A's id**: No row returned.
- **Special characters in title** (`'`, `"`, `<`, emoji): Stored and returned intact.

### 19.3 Concurrency Test
Twenty threads load the same thought at `version_no = 0` and each calls update. Assert that exactly one returns true, nineteen throw `StaleThoughtException`, and the final `version_no` is 1.

### 19.4 Servlet and Security Checks
- `GET /home` without a session: Redirect to `/login`.
- `POST` without CSRF token: `403`.
- Capture `<script>alert(1)</script>`: Displayed escaped as text.
- Request another user's `/thoughts/view?id=`: `404`.
- Six wrong passwords: Locked account, generic message.
- `/demo/clock` without demo flag: `404`.
- Error page: No stack trace.

### 19.5 Manual Demo Test
Run the Section 8 script three times from a fresh seed. Each run must finish in under 60 seconds, and the seed reset command must restore the starting state.

### 19.6 Exit Criteria
All unit and integration tests pass in `mvn clean package` / `build.bat`. The concurrency test is run 10 times without a flaky failure. Test names read as sentences, for example `closingWithoutReasonIsRejected`.

---

## 21. GitHub Repository Structure

The repository is organised so an evaluator can find the idea, the code and the run instructions within one minute.

```
paperless-world/
├── README.md
├── pom.xml
├── .gitignore
├── .devcontainer/
│   └── devcontainer.json
├── docs/
│   ├── PRD.md
│   ├── architecture.md
│   ├── demo-script.md
│   └── screenshots/
├── sql/
│   ├── 01_schema.sql
│   ├── 02_seed_demo.sql
│   └── 99_reset_demo.sql
├── src/
│   ├── main/
│   │   ├── java/com/paperlessworld/
│   │   │   ├── model/            Thought, IdeaThought, QuestionThought, NoteThought,
│   │   │   │                     ThoughtState, ThoughtType, ClosureReason, Action,
│   │   │   │                     ThoughtFactory, ResurfacingReason (record)
│   │   │   ├── dao/              ThoughtDao, ActionDao, UserDao, EventDao + JDBC impls
│   │   │   ├── service/          ThoughtService, UserService, ActionService
│   │   │   │   └── resurfacing/  ResurfacingRule, six rule classes, ResurfacingEngine,
│   │   │   │                     ResurfacingJob
│   │   │   ├── web/
│   │   │   │   ├── servlet/      one class per URL in section 14
│   │   │   │   ├── filter/       SecurityHeadersFilter, AuthFilter, CsrfFilter
│   │   │   │   └── listener/     AppLifecycleListener
│   │   │   ├── tx/               TransactionManager
│   │   │   ├── exception/        business and infrastructure exceptions
│   │   │   └── util/             TimeSource, SystemTimeSource, DemoTimeSource,
│   │   │                         PasswordHasher, Validators, Flash, SessionUser
│   │   ├── resources/
│   │   │   └── logging config
│   │   └── webapp/
│   │       ├── META-INF/context.xml.example
│   │       ├── WEB-INF/
│   │       │   ├── web.xml
│   │       │   └── views/        *.jsp, fragments/
│   │       └── static/           css/, js/
│   └── test/java/com/paperlessworld/
│       ├── model/                ThoughtStateTest, ThoughtTest
│       ├── service/resurfacing/  one test class per rule, EngineTest
│       ├── dao/                  integration tests (real MySQL)
│       └── concurrency/          OptimisticLockRaceTest
└── scripts/
    ├── setup-db.sh / setup-db.bat
    └── run-demo.md
```

### Repository Rules
| Rule | Detail |
| :--- | :--- |
| **Real credentials never committed** | `context.xml` is gitignored. Only `context.xml.example` is tracked |
| **Branches** | `main` always builds. Work happens on `feature/<name>` branches and merges by pull request |
| **Commit messages** | Imperative and specific, such as `Add NEVER_REVISITED rule with tests` |
| **One concern per commit** | Schema, model, DAO and servlet changes are not mixed |
| **CI** | A GitHub Actions workflow runs `mvn -B verify` with a MySQL service container on every push |
| **Releases** | A tag per review, such as `review-1`, so the evaluator sees exactly what was demoed |
| **Contribution evidence** | Each member owns named packages, listed in the README team table |

`.gitignore` essentials: `target/`, `*.iml`, `.idea/`, `.vscode/`, `META-INF/context.xml`, `*.log`, `.env`.

---

## 22. README Structure

The README is the first thing an evaluator opens. It should let someone with a laptop and no context run the project in under 40 minutes and understand the idea in two.

| # | Section | Content |
|---|:---|:---|
| 1 | **Title and one-line promise** | Paperless World: every thought stays open until you close it. |
| 2 | **The problem and the idea** | Three sentences on open loops and resurfacing, plus the closing line from section 8 |
| 3 | **Screenshots** | Home with resurfaced cards, inbox, detail with history (from `docs/screenshots/`) |
| 4 | **Feature list** | Short list matching Review 1 scope, with Review 2 items marked 'planned' |
| 5 | **How it works** | The lifecycle table, the six rules in one table, one architecture diagram |
| 6 | **Tech stack** | Java 17, Tomcat 10.1 (Jakarta Servlet 6), JSP and JSTL, JDBC, MySQL 8, Maven, JUnit 5 |
| 7 | **Quick start** | Five commands for people who know the tools |
| 8 | **Full setup** | JDK, Maven, Tomcat, MySQL install steps for Windows, macOS and Linux, with version checks |
| 9 | **Database setup** | Create the database, run `sql/01_schema.sql` then `sql/02_seed_demo.sql`, copy `context.xml.example` and fill in credentials |
| 10 | **Build and run** | `mvn clean package`, deploy the WAR, start Tomcat with `-Dpaperless.demo=true` for the demo, open the URL |
| 11 | **Demo accounts and the demo script** | Credentials for the seeded user, the 60-second script from section 8, how to reset with `99_reset_demo.sql` |
| 12 | **Run the tests** | `mvn test` for unit tests, how to run integration tests against a local MySQL |
| 13 | **Run without installing anything** | Codespaces or devcontainer instructions, as a fallback |
| 14 | **Project structure** | The tree from section 21, trimmed |
| 15 | **Design decisions** | Why one thoughts table, why optimistic locking, why one scheduler thread, why a demo clock |
| 16 | **Security notes** | The controls from section 15 in six lines |
| 17 | **Troubleshooting** | Table of the usual errors: wrong Tomcat version, JAVA_HOME, MySQL auth plugin, port 8080 busy, Access denied, timezone error |
| 18 | **Team and responsibilities** | Names, roles, and the packages each person owns |
| 19 | **Roadmap** | Review 2 items and future work |
| 20 | **Licence and acknowledgements** | Short |

### Writing Rules for the README
1. Every command is copy-pasteable and says which folder to run it in.
2. Every install step names the exact version needed and how to check it worked.
3. Passwords and secrets appear only as placeholders.
4. The troubleshooting table is written from errors the team actually hit, not guessed ones.
5. The README never promises features that are not built; unbuilt items are labelled 'planned'.


