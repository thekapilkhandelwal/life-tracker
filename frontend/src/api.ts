const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(init.headers ?? {}),
    },
  })

  if (!response.ok) {
    let message = `Request failed (${response.status})`
    try {
      const body = await response.json()
      if (body?.error) message = body.error
    } catch {
      // ignore parse errors
    }
    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  return text ? (JSON.parse(text) as T) : (undefined as T)
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  del: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
  googleLoginUrl: () => `${API_BASE}/oauth2/authorization/google`,
}
