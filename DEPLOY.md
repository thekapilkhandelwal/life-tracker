# Deploy Life Tracker (no credit card)

Avoid: Fly.io (billing), Koyeb (AI pivot), Back4App (blocked by MMT CCI).

## Option A — Hugging Face Spaces (recommended)

1. Open: https://huggingface.co/new-space  
2. Name: `life-tracker`  
3. SDK: **Docker**  
4. Visibility: Public  
5. Create Space, then **Settings → Connected Spaces / Files** or clone & push this repo, **or** Settings → sync with GitHub repo `thekapilkhandelwal/life-tracker`  
6. Space **Settings → Variables and secrets** add:

| Name | Value |
|------|--------|
| `MONGODB_URI` | Atlas connection string |
| `JWT_SECRET` | long random string |
| `FRONTEND_URL` | `https://thekapilkhandelwal-life-tracker.hf.space` (adjust to your Space URL) |
| `COOKIE_SECURE` | `true` |
| `COOKIE_SAME_SITE` | `None` |
| `DEMO_LOGIN_ENABLED` | `true` |
| `GOOGLE_OAUTH_ENABLED` | `false` until OAuth is ready |
| `PORT` | `8080` |

7. App port in Space README frontmatter is `8080` (already set in repo root README).  
8. After live, paste the `*.hf.space` URL here.

## Option B — Render

1. Open: https://render.com/deploy?repo=https://github.com/thekapilkhandelwal/life-tracker  
2. Use free web service from `render.yaml` / Docker (`backend/Dockerfile`)  
3. Set the same env vars as above (`FRONTEND_URL` = your `*.onrender.com` URL)

> Render may ask for a card for new accounts in some regions. If it does, use Hugging Face instead.

## Google OAuth (after you have a public HTTPS URL)

Redirect URI:

```text
https://<your-public-host>/login/oauth2/code/google
```

Then set `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_OAUTH_ENABLED=true`.

## Already published

- Repo: https://github.com/thekapilkhandelwal/life-tracker  
- Image: `ghcr.io/thekapilkhandelwal/life-tracker:latest`
