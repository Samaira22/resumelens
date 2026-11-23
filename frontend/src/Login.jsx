import { useState } from 'react'
import { login, register, saveSession } from './api'

export default function Login({ onLoggedIn }) {
    const [mode, setMode] = useState('login')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    async function submit(e) {
        e.preventDefault()
        setError('')
        setBusy(true)
        try {
            const action = mode === 'login' ? login : register
            const session = await action(email.trim(), password)
            saveSession(session)
            onLoggedIn(session)
        } catch (err) {
            setError(err.message)
        } finally {
            setBusy(false)
        }
    }

    return (
        <div className="card narrow">
            <h1>ResumeLens</h1>
            <p className="muted">Explainable resume analysis and job matching.</p>

            <form onSubmit={submit}>
                <label>
                    Email
                    <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                </label>
                <label>
                    Password
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        minLength={mode === 'register' ? 8 : undefined}
                        required
                    />
                </label>
                {mode === 'register' && <p className="muted small">Use 8 to 72 characters.</p>}
                {error && <p className="error">{error}</p>}
                <button type="submit" disabled={busy}>
                    {busy ? 'Please wait...' : mode === 'login' ? 'Log in' : 'Create account'}
                </button>
            </form>

            <p className="muted small">
                {mode === 'login' ? 'New here? ' : 'Already have an account? '}
                <button type="button" className="link" onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError('') }}>
                    {mode === 'login' ? 'Create an account' : 'Log in'}
                </button>
            </p>
        </div>
    )
}