import { useEffect, useState, type FormEvent } from 'react'
import { api } from '../api'
import { Loader, PageLoader } from '../components/Loader'
import type { ExpenseCategory, FinanceOverview, FinanceProfile } from '../types'

const categories: ExpenseCategory[] = ['NEEDS', 'WANTS', 'INVESTMENT', 'SAVINGS', 'OTHER']

function money(value?: number) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(value ?? 0)
}

export function FinanceTab() {
  const [overview, setOverview] = useState<FinanceOverview | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const [title, setTitle] = useState('')
  const [amount, setAmount] = useState('')
  const [category, setCategory] = useState<ExpenseCategory>('WANTS')
  const [note, setNote] = useState('')

  const [profile, setProfile] = useState<FinanceProfile | null>(null)

  async function load() {
    setLoading(true)
    setError('')
    try {
      const data = await api.get<FinanceOverview>('/api/finance/overview')
      setOverview(data)
      setProfile(data.profile)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load finance')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  async function addExpense(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    try {
      await api.post('/api/finance/expenses', {
        title,
        amount: Number(amount),
        category,
        note,
        spentOn: new Date().toISOString().slice(0, 10),
      })
      setTitle('')
      setAmount('')
      setNote('')
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not add expense')
    } finally {
      setSaving(false)
    }
  }

  async function saveProfile(event: FormEvent) {
    event.preventDefault()
    if (!profile) return
    setSaving(true)
    try {
      await api.put('/api/finance/profile', {
        ...profile,
        monthlySalary: Number(profile.monthlySalary),
        idealNeedsPercent: Number(profile.idealNeedsPercent),
        idealWantsPercent: Number(profile.idealWantsPercent),
        idealInvestPercent: Number(profile.idealInvestPercent),
        idealSavingsPercent: Number(profile.idealSavingsPercent),
        goalAmount: Number(profile.goalAmount),
      })
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save profile')
    } finally {
      setSaving(false)
    }
  }

  async function removeExpense(id: string) {
    setSaving(true)
    try {
      await api.del(`/api/finance/expenses/${id}`)
      await load()
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <PageLoader label="Loading money flow…" />
  if (!overview || !profile) return <p className="error">{error || 'No data'}</p>

  return (
    <div className="tab-grid">
      <section className="panel">
        <div className="panel-head">
          <h2>This month</h2>
          {saving ? <Loader label="Saving…" /> : null}
        </div>
        {error ? <p className="error">{error}</p> : null}

        <div className="stat-row">
          <div>
            <span className="muted">Spent</span>
            <strong>{money(overview.totalSpent)}</strong>
          </div>
          <div>
            <span className="muted">Left</span>
            <strong className={overview.remaining < 0 ? 'danger' : ''}>{money(overview.remaining)}</strong>
          </div>
          <div>
            <span className="muted">Goal progress</span>
            <strong>{overview.goalProgressPercent}%</strong>
          </div>
        </div>

        <div className="bars">
          {([
            ['Needs', overview.spentByCategory.NEEDS, overview.idealNeedsAmount],
            ['Wants', overview.spentByCategory.WANTS, overview.idealWantsAmount],
            ['Invest', overview.spentByCategory.INVESTMENT, overview.idealInvestAmount],
            ['Savings', overview.spentByCategory.SAVINGS, overview.idealSavingsAmount],
          ] as const).map(([label, actual, ideal]) => {
            const pct = ideal > 0 ? Math.min(100, Math.round((actual / ideal) * 100)) : 0
            return (
              <div key={label} className="bar-item">
                <div className="bar-meta">
                  <span>{label}</span>
                  <span>{money(actual)} / {money(ideal)}</span>
                </div>
                <div className="bar-track">
                  <div className={`bar-fill ${pct > 100 ? 'over' : ''}`} style={{ width: `${Math.min(pct, 100)}%` }} />
                </div>
              </div>
            )
          })}
        </div>

        {overview.flags.length > 0 ? (
          <div className="flags">
            {overview.flags.map((flag) => (
              <div key={flag.title} className={`flag ${flag.severity.toLowerCase()}`}>
                <strong>{flag.title}</strong>
                <p>{flag.message}</p>
                <span>{money(flag.actual)} vs ideal {money(flag.ideal)}</span>
              </div>
            ))}
          </div>
        ) : (
          <p className="muted">No overspend flags this month. Keep the allocations steady.</p>
        )}

        <div className="advice">
          <h3>Money management tips</h3>
          <ul>
            {overview.advice.map((tip) => (
              <li key={tip}>{tip}</li>
            ))}
          </ul>
        </div>
      </section>

      <section className="panel">
        <h2>Add expense / investment</h2>
        <form className="stack" onSubmit={addExpense}>
          <label>
            Title
            <input value={title} onChange={(e) => setTitle(e.target.value)} required placeholder="Groceries, SIP, dinner…" />
          </label>
          <label>
            Amount (INR)
            <input type="number" min="1" step="1" value={amount} onChange={(e) => setAmount(e.target.value)} required />
          </label>
          <label>
            Category
            <select value={category} onChange={(e) => setCategory(e.target.value as ExpenseCategory)}>
              {categories.map((item) => (
                <option key={item} value={item}>{item}</option>
              ))}
            </select>
          </label>
          <label>
            Note
            <input value={note} onChange={(e) => setNote(e.target.value)} placeholder="Optional" />
          </label>
          <button className="btn btn-primary" type="submit" disabled={saving}>Add entry</button>
        </form>

        <h3 className="mt">Recent flow</h3>
        <ul className="list">
          {overview.expenses.map((expense) => (
            <li key={expense.id}>
              <div>
                <strong>{expense.title}</strong>
                <span className="muted">{expense.category} · {expense.spentOn}</span>
              </div>
              <div className="row-actions">
                <span>{money(expense.amount)}</span>
                <button className="linkish" onClick={() => void removeExpense(expense.id)}>Delete</button>
              </div>
            </li>
          ))}
        </ul>
      </section>

      <section className="panel wide">
        <h2>Salary, targets & final goal</h2>
        <form className="form-grid" onSubmit={saveProfile}>
          <label>
            Monthly salary
            <input type="number" value={profile.monthlySalary} onChange={(e) => setProfile({ ...profile, monthlySalary: Number(e.target.value) })} />
          </label>
          <label>
            Goal amount
            <input type="number" value={profile.goalAmount} onChange={(e) => setProfile({ ...profile, goalAmount: Number(e.target.value) })} />
          </label>
          <label>
            Goal label
            <input value={profile.goalLabel} onChange={(e) => setProfile({ ...profile, goalLabel: e.target.value })} />
          </label>
          <label>
            Needs %
            <input type="number" value={profile.idealNeedsPercent} onChange={(e) => setProfile({ ...profile, idealNeedsPercent: Number(e.target.value) })} />
          </label>
          <label>
            Wants %
            <input type="number" value={profile.idealWantsPercent} onChange={(e) => setProfile({ ...profile, idealWantsPercent: Number(e.target.value) })} />
          </label>
          <label>
            Invest %
            <input type="number" value={profile.idealInvestPercent} onChange={(e) => setProfile({ ...profile, idealInvestPercent: Number(e.target.value) })} />
          </label>
          <label>
            Savings %
            <input type="number" value={profile.idealSavingsPercent} onChange={(e) => setProfile({ ...profile, idealSavingsPercent: Number(e.target.value) })} />
          </label>
          <button className="btn btn-primary" type="submit" disabled={saving}>Save targets</button>
        </form>
      </section>
    </div>
  )
}
