import { Navigate, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth'
import { Loader } from '../components/Loader'

export function AppShell() {
  const { user, loading, logout } = useAuth()

  if (loading) {
    return (
      <div className="page-loader">
        <Loader label="Restoring your session…" />
      </div>
    )
  }

  if (!user) return <Navigate to="/" replace />

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="brand">Life Tracker</p>
          <p className="muted small">Hello, {user.name}</p>
        </div>
        <nav className="tabs">
          <NavLink to="/app/finance">Money</NavLink>
          <NavLink to="/app/career">Career</NavLink>
          <NavLink to="/app/travel">Travel</NavLink>
        </nav>
        <button className="btn btn-ghost" type="button" onClick={() => void logout()}>
          Log out
        </button>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
