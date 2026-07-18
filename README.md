# Life Tracker

Personal life OS with three workspaces:

1. **Money** — salary targets, day-to-day expenses, investment tracking, goal amount, overspend flags, and 50/30/15/5 style advice  
2. **Career** — goals / daily todos / learning list, CSV·JSON import, email reminders to your mailbox  
3. **Travel** — Notion-like multi-page notebook stored in MongoDB  

Auth: **Google OAuth** + **httpOnly session cookie** (30 days). Demo login available for local use.

## Stack

| Layer | Tech |
|-------|------|
| Frontend | React + Vite + TypeScript (Netlify) |
| Backend | Spring Boot 3 + Security OAuth2 (Render) |
| Database | MongoDB Atlas (free) |

## Local run

### 1. MongoDB

```bash
docker compose up -d
```

### 2. Backend

```bash
cd backend
# optional: export GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET
mvn spring-boot:run
```

API: `http://localhost:8080`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

App: `http://localhost:5173`

Vite proxies `/api` and `/oauth2` to the backend, so cookies work on localhost without CORS pain. Use **Enter demo workspace** if Google OAuth is not configured yet.

### Google OAuth (local + production)

1. Create credentials in [Google Cloud Console](https://console.cloud.google.com/apis/credentials)  
2. Authorized redirect URI:
   - Local: `http://localhost:8080/login/oauth2/code/google`
   - Prod: `https://<your-render-api>.onrender.com/login/oauth2/code/google`
3. Set `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and `FRONTEND_URL`

### Email reminders

Set Gmail (or any SMTP) env vars:

```bash
export MAIL_USERNAME=you@gmail.com
export MAIL_PASSWORD=your-app-password
export MAIL_FROM=you@gmail.com
```

Use a Google [App Password](https://myaccount.google.com/apppasswords) if 2FA is on.

## Free hosting

### A. MongoDB Atlas

1. Create a free M0 cluster  
2. Database user + network access `0.0.0.0/0`  
3. Copy connection string → `MONGODB_URI`

### B. Backend on Render

1. Push this repo to GitHub  
2. New → Blueprint → select repo (`render.yaml`) **or** Web Service from `backend/Dockerfile`  
3. Set env vars from `render.yaml` (`MONGODB_URI`, Google, `FRONTEND_URL`, mail)  
4. Deploy → note the API URL, e.g. `https://life-tracker-api.onrender.com`

Free Render dynos sleep after idle; first request may take ~30–60s (frontend loaders cover this).

### C. Frontend on Netlify

1. New site from Git → `frontend/` as base directory  
2. Build: `npm run build`, publish: `dist`  
3. Env var: `VITE_API_BASE_URL=https://<your-render-api>.onrender.com`  
4. Deploy  

`netlify.toml` already handles SPA redirects.

### Production cookie note

Cross-site cookies require:

- `COOKIE_SECURE=true`
- `COOKIE_SAME_SITE=None`
- HTTPS on both Netlify and Render

## API map

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/auth/me` | Current user |
| POST | `/api/auth/demo-login` | Local/demo session |
| POST | `/api/auth/logout` | Clear cookie |
| GET | `/oauth2/authorization/google` | Start Google login |
| GET/PUT | `/api/finance/profile` | Salary & targets |
| GET | `/api/finance/overview` | Flow + flags + advice |
| CRUD | `/api/finance/expenses` | Money entries |
| CRUD | `/api/career/items` | Goals / todos / learn |
| POST | `/api/career/import` | Bulk upload rows |
| CRUD | `/api/reminders` | Mailbox reminders |
| CRUD | `/api/travel/pages` | Travel notebook |

## Sample career CSV

```csv
type,title,description,priority,status,completed
GOAL,Become staff engineer,Lead 2 cross-team designs,1,OPEN,false
TODO,Write design doc,Async processing RFC,2,OPEN,false
LEARN,System design,Grokking chapter 4,3,OPEN,false
```
