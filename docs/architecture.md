# Paperless World: Architecture Specification

A classic layered Jakarta EE web application built on Tomcat 10.1 and MySQL 8. Requests flow strictly one layer at a time: no layer bypasses another.

```
Browser (JSP pages, plain CSS, small vanilla JS)
    │  HTTP
    ▼
Filters:  AuthFilter -> CsrfFilter -> SecurityHeadersFilter
    │
    ▼
Servlets (controllers): validate input, call a service, forward/redirect to JSP
    │
    ▼
Services: business rules, state machine, transactions (TransactionManager)
    │                                   ▲
    ▼                                   │
DAOs (JDBC, PreparedStatement)     ResurfacingScheduler
    │                              (one background thread, rule engine)
    ▼
MySQL 8  (Tomcat pooled JNDI DataSource)
```

---

## 1. Layer Responsibilities

| Layer | Package | Does | Never Does |
|---|---|---|---|
| **Web** | `web.servlet`, `web.filter`, `web.listener` | Parse request, check session, call service, choose view | SQL, business rules |
| **Service** | `service` | Rules, transitions, transactions, events | HTTP types, raw SQL |
| **Rules** | `service.resurfacing` | Evaluate each rule, return reason records | Write directly to database |
| **DAO** | `dao` | One method per query, takes a `Connection` | Decide business outcomes |
| **Model** | `model` | Thought hierarchy, enums, immutable records | I/O, database access |
| **Util** | `util` | `TimeSource`, `PasswordHasher`, `Validators` | State, DB queries |

---

## 2. Core Java Design

### 2.1 Thought Hierarchy (Inheritance & Polymorphism)
Subclasses differ in real behaviour (cooling threshold `Duration`, applicable rules), not simply labels.
- `Thought` (Abstract root)
  - `IdeaThought`: cooling threshold = 14 days
  - `QuestionThought`: cooling threshold = 7 days
  - `NoteThought`: cooling threshold = 30 days
  - `JournalThought`: cooling threshold = 30 days
  - `ReferenceThought`: cooling threshold = 60 days
`ThoughtFactory.create` and `ThoughtFactory.fromRow(ResultSet)` are the only places that select concrete classes; the rest of the codebase avoids `instanceof`.

### 2.2 State Machine in an Enum
`ThoughtState` (`INBOX`, `OPEN`, `CLOSED`) defines the allowed transition table and exposes `requireTransitionTo(target)`. Open loops cannot transition back to `INBOX`.

### 2.3 Resurfacing Rules (Strategy Pattern)
`ResurfacingRule` interface defines:
- `String code()`
- `int priority()`
- `Optional<ResurfacingReason> evaluate(Thought t, List<Action> actions, Instant now)`

Rules implemented:
1. `OVERDUE_ACTION` (priority 10)
2. `HIGH_IMPORTANCE_QUIET` (priority 15)
3. `NEVER_REVISITED` (priority 20)
4. `QUESTION_UNANSWERED` (priority 25)
5. `COOLING` (priority 30)
6. `STALE_INBOX` (priority 40)

`ResurfacingEngine` ranks matching candidates by `importance` descending, then `daysNeglected` descending.

---

## 3. Concurrency & Multithreading

1. **Background Scheduler Thread:**
   - Single daemon worker orchestrated by `AppLifecycleListener` using `ScheduledExecutorService.scheduleWithFixedDelay(1, 15, TimeUnit.MINUTES)`.
   - Clean shutdown with `awaitTermination(10, TimeUnit.SECONDS)`.
   - Idempotent writes using MySQL `UNIQUE(thought_id, surfaced_on)` and `INSERT IGNORE`.
2. **Optimistic Locking on Request Threads:**
   - Updates carry `version_no`.
   - If version mismatched, throws `StaleThoughtException`.
   - Verified by `ConcurrentEditTest` across 20 simultaneous threads.

---

## 4. JDBC & Transaction Management

`TransactionManager.inTransaction(Function<Connection, R>)` controls transaction boundaries cleanly without leaky try-catch-rollback boilerplate in services.
- Atomic closure transaction executes 3 writes in 1 transaction:
  1. `UPDATE thoughts` (state, closure reason, version bump)
  2. `INSERT thought_events` (transition audit record)
  3. `UPDATE resurfacing_events` (dismiss active notifications)
