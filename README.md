# Citizen Complaint Management System

A desktop application for citizens to lodge complaints with government departments and
for officials to track, filter, and resolve them. Built with **JavaFX** and **Hibernate**
on an **Oracle** database, with **email-based OTP verification** at signup.

---

## Tech Stack

| Layer            | Technology                                             |
|------------------|--------------------------------------------------------|
| Language         | Java 21                                                 |
| Build tool       | Apache Maven                                            |
| UI               | JavaFX 21 (FXML views + CSS)                            |
| Persistence      | Hibernate ORM 6.3 (Jakarta Persistence / JPA)          |
| Database         | Oracle Database 21c XE                                  |
| Connection pool  | HikariCP                                                |
| Password hashing | jBCrypt                                                 |
| Email (OTP)      | Brevo transactional email API (via the JDK HTTP client)|
| Logging          | SLF4J + Logback                                         |

---

## Architecture

A clean layered design under the package `com.complaint.system`:

```
MainApplication                 JavaFX entry point (starts UI, seeds demo data)
        │
controller/   ── FXML view controllers (Login, Signup, Verification,
        │         Citizen/Official dashboards, Complaint details, Profile)
        │
dao/          ── Data access objects over Hibernate (BaseDAO + per-entity DAOs)
        │
entity/       ── JPA entities: User, Complaint, ComplaintHistory, Department
        │
util/         ── SceneManager, SessionManager, validators, email/OTP helpers,
                  StatusBadge, DataSeeder

resources/
  fxml/       ── Screen layouts
  css/        ── styles.css (theme)
  hibernate.cfg.xml, logback.xml
```

- **Roles:** `CITIZEN` and `OFFICIAL`.
- **Complaint statuses:** `LODGED → IN_PROGRESS → RESOLVED → CLOSED`.

---

## Application Flow

1. **Launch** → the app connects to Oracle, seeds demo data, and shows the **Login** screen.
2. **Sign up** (new users):
   - Enter full name, username, email, account type, and a password.
   - The app checks the email/username are available, then emails a **6-digit OTP**.
   - Enter the OTP on the **Verification** screen. The account is created **only after**
     the code is verified — so fake/unreachable emails can't register.
3. **Login** → routed by role:
   - **Citizen dashboard:** lodge a complaint (title, department, description), view
     *Your Complaints* with color-coded status badges, and open a complaint to see its
     full **status history**.
   - **Official dashboard:** view *all* complaints, **filter** by department/status,
     and **update a complaint's status** with remarks (each change is recorded in history).
4. **My Profile** (both roles) → edit full name and username.
5. **Logout** → returns to the Login screen.

### Seeded demo accounts

| Role     | Username  | Password       |
|----------|-----------|----------------|
| Citizen  | `citizen` | `Password@123` |
| Official | `official`| `Password@123` |

> Password policy for new accounts: at least 8 characters with an uppercase letter,
> a lowercase letter, a digit, and a special character.

---

## Prerequisites

Install these before running:

1. **JDK 21 or newer** — verify with `java -version`.
2. **Apache Maven** — verify with `mvn -v`.
3. **Oracle Database 21c XE** — running locally, with the listener on port `1521`
   and the `XEPDB1` pluggable database available.

---

## Setup

### 1. Create the database user

The app connects as `complaint_system` / `password123` to `XEPDB1`. Create that user once.
Connect as a DBA (e.g. `sqlplus / as sysdba`) and run:

```sql
ALTER SESSION SET CONTAINER = XEPDB1;

CREATE USER complaint_system IDENTIFIED BY password123;
GRANT CONNECT, RESOURCE TO complaint_system;
ALTER USER complaint_system QUOTA UNLIMITED ON USERS;

-- Optional: stop the password from expiring (avoids ORA-28002 warnings)
ALTER PROFILE DEFAULT LIMIT PASSWORD_LIFE_TIME UNLIMITED;
```

> You do **not** need to create tables manually — Hibernate (`hbm2ddl.auto=update`) creates
> the tables and sequences automatically on first run.

If your credentials or connection differ, edit
`src/main/resources/hibernate.cfg.xml`.

### 2. Configure email (OTP verification)

Copy the template and fill it in:

```bash
cp email.properties.example email.properties
```

Then choose one of:

- **Real emails (Brevo):** create a free account at <https://www.brevo.com>, verify a
  sender address, generate an API key, and set in `email.properties`:
  ```properties
  mail.mode=live
  brevo.api.key=xkeysib-your-key-here
  mail.from=your-verified-sender@example.com
  mail.from.name=Complaint Management System
  ```
- **Local testing without email:** set `mail.mode=simulate`. The OTP is then written to
  the console/log instead of being emailed, so you can complete signup without any account.

> `email.properties` is git-ignored, so your API key is never committed.

---

## Build & Run

From the project root:

```bash
# Run the app
mvn javafx:run

# Or build a jar
mvn clean package
```

If your default `java`/`mvn` is an older JDK, point Maven at a JDK 21+ first:

```powershell
# Windows PowerShell
$env:JAVA_HOME = "C:\Path\To\jdk-21-or-newer"
mvn javafx:run
```

Convenience scripts `run.ps1` and `build.ps1` are included (they pin `JAVA_HOME`);
update the JDK path inside them to match your machine.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `invalid target release: 21` | Your JDK is older than 21. Install JDK 21+ and set `JAVA_HOME` to it. |
| App fails to start / connection errors | Make sure Oracle XE is running and the `complaint_system` user exists in `XEPDB1`. |
| `ORA-28002: the password will expire` | Run `ALTER PROFILE DEFAULT LIMIT PASSWORD_LIFE_TIME UNLIMITED;` and reset the password. |
| Signup shows *"Email is not configured"* | Fill in `email.properties` (Brevo key + sender), or set `mail.mode=simulate`. |
| OTP email not received | Check Spam/Promotions; ensure `mail.from` exactly matches a **verified** Brevo sender. |
| `paging file too small` / out of memory | Free up disk space / RAM; the run is heap-capped but still needs headroom. |

---

## Project Structure

```
complaint-logging-system/
├── pom.xml                     Maven build + dependencies
├── run.ps1 / build.ps1         Convenience run/build scripts (JDK-pinned)
├── email.properties.example    Email config template (copy to email.properties)
└── src/main/
    ├── java/com/complaint/system/
    │   ├── MainApplication.java
    │   ├── controller/         FXML controllers
    │   ├── dao/                Hibernate data access
    │   ├── entity/             JPA entities
    │   └── util/               Helpers (email, OTP, session, validation, ...)
    └── resources/
        ├── fxml/               UI layouts
        ├── css/styles.css      Theme
        ├── hibernate.cfg.xml   DB / Hibernate config
        └── logback.xml         Logging config
```
