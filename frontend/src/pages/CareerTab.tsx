import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { api } from '../api'
import { Loader, PageLoader } from '../components/Loader'
import type { CareerItem, CareerItemType, Reminder } from '../types'

const types: CareerItemType[] = ['GOAL', 'TODO', 'LEARN']

export function CareerTab() {
  const [items, setItems] = useState<CareerItem[]>([])
  const [reminders, setReminders] = useState<Reminder[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [filter, setFilter] = useState<CareerItemType | 'ALL'>('ALL')

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [type, setType] = useState<CareerItemType>('TODO')
  const [priority, setPriority] = useState(2)

  const [subject, setSubject] = useState('Career reminder')
  const [body, setBody] = useState('Check your daily goals and learning list.')
  const [sendAt, setSendAt] = useState('')

  async function load() {
    setLoading(true)
    setError('')
    try {
      const [career, mails] = await Promise.all([
        api.get<CareerItem[]>('/api/career/items'),
        api.get<Reminder[]>('/api/reminders'),
      ])
      setItems(career)
      setReminders(mails)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load career data')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  const visible = useMemo(
    () => (filter === 'ALL' ? items : items.filter((item) => item.type === filter)),
    [items, filter],
  )

  async function addItem(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    try {
      await api.post('/api/career/items', {
        type,
        title,
        description,
        priority,
        completed: false,
        status: 'OPEN',
      })
      setTitle('')
      setDescription('')
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not add item')
    } finally {
      setSaving(false)
    }
  }

  async function toggleComplete(item: CareerItem) {
    setSaving(true)
    try {
      await api.put(`/api/career/items/${item.id}`, {
        ...item,
        completed: !item.completed,
        status: !item.completed ? 'DONE' : 'OPEN',
      })
      await load()
    } finally {
      setSaving(false)
    }
  }

  async function removeItem(id: string) {
    setSaving(true)
    try {
      await api.del(`/api/career/items/${id}`)
      await load()
    } finally {
      setSaving(false)
    }
  }

  async function onUpload(file: File) {
    setSaving(true)
    setError('')
    try {
      const text = await file.text()
      let rows: Array<Record<string, string>> = []
      if (file.name.endsWith('.json')) {
        const parsed = JSON.parse(text) as unknown
        rows = Array.isArray(parsed) ? parsed : []
      } else {
        const lines = text.split(/\r?\n/).filter(Boolean)
        if (lines.length < 2) throw new Error('CSV needs a header row and at least one data row')
        const headers = lines[0].split(',').map((h) => h.trim())
        rows = lines.slice(1).map((line) => {
          const cols = line.split(',').map((c) => c.trim())
          const row: Record<string, string> = {}
          headers.forEach((header, index) => {
            row[header] = cols[index] ?? ''
          })
          return row
        })
      }
      await api.post('/api/career/import', rows)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Import failed')
    } finally {
      setSaving(false)
    }
  }

  async function scheduleReminder(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    try {
      await api.post('/api/reminders', {
        subject,
        body,
        sendAt: new Date(sendAt).toISOString(),
      })
      setSendAt('')
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not schedule reminder')
    } finally {
      setSaving(false)
    }
  }

  async function sendNow(id: string) {
    setSaving(true)
    try {
      await api.post(`/api/reminders/${id}/send-now`)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Send failed — configure SMTP')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <PageLoader label="Loading career workspace…" />

  return (
    <div className="tab-grid">
      <section className="panel">
        <div className="panel-head">
          <h2>Daily focus</h2>
          {saving ? <Loader label="Updating…" /> : null}
        </div>
        {error ? <p className="error">{error}</p> : null}

        <div className="chip-row">
          {(['ALL', ...types] as const).map((item) => (
            <button
              key={item}
              className={`chip ${filter === item ? 'active' : ''}`}
              onClick={() => setFilter(item)}
              type="button"
            >
              {item}
            </button>
          ))}
        </div>

        <ul className="list">
          {visible.map((item) => (
            <li key={item.id} className={item.completed ? 'done' : ''}>
              <div>
                <strong>{item.title}</strong>
                <span className="muted">{item.type} · P{item.priority}{item.description ? ` · ${item.description}` : ''}</span>
              </div>
              <div className="row-actions">
                <button className="linkish" onClick={() => void toggleComplete(item)}>
                  {item.completed ? 'Reopen' : 'Done'}
                </button>
                <button className="linkish" onClick={() => void removeItem(item.id)}>Delete</button>
              </div>
            </li>
          ))}
        </ul>
      </section>

      <section className="panel">
        <h2>Add work item</h2>
        <form className="stack" onSubmit={addItem}>
          <label>
            Type
            <select value={type} onChange={(e) => setType(e.target.value as CareerItemType)}>
              {types.map((item) => <option key={item} value={item}>{item}</option>)}
            </select>
          </label>
          <label>
            Title
            <input value={title} onChange={(e) => setTitle(e.target.value)} required placeholder="Ship feature X / Learn Kafka" />
          </label>
          <label>
            Description
            <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} />
          </label>
          <label>
            Priority (1 high – 5 low)
            <input type="number" min={1} max={5} value={priority} onChange={(e) => setPriority(Number(e.target.value))} />
          </label>
          <button className="btn btn-primary" type="submit" disabled={saving}>Add to list</button>
        </form>

        <h3 className="mt">Upload CSV / JSON</h3>
        <p className="muted">Columns: type, title, description, priority, status, completed</p>
        <input
          type="file"
          accept=".csv,.json"
          onChange={(e) => {
            const file = e.target.files?.[0]
            if (file) void onUpload(file)
          }}
        />
      </section>

      <section className="panel wide">
        <h2>Email reminders</h2>
        <form className="form-grid" onSubmit={scheduleReminder}>
          <label>
            Subject
            <input value={subject} onChange={(e) => setSubject(e.target.value)} required />
          </label>
          <label>
            Send at
            <input type="datetime-local" value={sendAt} onChange={(e) => setSendAt(e.target.value)} required />
          </label>
          <label className="span-2">
            Body
            <textarea value={body} onChange={(e) => setBody(e.target.value)} rows={3} required />
          </label>
          <button className="btn btn-primary" type="submit" disabled={saving}>Schedule to my mailbox</button>
        </form>

        <ul className="list mt">
          {reminders.map((reminder) => (
            <li key={reminder.id}>
              <div>
                <strong>{reminder.subject}</strong>
                <span className="muted">
                  {new Date(reminder.sendAt).toLocaleString()} · {reminder.sent ? 'Sent' : 'Queued'}
                  {reminder.errorMessage ? ` · ${reminder.errorMessage}` : ''}
                </span>
              </div>
              {!reminder.sent ? (
                <button className="linkish" onClick={() => void sendNow(reminder.id)}>Send now</button>
              ) : null}
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
