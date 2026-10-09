# Paperless World 🌐

> **Paperless World: every thought stays open until you close it.**

---

## 2. The Problem and the Idea
Traditional note applications are passive graveyards: you capture a thought, tag it, and never look at it again until search unearths it months too late. **Paperless World** treats every thought as an **Open Loop** that demands a deliberate decision. If you neglect an open thought, the app’s deterministic rule engine brings it back to your home screen with a plain-English explanation of why it returned.

> *"Nothing disappears silently. It stays open until you decide, and the app asks again."*

---

## 3. Screenshots

| Home: Back for Another Look | Triage Inbox | Thought Detail & Audit History |
| :---: | :---: | :---: |
| `docs/screenshots/home_resurfacing.png` | `docs/screenshots/inbox_triage.png` | `docs/screenshots/thought_detail.png` |
| *Open loops resurfaced with reasons* | *Keep, Add action, or Close* | *Full audit trail & state events* |

*(Screenshots referenced from [docs/screenshots/](file:///c:/Users/singh/OneDrive/Desktop/proj/code/prerna/docs/screenshots))*

---

## 4. Feature List

### In Scope (Review 1)
- [x] **Zero-Friction Capture**: Fast title-only capture, idempotent by client token.
- [x] **Open Loop Lifecycle**: Strict state machine (`INBOX` $\to$ `OPEN` $\leftrightarrow$ `CLOSED`).
- [x] **Deliberate Closure**: Rejects closures without explicit choice (`DONE`, `LET_GO`, `RECORDED`).
- [x] **OOP Thought Hierarchy**: Subclasses (`IdeaThought`, `QuestionThought`, `NoteThought`) with polymorphic cooling thresholds (`Duration`).
- [x] **Resurfacing Rule Engine**: 6 strategy rules evaluating neglect without machine learning.
- [x] **Controllable Demo Clock**: Per-user virtual offset stored in database for viva time travel.
- [x] **Optimistic Concurrency Control**: Tab collision rejection via `version_no` check.
- [x] **Atomic Multi-Write Transactions**: Three-write closure transaction managed by `TransactionManager`.
- [x] **Security Hardening**: PBKDF2 (600,000 iterations), 5-strike account lockout, CSRF filter, XSS escaping, IDOR defense.

### Planned for Review 2
- [ ] *Tag taxonomy & full-text search* (Planned - Review 2)
- [ ] *Snooze open loops for N days* (Planned - Review 2)
- [ ] *Weekly review summary from `thought_events`* (Planned - Review 2)
- [ ] *Smart classification & ShapeSuggester heuristics* (Planned - Review 2)

---

## 5. How It Works

### The Lifecycle State Machine
| From State | Allowed To State | Trigger Method | Business Outcome |
|---|---|---|---|
| `INBOX` | `OPEN` | `keep(now)` | Promoted to active open loop |
| `INBOX` | `CLOSED` | `close(reason, now)` | Triage rejection; requires closure reason |
| `OPEN` | `CLOSED` | `close(reason, now)` | Deliberate conclusion (`DONE`, `LET_GO`, `RECORDED`) |
| `CLOSED` | `OPEN` | `reopen(now)` | Reopening resets closure reason and logs audit event |
| *Any* | `INBOX` | *Forbidden* | Throws `IllegalStateTransitionException`. Loops never return to inbox. |

### The Six Resurfacing Rules (Strategy Pattern)
| Rule Code | Priority | Trigger Condition | Stored Reason Text |
|---|:---:|---|---|
| `OVERDUE_ACTION` | 10 | Action attached to thought is past due and not marked done | *"Action overdue by N days"* |
| `HIGH_IMPORTANCE_QUIET` | 15 | Importance is 4 or 5 and untouched for $\ge$ 10 days | *"Important and quiet for N days"* |
| `NEVER_REVISITED` | 20 | State is `OPEN`, `version_no == 0`, age $\ge$ type cooling threshold | *"Open for N days, never edited"* |
| `QUESTION_UNANSWERED` | 25 | Type is `QUESTION` and open for $> 7$ days | *"Question still open after N days"* |
| `COOLING` | 30 | Last touched time is older than type cooling threshold | *"Not touched for N days"* |
| `STALE_INBOX` | 40 | Remained in `INBOX` for $> 7$ days | *"In the inbox for N days"* |

### Architecture Flow
```
Browser (JSP pages, plain CSS, small vanilla JS)
    │  HTTP
    ▼
Filters:  AuthFilter -> CsrfFilter -> SecurityHeadersFilter
    │
    ▼
Servlets (controllers): validate input, call service, choose view
    │
    ▼
Services: business rules, state machine, transactions (TransactionManager)
    │                                   ▲
    ▼                                   │
DAOs (JDBC, PreparedStatement)     ResurfacingScheduler
    │                              (one background daemon thread)
    ▼
MySQL 8 (Tomcat pooled JNDI DataSource)
```

---

## 6. Tech Stack
- **Language & Runtime:** Java 17+ / Java 21 LTS
- **Application Server:** Apache Tomcat 10.1 (Jakarta Servlet 6.0, Jakarta EE 10)
- **Presentation:** Server-Rendered JSP with JSTL, Vanilla CSS
- **Persistence:** MySQL 8 (InnoDB, `utf8mb4`), JDBC with `PreparedStatement`
- **Build & Test:** Apache Maven / Windows `build.bat`, JUnit 5
- **Dependencies:** Pure standard library with zero external AI or heavyweight web frameworks

---

## 7. Quick Start (5 Commands)

Run from project root directory `prerna/`:
```bash
# 1. Provision database schema and demo seed
mysql -u root -p < sql/01_schema.sql
mysql -u root -p < sql/02_seed_demo.sql

# 2. Build the WAR and run all 28 tests
.\build.bat

# 3. Deploy WAR to Tomcat and start with demo flag
copy target\dist\paperless-world.war %CATALINA_HOME%\webapps\
%CATALINA_HOME%\bin\catalina.bat run -Dpaperless.demo=true
```
Then open: `http://localhost:8080/paperless-world/home`

---

## 8. Full Setup Guide

### Prerequisites & Version Checks
1. **JDK 17+ (or JDK 21+):**
   ```bash
   java -version
   javac -version
   ```
2. **Apache Tomcat 10.1.x:**
   ```bash
   %CATALINA_HOME%\bin\version.bat      # Windows
   $CATALINA_HOME/bin/version.sh        # Linux / macOS
   ```
   *(Ensure Tomcat 10.1 is selected; Tomcat 9 uses obsolete `javax.servlet` and will fail).*
3. **MySQL Server 8.0+:**
   ```bash
   mysql --version
   ```

---

## 9. Database Setup

1. Open a terminal in `prerna/` and create the schema:
   ```bash
   mysql -u root -p < sql/01_schema.sql
   ```
2. Load the 5 pre-seeded back-dated thoughts:
   ```bash
   mysql -u root -p < sql/02_seed_demo.sql
   ```
3. Copy `context.xml.example` to `src/main/webapp/META-INF/context.xml` and configure your credentials:
   ```xml
   <Context>
     <Resource name="jdbc/paperless" auth="Container" type="javax.sql.DataSource"
               driverClassName="com.mysql.cj.jdbc.Driver"
               url="jdbc:mysql://localhost:3306/paperless_world?useSSL=false&amp;serverTimezone=UTC&amp;allowPublicKeyRetrieval=true"
               username="your_db_username" password="your_db_password"
               maxTotal="20" maxIdle="5" maxWaitMillis="5000"
               validationQuery="SELECT 1" testOnBorrow="true"/>
   </Context>
   ```

---

## 10. Build and Run

1. Build WAR package and execute full test suite:
   ```powershell
   # In prerna/
   .\build.bat
   ```
   *(Alternatively: `mvn clean package` if Maven is on your system PATH).*
2. Copy `target/dist/paperless-world.war` into Tomcat's `webapps/` folder.
3. Start Tomcat with the `-Dpaperless.demo=true` flag:
   - **Windows:**
     ```cmd
     set "JAVA_OPTS=-Dpaperless.demo=true"
     %CATALINA_HOME%\bin\catalina.bat run
     ```
   - **Linux / macOS:**
     ```bash
     export JAVA_OPTS="-Dpaperless.demo=true"
     $CATALINA_HOME/bin/catalina.sh run
     ```
4. Access web application at: `http://localhost:8080/paperless-world/login`

---

## 11. Demo Accounts and the 60-Second Script

### Seeded Credentials
- **Email:** `student@paperless.org`
- **Password:** `password123`

### To Reset Before a Rehearsal
Run from `prerna/`:
```bash
mysql -u root -p < sql/99_reset_demo.sql
```

### The 60-Second Viva Script
| Time | Action | Screen Feedback | Technical Explanation to Panel |
|---|---|---|---|
| **0:00** | Type: `Look into how connection pooling actually works` $\to$ Enter | Item appears in Inbox; suggested type `IDEA` | *"Zero-friction capture into INBOX. Idempotent by client token."* |
| **0:10** | Click **Keep** | Thought state transitions to `OPEN` | *"Triage: keeping turns the thought into an active open loop."* |
| **0:20** | Click **+15 Days** on the sticky demo top banner | Banner reads: `Demo clock: +15 days (simulated)` | *"Virtual per-user time source in MySQL allows live testing of time-based rules."* |
| **0:30** | Click **Run Resurfacing Now** | Home screen fills with cards under `BACK FOR ANOTHER LOOK` | *"Deterministic rule engine scans open loops against cooling thresholds."* |
| **0:40** | Inspect card #1 | Plain reason shown: `Open for 15 days, never edited` | *"Auditable, stored explanation in the database. No black-box AI."* |
| **0:50** | Click **Edit**, modify content, add action due Friday | The card departs the list | *"Touching the thought resets its cooling cycle."* |
| **0:58** | Attempt to close thought without choosing a reason | Rejection message: `A closure reason is required` | *"Closing is an explicit decision (`DONE`, `LET_GO`, `RECORDED`). Loops never vanish silently."* |

---

## 12. Run the Tests

Run the test suite using `build.bat` or standalone test runner:
```powershell
# In prerna/
java -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path "target/classes;target/test-classes" --scan-class-path --details=tree
```

### Current Status: 28/28 Tests Passing
- `ThoughtStateMachineTest` (5 tests): Full transition matrix verification & closure checks.
- `RuleEngineTest` (7 tests): Threshold boundary evaluation & simulated `TimeSource`.
- `ConcurrentEditTest` (1 test): **20 concurrent threads race on version 0**: exactly 1 succeeds, 19 throw `StaleThoughtException`.
- `CloseTransactionTest` (1 test): Atomic transaction rollback on audit event failure.
- `ThoughtOwnershipSecurityTest` (3 tests): IDOR broken access control prevention (`NotFoundException`).
- `PasswordHasherTest` (3 tests): PBKDF2 slow-hash, wrong password rejection, and salt uniqueness.
- `ValidatorsTest` (5 tests): Length, blank, null, non-numeric, and enum boundary validation.
- `ShapeSuggesterTest` (3 tests): Heuristic pattern detection.

---

## 13. Run Without Installing Anything (Devcontainer)

Open `prerna/` in **VS Code** with the *Remote - Containers* extension installed:
1. Press `F1` and choose **Remote-Containers: Reopen in Container**.
2. VS Code starts the container defined in [`.devcontainer/devcontainer.json`](file:///c:/Users/singh/OneDrive/Desktop/proj/code/prerna/.devcontainer/devcontainer.json).
3. The container forwards ports `8080` and `3306` automatically.

---

## 14. Project Structure

```
paperless-world/
├── README.md
├── pom.xml
├── build.bat
├── .gitignore
├── .devcontainer/devcontainer.json
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
│   │   │   ├── dao/             # ThoughtDao, ActionDao, UserDao, EventDao
│   │   │   ├── exception/       # StaleThoughtException, InvalidClosureException
│   │   │   ├── model/           # Thought, IdeaThought, QuestionThought, NoteThought
│   │   │   ├── service/         # ThoughtService, UserService, TransactionManager
│   │   │   │   └── resurfacing/ # Strategy Rules & ResurfacingEngine
│   │   │   ├── util/            # PasswordHasher, TimeSource, Validators
│   │   │   └── web/             # Servlets, Filters, AppLifecycleListener
│   │   ├── resources/db.properties
│   │   └── webapp/
│   │       ├── META-INF/context.xml.example
│   │       ├── WEB-INF/views/   # JSP views: home, inbox, edit, view, login
│   │       └── static/style.css
│   └── test/java/com/paperlessworld/
└── scripts/
    ├── setup-db.bat / setup-db.sh
    └── run-demo.md
```

---

## 15. Key Design Decisions

1. **One unified `thoughts` table:** Replaced separate tables for notes, ideas, and questions with a `thought_type` discriminator column, eliminating complex `UNION` queries.
2. **Optimistic Locking with `version_no`:** Prevents silent overwrites across browser tabs without holding thread locks in memory.
3. **Single Background Scheduler Thread:** Background resurfacing runs on one daemon thread (`scheduleWithFixedDelay`) so slow runs never overlap, and idempotency prevents duplicate events on the same day.
4. **Virtual Controllable `TimeSource`:** Stores demo clock offsets per user in MySQL so the engine and UI share the same simulated time without system clock manipulation.

---

## 16. Security Notes

- **SQL Injection:** 100% parameterised queries via JDBC `PreparedStatement`.
- **Stored XSS:** Escaped outputs with `<c:out>` and Content Security Policy (`script-src 'self'`).
- **Broken Access Control (IDOR):** Scoped queries with `AND user_id = ?`; unauthorized IDs return `404 Not Found`.
- **CSRF Defense:** Session tokens verified by `CsrfFilter` on all state-changing `POST` requests.
- **Credential Storage:** PBKDF2WithHmacSHA256, 600,000 iterations, 16-byte random salt per user.
- **Brute-Force Lockout:** Accounts lock for 10 minutes after 5 consecutive failed login attempts.

---

## 17. Troubleshooting Guide

| Issue | Likely Cause | Solution |
|---|---|---|
| `ClassNotFoundException: jakarta.servlet.*` | Running on Tomcat 9 or older | Deploy to **Tomcat 10.1+** (supports Jakarta EE 10 / Servlet 6.0). |
| `UnsupportedClassVersionError` | JDK / JRE version mismatch | Ensure `JAVA_HOME` points to JDK 17+ or JDK 21+. |
| `Access denied for user 'root'@'localhost'` | Database password mismatch | Verify credentials in `META-INF/context.xml` and `db.properties`. |
| Port 8080 already in use | Conflicting process on 8080 | Change `<Connector port="8080">` to `8081` in `%CATALINA_HOME%\conf\server.xml`. |
| Demo banner not functioning | System property missing | Start Tomcat with `-Dpaperless.demo=true`. |
| Timezone off-by-one discrepancies | MySQL server timezone not UTC | Ensure JDBC URL contains `&serverTimezone=UTC`. |

---

## 18. Team and Responsibilities

| Member | Academic Role | Packages & Deliverables Owned |
|---|---|---|
| **Prabhjot Singh** | Lead Architect & Core Java | `model`, `service.resurfacing`, `concurrency`, `TimeSource`, `TransactionManager` |
| **Team Member 2** | Persistence & Data Access | `dao`, `schema.sql`, `sql/*`, connection pooling, transaction boundary tests |
| **Team Member 3** | Web & Security | `web.servlet`, `web.filter`, `PasswordHasher`, JSP views, authentication |

---

## 19. Roadmap

- **Review 1 (Current):** Open loops, triage, 3-rule resurfacing, 60-second wow demo, optimistic locking, security filter chain.
- **Review 2 (Phase 3):** Tag system, full-text MySQL search, weekly review generation, remaining rules (`QUESTION_UNANSWERED`, `HIGH_IMPORTANCE_QUIET`, `STALE_INBOX`), snooze mechanism.
- **Future Directions:** Real-link network graph, attachment storage, end-to-end export.

---

## 20. Licence and Acknowledgements

Developed as an academic software engineering project exploring deterministic open-loop psychology and robust Jakarta EE architecture.  
Distributed under the MIT Licence.
