# Deploy Life Tracker (no credit card)

Koyeb’s free web hosting is no longer a good fit (product is shifting to Mistral AI). Use **Back4App Containers** instead.

## 1. Image / repo (already done)

- GitHub: https://github.com/thekapilkhandelwal/life-tracker
- Docker image: `ghcr.io/thekapilkhandelwal/life-tracker:latest` (public)

## 2. Deploy on Back4App (free, no card)

1. Sign up: https://www.back4app.com/  
2. Dashboard → **Containers** → **New App** → connect GitHub  
3. Select repo `thekapilkhandelwal/life-tracker`  
4. Settings:
   - **Root directory:** `backend`
   - **Dockerfile path:** `Dockerfile` (inside `backend`)
5. Environment variables:

| Key | Value |
|-----|--------|
| `MONGODB_URI` | your Atlas URI |
| `JWT_SECRET` | long random string |
| `FRONTEND_URL` | Back4App URL after first deploy (update once you have it) |
| `COOKIE_SECURE` | `true` |
| `COOKIE_SAME_SITE` | `None` |
| `DEMO_LOGIN_ENABLED` | `true` |
| `GOOGLE_OAUTH_ENABLED` | `false` initially |
| `PORT` | `8080` |

6. Deploy → copy the public URL (looks like `https://….b4a.app`)

7. Set `FRONTEND_URL` to that URL and redeploy once.

## 3. Google OAuth (after URL is live)

In [Google Cloud Console → Credentials](https://console.cloud.google.com/apis/credentials):

- Authorized redirect URI:  
  `https://<your-b4a-url>/login/oauth2/code/google`

Then set on Back4App:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_OAUTH_ENABLED=true`

Send those two values here if you want them wired for you.
