import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'

type Served = 'cache' | 'groq' | 'gemini' | 'none'
type Msg = { role: 'user' | 'ai'; text: string; served?: Served; ms?: number }

const kind = (s: string): Served =>
  s === 'cache' ? 'cache' : s === 'groq' ? 'groq' : s.startsWith('gemini') ? 'gemini' : 'none'

const NAME: Record<Served, string> = {
  cache: 'Redis cache',
  groq: 'Groq',
  gemini: 'Gemini (fallback)',
  none: 'No provider',
}

const valid = (t: string) => {
  try {
    const p = t.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(p)).exp * 1000 > Date.now()
  } catch {
    return false
  }
}

export default function App() {
  const [token, setToken] = useState(() => {
    const t = localStorage.getItem('token') ?? ''
    return valid(t) ? t : ''
  })
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [authError, setAuthError] = useState('')
  const [msgs, setMsgs] = useState<Msg[]>([])
  const [prompt, setPrompt] = useState('')
  const [busy, setBusy] = useState(false)
  const endRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    endRef.current?.scrollIntoView()
  }, [msgs])

  async function auth(mode: 'login' | 'register', e?: FormEvent) {
    e?.preventDefault()
    setAuthError('')
    try {
      const res = await fetch(`/api/auth/${mode}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })
      const body = await res.text()
      if (!res.ok) return setAuthError(body || 'Request failed')
      const t = JSON.parse(body).token as string
      localStorage.setItem('token', t)
      setToken(t)
    } catch {
      setAuthError('Cannot reach the gateway on port 8080. Start all services first.')
    }
  }

  function signOut(note = '') {
    localStorage.removeItem('token')
    setToken('')
    setMsgs([])
    setAuthError(note)
  }

  async function send(e: FormEvent) {
    e.preventDefault()
    const text = prompt.trim()
    if (!text || busy) return
    if (!valid(token)) return signOut('Your session expired. Sign in again.')
    setPrompt('')
    setBusy(true)
    setMsgs((m) => [...m, { role: 'user', text }])
    try {
      const res = await fetch('/api/ask', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ prompt: text }),
      })
      const d = await res.json()
      setMsgs((m) => [...m, { role: 'ai', text: d.reply, served: kind(d.servedBy), ms: d.latencyMs }])
    } catch {
      setMsgs((m) => [
        ...m,
        { role: 'ai', text: 'The gateway did not respond. Check that all services are running.', served: 'none' },
      ])
    } finally {
      setBusy(false)
    }
  }

  if (!token)
    return (
      <main className="gate">
        <h1>Multi-AI Gateway</h1>
        <p>One API in front of Groq and Gemini, with Redis caching and automatic fallback.</p>
        <form onSubmit={(e) => auth('login', e)}>
          <label>
            Email
            <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
          </label>
          <label>
            Password
            <input type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
          </label>
          {authError && <p className="error" role="alert">{authError}</p>}
          <div className="row">
            <button type="submit">Sign in</button>

            <button
              type="button"
              className="ghost"
              onClick={(e) => {
                if (e.currentTarget.form?.reportValidity()) auth('register')
              }}
            >
              Create account
            </button>
          </div>
        </form>
      </main>
    )

  const ai = msgs.filter((m) => m.role === 'ai' && m.ms !== undefined)
  const hits = ai.filter((m) => m.served === 'cache').length
  const avg = ai.length ? Math.round(ai.reduce((s, m) => s + (m.ms ?? 0), 0) / ai.length) : 0
  const max = Math.max(1, ...ai.map((m) => m.ms ?? 0))

  return (
    <div className="app">
      <section className="chat">
        <header>
          <h1>Multi-AI Gateway</h1>
          <button className="ghost" onClick={() => signOut()}>Sign out</button>
        </header>
        <div className="log">
          {msgs.length === 0 && (
            <p className="empty">Ask anything. Send the same question twice to watch the cache answer instead of a model.</p>
          )}
          {msgs.map((m, i) => (
            <article key={i} className={`msg ${m.role}`}>
              <p>{m.text}</p>
              {m.served && (
                <span className={`tag ${m.served}`}>
                  {NAME[m.served]}
                  {m.ms !== undefined && <span>{m.ms} ms</span>}
                </span>
              )}
            </article>
          ))}
          {busy && <p className="empty">Routing your request…</p>}
          <div ref={endRef} />
        </div>
        <form className="composer" onSubmit={send}>
          <input value={prompt} onChange={(e) => setPrompt(e.target.value)} placeholder="Ask a question" aria-label="Prompt" />
          <button disabled={busy}>Send</button>
        </form>
      </section>
      <aside className="trace">
        <h2>Route trace</h2>
        <dl className="stats">
          <div><dt>Requests</dt><dd>{ai.length}</dd></div>
          <div><dt>Cache hits</dt><dd>{hits}</dd></div>
          <div><dt>Avg latency</dt><dd>{avg} ms</dd></div>
        </dl>
        <ol>
          {ai.map((m, i) => (
            <li key={i} className={m.served}>
              <span className="dot" />
              <div>
                <strong>{NAME[m.served!]}</strong>
                <div className="bar"><i style={{ width: `${Math.max(4, ((m.ms ?? 0) / max) * 100)}%` }} /></div>
              </div>
              <span>{m.ms} ms</span>
            </li>
          ))}
        </ol>
      </aside>
    </div>
  )
}