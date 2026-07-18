import { useEffect, useState, type FormEvent } from 'react'
import { api } from '../api'
import { Loader, PageLoader } from '../components/Loader'
import type { TravelPage } from '../types'

export function TravelTab() {
  const [pages, setPages] = useState<TravelPage[]>([])
  const [activeId, setActiveId] = useState<string | null>(null)
  const [draft, setDraft] = useState<TravelPage | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function load(selectId?: string) {
    setLoading(true)
    setError('')
    try {
      const data = await api.get<TravelPage[]>('/api/travel/pages')
      setPages(data)
      const nextId = selectId ?? activeId ?? data[0]?.id ?? null
      setActiveId(nextId)
      if (nextId) {
        const page = data.find((item) => item.id === nextId) ?? await api.get<TravelPage>(`/api/travel/pages/${nextId}`)
        setDraft(page)
      } else {
        setDraft(null)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load pages')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function createPage() {
    setSaving(true)
    try {
      const page = await api.post<TravelPage>('/api/travel/pages', {
        title: 'Untitled trip',
        icon: '✈',
        content: '# Destination\n\n- Flights\n- Stay\n- Things to do\n',
        tags: [],
      })
      await load(page.id)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not create page')
    } finally {
      setSaving(false)
    }
  }

  async function savePage(event?: FormEvent) {
    event?.preventDefault()
    if (!draft?.id) return
    setSaving(true)
    try {
      const updated = await api.put<TravelPage>(`/api/travel/pages/${draft.id}`, {
        title: draft.title,
        icon: draft.icon,
        content: draft.content,
        tags: draft.tags ?? [],
      })
      setDraft(updated)
      setPages((prev) => prev.map((page) => (page.id === updated.id ? updated : page)))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save page')
    } finally {
      setSaving(false)
    }
  }

  async function removePage(id: string) {
    setSaving(true)
    try {
      await api.del(`/api/travel/pages/${id}`)
      setActiveId(null)
      await load()
    } finally {
      setSaving(false)
    }
  }

  async function openPage(id: string) {
    setActiveId(id)
    setSaving(true)
    try {
      const page = await api.get<TravelPage>(`/api/travel/pages/${id}`)
      setDraft(page)
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <PageLoader label="Opening travel notebook…" />

  return (
    <div className="travel-layout">
      <aside className="travel-sidebar panel">
        <div className="panel-head">
          <h2>Pages</h2>
          <button className="btn btn-ghost" type="button" onClick={() => void createPage()} disabled={saving}>
            New
          </button>
        </div>
        {error ? <p className="error">{error}</p> : null}
        <ul className="page-list">
          {pages.map((page) => (
            <li key={page.id}>
              <button
                type="button"
                className={`page-link ${activeId === page.id ? 'active' : ''}`}
                onClick={() => void openPage(page.id)}
              >
                <span>{page.icon || '✈'}</span>
                <span>{page.title}</span>
              </button>
            </li>
          ))}
        </ul>
      </aside>

      <section className="panel travel-editor">
        {!draft ? (
          <div className="empty">
            <p>Create a page for each trip — notes, budgets, packing lists, all stored in MongoDB.</p>
            <button className="btn btn-primary" type="button" onClick={() => void createPage()}>Create first page</button>
          </div>
        ) : (
          <form className="stack" onSubmit={(e) => void savePage(e)}>
            <div className="panel-head">
              <div className="title-row">
                <input
                  className="icon-input"
                  value={draft.icon ?? ''}
                  onChange={(e) => setDraft({ ...draft, icon: e.target.value })}
                  aria-label="Icon"
                />
                <input
                  className="title-input"
                  value={draft.title}
                  onChange={(e) => setDraft({ ...draft, title: e.target.value })}
                  required
                />
              </div>
              <div className="row-actions">
                {saving ? <Loader label="Saving…" /> : null}
                <button className="btn btn-primary" type="submit" disabled={saving}>Save</button>
                <button className="linkish" type="button" onClick={() => void removePage(draft.id)}>Delete</button>
              </div>
            </div>
            <textarea
              className="notion-editor"
              value={draft.content}
              onChange={(e) => setDraft({ ...draft, content: e.target.value })}
              placeholder="Write anything — itinerary, links, packing list…"
              rows={22}
            />
          </form>
        )}
      </section>
    </div>
  )
}
