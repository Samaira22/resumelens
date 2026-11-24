import { useEffect, useState } from 'react'
import Login from './Login'
import AnalyzeForm from './AnalyzeForm'
import Results from './Results'
import History from './History'
import { clearSession, getEmail, getToken } from './api'

export default function App() {
    const [email, setEmail] = useState(getToken() ? getEmail() : null)
    const [result, setResult] = useState(null)
    const [tab, setTab] = useState('analyze')

    useEffect(() => {
        const expired = () => setEmail(null)
        window.addEventListener('auth-expired', expired)
        return () => window.removeEventListener('auth-expired', expired)
    }, [])

    function logout() {
        clearSession()
        setResult(null)
        setTab('analyze')
        setEmail(null)
    }

    if (!email) {
        return <Login onLoggedIn={(s) => setEmail(s.email)} />
    }

    return (
        <div className="page">
            <header className="topbar">
                <strong>ResumeLens</strong>
                <span>
          {email} <button className="link" onClick={logout}>Log out</button>
        </span>
            </header>

            <nav className="tabs">
                <button className={tab === 'analyze' ? 'tab active' : 'tab'} onClick={() => setTab('analyze')}>
                    Analyze
                </button>
                <button className={tab === 'history' ? 'tab active' : 'tab'} onClick={() => setTab('history')}>
                    History
                </button>
            </nav>

            {tab === 'history' ? (
                <History />
            ) : result ? (
                <Results result={result} onBack={() => setResult(null)} backLabel="Analyze another resume" />
            ) : (
                <AnalyzeForm onResult={setResult} />
            )}
        </div>
    )
}