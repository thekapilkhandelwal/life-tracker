# Life Tracker — Architecture & Technology Decisions

This document describes the backend architecture, technology choices, data model, security, integrations, and deployment of **Life Tracker**. It intentionally omits frontend React implementation details. The UI is treated only as static assets served by the API for a single deployable unit.

---

## 1. Purpose

Life Tracker is a personal life-management backend that consolidates three domains:

| Domain | Capability |
|--------|------------|
| **Finance** | Salary targets, expenses, investments/savings, goal amount, overspend flags, advice |
| **Career** | Goals, daily todos, learning items, CSV/JSON import, email reminders |
| **Travel** | Multi-page Notion-like notes persisted in the database |

Cross-cutting concerns: authentication (Google OAuth + session cookie), scheduling for reminders, and health monitoring for hosting.

**Live production (current):** `https://life-tracker-xwdz.onrender.com`  
**Source:** `https://github.com/thekapilkhandelwal/life-tracker`

---

## 2. High-level architecture

```text
┌─────────────────────────────────────────────────────────────┐
│                     Client (browser)                        │
│         Static UI + API calls (credentials: include)        │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTPS
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              Render Web Service (Docker)                    │
│  ┌───────────────────────────────────────────────────────┐  │
│  │         Spring Boot 3.4 (Java 21)                     │  │
│  │  • Security + OAuth2 Client (Google)                  │  │
│  │  • JWT in httpOnly cookie                             │  │
│  │  • REST controllers (finance / career / travel)       │  │
│  │  • Services + repositories                            │  │
│  │  • Mail sender + scheduled reminder dispatch          │  │
│  │  • Actuator health                                    │  │
│  │  • Serves packaged static UI from /static             │  │
│  └───────────────────────────┬───────────────────────────┘  │
└──────────────────────────────┼──────────────────────────────┘
                               │
               ┌───────────────┼───────────────┐
               ▼               ▼               ▼
        MongoDB Atlas     Google OAuth      SMTP (optional)
        (primary DB)      (identity)        Gmail App Password
```

### Why a single deployable (API + static UI)

- One URL, one cookie domain → simpler auth and CORS.
- Free hosting (Render) is easier with one service.
- No separate frontend host required for production.

---

## 3. Technology stack (backend & platform)

| Layer | Technology | Version / notes | Why chosen |
|-------|------------|-----------------|------------|
| Language | **Java** | 21 (LTS) | Strong typing, mature ecosystem, team familiarity |
| Framework | **Spring Boot** | 3.4.5 | Fast to build secure REST APIs; first-class OAuth2, Mongo, Mail, Actuator |
| Build | **Maven** | via Spring Boot parent | Standard Java packaging; reproducible Docker builds |
| Security | **Spring Security** | Boot starter | Filter chain, OAuth2 login, CORS, authorization |
| Identity | **Google OAuth 2.0** | Authorization Code via Spring OAuth2 Client | User asked for Google login; no password storage |
| Session | **JWT (JJWT 0.12.6)** in **httpOnly cookie** | 30-day expiry | Stay logged in without server session store; cookie resists XSS vs `localStorage` |
| Persistence | **MongoDB** via **Spring Data MongoDB** | Atlas free M0 | Flexible documents for travel pages & mixed career items; easy schema evolution |
| Validation | **Jakarta Validation** | `@NotNull`, `@NotBlank`, etc. | Fail fast at API boundary |
| Email | **Spring Mail (JavaMail)** | SMTP | Career reminders to the user’s mailbox |
| Scheduling | **Spring `@Scheduled`** | Poll due reminders | Simple in-process scheduler for hobby scale |
| Health | **Spring Actuator** | `/actuator/health` | Required by Render health checks |
| Boilerplate | **Lombok** | 1.18.38 | `@Data`, `@Builder`, `@RequiredArgsConstructor` |
| Container | **Docker** (multi-stage Maven → JRE 21) | — | Same artifact locally and on Render |
| Registry | **GHCR** | `ghcr.io/thekapilkhandelwal/life-tracker` | Public image for portable deploys |
| Hosting | **Render** (free web service) | Docker runtime | Free HTTPS, GitHub deploy, env secrets; no need for separate FE host |
| Database hosting | **MongoDB Atlas** | Free tier | Managed Mongo, global connectivity from Render |
| CI | **GitHub Actions** | Publish image to GHCR | Build without local Docker when needed |

### Why not alternatives (brief)

| Alternative | Why not (for this project) |
|-------------|----------------------------|
| PostgreSQL / JPA | Fine, but travel pages + flexible career import map more naturally to documents |
| Server-side sessions (Redis) | Extra infra on free tier; JWT cookie is enough for one-user app |
| Separate Node API | User preference / Java conventions; Spring OAuth2 is mature |
| Fly.io | Required billing for new orgs |
| Koyeb | Product pivot away from general free app hosting |
| Back4App | Blocked by corporate CCI filter on MMT network |

---

## 4. Backend package structure

Layered design:

```text
com.lifetracker
├── LifeTrackerApplication          # Boot entry + @EnableScheduling
├── config
│   ├── AppProperties               # Typed config (JWT, cookies, OAuth flags)
│   └── SecurityConfig              # Filter chain, CORS, OAuth2 login gate
├── domain                          # Mongo documents / enums
├── repository                      # Spring Data Mongo repositories
├── security
│   ├── JwtService                  # Create / parse JWT
│   ├── JwtCookieAuthFilter         # Read cookie → SecurityContext
│   ├── OAuth2LoginSuccessHandler   # Google success → user upsert + cookie
│   └── UserPrincipal               # Authenticated user in context
├── service                         # Business logic
├── web
│   ├── *Controller                 # Thin HTTP adapters
│   ├── SpaForwardController        # Forward /app/** → index.html
│   ├── AuthSupport                 # Resolve current UserPrincipal
│   ├── GlobalExceptionHandler      # Consistent JSON errors
│   └── dto                         # Request/response records
```

**Flow:** `Controller → Service → Repository`  
Controllers stay thin; services own rules (finance advice, career import, reminder dispatch).

---

## 5. Domain model (MongoDB)

Collections (logical):

### `users`
- Identity after Google or demo login  
- Fields: `email` (unique), `name`, `pictureUrl`, `googleId`, timestamps  

### `finance_profiles`
- One profile per user  
- Monthly salary, ideal % splits (needs / wants / invest / savings), goal amount & label  

### `expenses`
- Day-to-day money flow  
- `title`, `amount`, `category` (`NEEDS|WANTS|INVESTMENT|SAVINGS|OTHER`), `spentOn`, `note`  

### `career_items`
- Typed work items: `GOAL | TODO | LEARN`  
- Priority, status, completion, optional due date  

### `reminders`
- Queued emails: `subject`, `body`, `sendAt`, `sent`, `errorMessage`  

### `travel_pages`
- Notion-like pages: `title`, `icon`, `content` (markdown/text), `tags`, `archived`  

**Why MongoDB fits:**  
Travel content is free-form; career import is schema-flexible; finance docs are simple aggregates—no heavy relational joins required for a single-user product.

---

## 6. API surface

Base URL (production): `https://life-tracker-xwdz.onrender.com`

| Area | Methods | Paths |
|------|---------|--------|
| Auth | `GET` | `/api/auth/me`, `/api/auth/config` |
| Auth | `POST` | `/api/auth/demo-login`, `/api/auth/logout` |
| OAuth | browser | `/oauth2/authorization/google` → Google → `/login/oauth2/code/google` |
| Finance | `GET/PUT` | `/api/finance/profile`, `/api/finance/overview` |
| Finance | CRUD | `/api/finance/expenses` |
| Career | CRUD | `/api/career/items` |
| Career | `POST` | `/api/career/import` (list of row maps) |
| Reminders | CRUD + send | `/api/reminders`, `/api/reminders/{id}/send-now` |
| Travel | CRUD | `/api/travel/pages` |
| Ops | `GET` | `/actuator/health` |

All domain APIs require an authenticated principal (JWT cookie), except auth bootstrap endpoints and static assets.

---

## 7. Security architecture

### 7.1 Google OAuth 2.0 (Authorization Code)

1. User hits `/oauth2/authorization/google`.  
2. Google authenticates and redirects to `/login/oauth2/code/google`.  
3. `OAuth2LoginSuccessHandler` upserts `User`, issues JWT, sets httpOnly cookie, redirects to frontend (`FRONTEND_URL` / `/app`).  

**Why OAuth2 Client (not resource server only):** Interactive browser login is the product requirement.

**Production OAuth settings:**
- Client ID / Secret from Google Cloud Console  
- Redirect URI: `https://life-tracker-xwdz.onrender.com/login/oauth2/code/google`  
- `server.forward-headers-strategy=framework` so Spring builds **https** redirect URIs behind Render’s proxy  

### 7.2 JWT cookie session

- Cookie name: `lt_session` (configurable)  
- httpOnly: yes (JS cannot read)  
- Secure + SameSite configurable (`None` + `Secure` for HTTPS production)  
- Expiry: 30 days (configurable)  
- Validated on each request by `JwtCookieAuthFilter`  

**Why JWT in cookie (not bearer header):** Matches “don’t log in again and again” with browser-native cookie send; no token storage in frontend code.

### 7.3 Demo login

- `POST /api/auth/demo-login` when `DEMO_LOGIN_ENABLED=true`  
- Useful before Google credentials exist or for quick testing  
- Same JWT cookie path as OAuth  

### 7.4 Feature flag: `GOOGLE_OAUTH_ENABLED`

- When `false`, OAuth2 login is disabled in the security filter chain and UI hides Google button  
- Prevents `invalid_client` when placeholders were used  

### 7.5 CORS

- Allowed origin(s) from `FRONTEND_URL` (comma-separated supported)  
- `allowCredentials=true` for cookies  
- In production (same origin static+API), CORS is largely unused but kept for local split-dev  

---

## 8. Feature modules (backend logic)

### 8.1 Finance

`FinanceService` responsibilities:

- Maintain salary + ideal allocation percentages (default 50/30/15/5 style).  
- Track expenses by category for the current month.  
- Compare actual vs ideal → **spending flags** (`ALERT` / `WARNING`).  
- Produce short **advice** list (pay yourself first, emergency fund, cut flagged categories).  
- Goal progress from savings + investment amounts vs goal target.  

**Why in application code (not a rules engine):** Personal app; rules are transparent and easy to change.

### 8.2 Career

`CareerService` responsibilities:

- CRUD for typed items (goal / todo / learn).  
- Bulk **import** from CSV/JSON rows (`type`, `title`, `description`, `priority`, …).  
- Soft defaults for missing fields.  

`ReminderService`:

- Persist scheduled reminders.  
- `@Scheduled` poll sends due mail via SMTP when configured.  
- `send-now` for immediate dispatch.  
- Graceful skip if mail is not configured (`ObjectProvider<JavaMailSender>`).  

### 8.3 Travel

`TravelService`:

- Multi-page CRUD, user-scoped.  
- Free-text `content` stored in Mongo (Notion-like notebook semantics without a CRDT editor on the server).  

---

## 9. Configuration & secrets

Configuration is externalized via environment variables (12-factor style).

| Variable | Role |
|----------|------|
| `MONGODB_URI` | Atlas connection string |
| `JWT_SECRET` | HMAC key for JWT (≥ 256-bit) |
| `FRONTEND_URL` | Post-login redirect + CORS origin |
| `PUBLIC_BASE_URL` | Public https base for OAuth redirect display / helpers |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth app |
| `GOOGLE_OAUTH_ENABLED` | Gate Google login |
| `COOKIE_SECURE` / `COOKIE_SAME_SITE` | Cookie attributes |
| `DEMO_LOGIN_ENABLED` | Allow email demo login |
| `MAIL_*` | Optional SMTP for reminders |
| `PORT` | Set by Render |

Secrets live in Render dashboard / API — **not** in git.

---

## 10. Deployment architecture

```text
GitHub (main)
    │
    ├─► Render auto/manual deploy (Dockerfile in backend/)
    │       • Build Maven inside Docker
    │       • Run JRE 21 jar
    │       • Health check: GET /actuator/health
    │
    └─► GitHub Actions (optional): publish image to GHCR
```

### Docker

Multi-stage:

1. `maven:3.9.9-eclipse-temurin-21` → `mvn package`  
2. `eclipse-temurin:21-jre` → run jar with constrained `JAVA_TOOL_OPTIONS` for free-tier RAM  

### Why Render

- Free HTTPS web service suitable for a Spring Boot jar/Docker app.  
- Env var management via dashboard/API.  
- GitHub integration for redeploys on push.  

### Runtime notes

- Free tier may **spin down** when idle → first request can be slow (cold start).  
- MongoDB Atlas must allow network access from Render (typically `0.0.0.0/0` on free tier with strong DB user password).  

---

## 11. Observability & operations

| Concern | Approach |
|---------|----------|
| Liveness | Actuator `/actuator/health` |
| Logs | stdout → Render log stream |
| Errors | `GlobalExceptionHandler` → JSON `{ "error": "..." }` |
| Auth failures | `401` via security entry point / unauthorized helper |

No APM/metrics stack was added (out of scope for personal free-tier app).

---

## 12. Design principles applied

1. **Constructor injection** (`@RequiredArgsConstructor`) — testable, immutable dependencies.  
2. **Optional chaining** where nullability is expected (e.g. OAuth attributes, mail sender).  
3. **Records for DTOs / config** — immutability at boundaries.  
4. **Thin controllers** — HTTP mapping only.  
5. **User-scoped data** — every query filters by `userId` from JWT principal.  
6. **Fail at edges** — validation annotations + domain exceptions.  

---

## 13. Threat model (lightweight)

| Risk | Mitigation |
|------|------------|
| XSS stealing session | httpOnly cookie |
| CSRF | SameSite cookie; SPA same-origin production |
| Token theft via MitM | HTTPS + `Secure` cookie |
| Open Google client | Real client ID/secret; OAuth feature flag |
| Data isolation | Always filter by authenticated `userId` |
| Secrets in repo | Env vars only; `.env.local` gitignored |

---

## 14. Future extensions (backend-oriented)

- Move reminder scheduler to a managed job / queue if multi-instance.  
- Refresh-token rotation / shorter JWT + sliding expiry.  
- Audit log collection for finance changes.  
- Stronger money rules engine (budgets per subcategory).  
- Attachments for travel pages (object storage).  
- Disable demo login in production permanently.  

---

## 15. Quick reference — key components

| Component | Class / resource |
|-----------|------------------|
| Security filter chain | `SecurityConfig` |
| JWT create/parse | `JwtService` |
| Cookie auth | `JwtCookieAuthFilter` |
| Google success | `OAuth2LoginSuccessHandler` |
| Finance rules | `FinanceService` |
| Career + import | `CareerService` |
| Reminders + mail | `ReminderService` |
| Travel pages | `TravelService` |
| App config | `AppProperties` + `application.yml` |
| Container | `backend/Dockerfile` |
| Deploy blueprint | `render.yaml` |

---

*Document version: 1.0 — reflects the Spring Boot + MongoDB Atlas + Render deployment of Life Tracker.*
