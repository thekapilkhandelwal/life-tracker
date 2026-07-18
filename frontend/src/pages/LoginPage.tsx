import { useEffect, useState, type FormEvent } from 'react'
import { Navigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { Loader } from '../components/Loader'

type AuthConfig = {
  googleOAuthEnabled: boolean
  demoLoginEnabled: boolean
  googleLoginPath: string
}

export function LoginPage() {
  const { user, loading, demoLogin } = useAuth()
  const [email, setEmail] = useState('thekapilkhandelwal@gmail.com')
  const [name, setName] = useState('Kapil')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [config, setConfig] = useState<AuthConfig | null>(null)

  useEffect(() => {
    api.get<AuthConfig>('/api/auth/config')
      .then(setConfig)
      .catch(() => setConfig({
        googleOAuthEnabled: false,
        demoLoginEnabled: true,
        googleLoginPath: '/oauth2/authorization/google',
      }))
  }, [])

  if (loading) return <div className="page-loader"><Loader label="Checking session…" /></div>
  if (user) return <Navigate to="/app" replace />

  async function onDemoLogin(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      await demoLogin(email, name)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="login-shell">
      <div className="login-panel">
        <p className="brand">Life Tracker</p>
        <h1>One place for money, career, and travel.</h1>
        <p className="muted">
          Minimal workspace with Google sign-in, persistent cookies, and a Mongo-backed Notion-style travel notebook.
        </p>

        {config?.googleOAuthEnabled ? (
          <a className="btn btn-primary btn-block" href={api.googleLoginUrl()}>
            Continue with Google
          </a>
        ) : (
          <div className="flag warning" style={{ marginBottom: '1rem' }}>
            <strong>Google sign-in not configured yet</strong>
            <p>
              The previous error (<code>invalid_client</code>) means Google OAuth credentials are missing.
              Create an OAuth client in Google Cloud Console, then set
              {' '}<code>GOOGLE_CLIENT_ID</code>, <code>GOOGLE_CLIENT_SECRET</code>, and
              {' '}<code>GOOGLE_OAUTH_ENABLED=true</code> on the API.
            </p>
            <p className="muted" style={{ marginBottom: 0 }}>
              Redirect URI must be: <code>{`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'}/login/oauth2/code/google`}</code>
            </p>
          </div>
        )}

        {config?.demoLoginEnabled !== false ? (
          <>
            <div className="divider"><span>{config?.googleOAuthEnabled ? 'or use email login' : 'continue with email'}</span></div>
            <form className="stack" onSubmit={onDemoLogin}>
              <label>
                Name
                <input value={name} onChange={(e) => setName(e.target.value)} required />
              </label>
              <label>
                Email
                <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
              </label>
              {error ? <p className="error">{error}</p> : null}
              <button className="btn btn-ghost btn-block" type="submit" disabled={busy}>
                {busy ? <Loader label="Signing in…" /> : 'Enter workspace'}
              </button>
            </form>
          </>
        ) : null}
      </div>
    </div>
  )
}
