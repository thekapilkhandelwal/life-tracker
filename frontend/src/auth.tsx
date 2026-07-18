import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { api } from './api'
import type { User } from './types'

type AuthContextValue = {
  user: User | null
  loading: boolean
  refresh: () => Promise<void>
  logout: () => Promise<void>
  demoLogin: (email: string, name: string) => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  const refresh = useCallback(async () => {
    try {
      const me = await api.get<User>('/api/auth/me')
      setUser(me)
    } catch {
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void refresh()
  }, [refresh])

  const logout = useCallback(async () => {
    await api.post('/api/auth/logout')
    setUser(null)
  }, [])

  const demoLogin = useCallback(async (email: string, name: string) => {
    const me = await api.post<User>('/api/auth/demo-login', { email, name })
    setUser(me)
  }, [])

  const value = useMemo(
    () => ({ user, loading, refresh, logout, demoLogin }),
    [user, loading, refresh, logout, demoLogin],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
