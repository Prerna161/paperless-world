# 60-Second Live Viva Demo Script

Follow this script during the technical viva examination to demonstrate the open-loop lifecycle, deterministic resurfacing rules, optimistic concurrency, and audit guarantees in under 60 seconds.

---

## 1. Pre-Demo Setup
Reset the demo user (`student@paperless.org`) to the starting 5-thought benchmark state:
```bash
mysql -u root -p < sql/99_reset_demo.sql
```

Ensure Tomcat 10.1 is running with the demo property:
```bash
catalina.bat run -Dpaperless.demo=true
```
*(On Linux/macOS: `./catalina.sh run -Dpaperless.demo=true`)*

Open your browser to: `http://localhost:8080/paperless-world/login`  
Sign in with:
- **Email:** `student@paperless.org`
- **Password:** `password123`

---

## 2. The 60-Second Viva Script

| Time | Action | Screen Feedback | Technical Explanation to Panel |
|---|---|---|---|
| **0:00** | Type: `Look into how connection pooling actually works` and press Enter | Card lands in Inbox instantly; type `IDEA` suggested | *"Zero-friction capture into INBOX. Enforced by client-token idempotency so double-clicks never duplicate."* |
| **0:10** | Click **Keep** on the newly captured card | State transitions from `INBOX` to `OPEN`; inbox count decrements | *"Triage: keeping transitions the thought to an active Open Loop."* |
| **0:20** | Click **+15 Days** on the sticky demo top banner | Top banner updates: `Demo clock: +15 days (simulated)` | *"Virtual time machine (`TimeSource`) stored per-user in MySQL to deterministically evaluate time-based rules without waiting."* |
| **0:30** | Click **Run Resurfacing Now** | `BACK FOR ANOTHER LOOK` tray populates with resurfaced thoughts | *"Deterministic Strategy pattern engine scans open thoughts and evaluates type-specific cooling thresholds."* |
| **0:40** | Point at card: *Connection pooling* | Card displays stored reason: `Open for 15 days, never edited` | *"Zero black-box AI. Every resurfacing has a stored, auditable reason persisted in the database."* |
| **0:50** | Click **Edit**, modify title/body, add action due Friday | The card leaves the resurfaced list | *"Interacting with a thought resets its cooling cycle."* |
| **0:58** | Try to close a thought without selecting a reason | Rejection alert: `A closure reason is required` | *"Closing is a deliberate decision (`DONE`, `LET_GO`, `RECORDED`). Open loops never disappear silently."* |

---

## 3. Concurrency Proof (Failure Path)
To show optimistic locking live to the panel:
1. Open two browser tabs on the same thought's edit page: `/thoughts/edit?id=1`.
2. Edit title in Tab 1 and click **Save Changes** (succeeds, increments `version_no`).
3. Without refreshing, click **Save Changes** in Tab 2.
4. **Result:** Tab 2 is rejected with message:
   > *"This thought changed in another tab. Reload to see the latest version."*

---

## 4. Closing Viva Statement
> *"Nothing disappears silently. It stays open until you decide, and the app asks again."*
