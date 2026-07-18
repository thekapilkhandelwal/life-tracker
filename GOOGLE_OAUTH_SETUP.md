# Fix: `Error 401: invalid_client` / OAuth client was not found

This happens when the API has no real Google OAuth client (placeholder `changeme` values).

## Create Google OAuth credentials (5 minutes)

1. Open [Google Cloud Console → Credentials](https://console.cloud.google.com/apis/credentials)
2. Select or create a project (e.g. `life-tracker`)
3. **Configure OAuth consent screen** (External is fine for personal use)
   - App name: `Life Tracker`
   - User support email: `thekapilkhandelwal@gmail.com`
   - Developer contact: `thekapilkhandelwal@gmail.com`
4. **Create credentials → OAuth client ID**
   - Application type: **Web application**
   - Name: `Life Tracker Web`
   - Authorized JavaScript origins:
     - `http://localhost:5173`
     - `http://localhost:8081`
     - your Netlify URL (after deploy), e.g. `https://something.netlify.app`
   - Authorized redirect URIs:
     - `http://localhost:8081/login/oauth2/code/google`
     - `https://<your-render-api>.onrender.com/login/oauth2/code/google`
5. Copy **Client ID** and **Client Secret**

## Set on the API (local)

```bash
export GOOGLE_CLIENT_ID='....apps.googleusercontent.com'
export GOOGLE_CLIENT_SECRET='....'
export GOOGLE_OAUTH_ENABLED=true
export FRONTEND_URL='http://localhost:5173'
```

Then restart the backend. The Google button will appear on the login page.

## Set on Render (production)

Environment variables:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_OAUTH_ENABLED=true`
- `FRONTEND_URL=https://<your-netlify-site>.netlify.app`
- `COOKIE_SECURE=true`
- `COOKIE_SAME_SITE=None`
