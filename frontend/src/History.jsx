import { useEffect, useState } from 'react'
import { deleteAnalysis, getAnalysis, listAnalyses } from './api'
import Results from './Results'

export default function History() {
    const [items, setItems] = useState(null)   // null = still loading
    const [selected, setSelected] = useState(null)
    const [error, setError] = useState('')
    const [busyId, setBusyId] = useState(null)

    useEffect(() => {
        let active = true
        listAnalyses()
            .then((data) => { if (active) setItems(data) })
            .catch((e) => { if (active) { setError(e.message); setItems([]) } })
        return () => { active = false }
    }, [])

    async function open(id) {
        setError('')
        setBusyId(id)
        try {
            setSelected(await getAnalysis(id))
        } catch (e) {
            setError(e.message)
        } finally {
            setBusyId(null)
        }
    }

    async function remove(id) {
        if (!window.confirm('Delete this analysis? This cannot be undone.')) return
        setError('')
        setBusyId(id)
        try {
            await deleteAnalysis(id)
            setItems((current) => current.filter((item) => item.id !== id))
        } catch (e) {
            setError(e.message)
        } finally {
            setBusyId(null)
        }
    }

    if (selected) {
        return <Results result={selected} onBack={() => setSelected(null)} backLabel="Back to history" />
    }

    if (items === null) return <div className="card"><p className="muted">Loading...</p></div>

    return (
        <div className="card">
            <h2>Your analyses</h2>
            {error && <p className="error">{error}</p>}
            {items.length === 0 ? (
                <p className="muted">Nothing here yet. Analyze a resume and it will appear in this list.</p>
            ) : (
                items.map((item) => (
                    <div className="history-row" key={item.id}>
                        <div>
                            <strong>{item.jobTitle}</strong>
                            <div className="muted small">
                                {item.resumeFileName} · {new Date(item.createdAt).toLocaleString()}
                            </div>
                        </div>
                        <div className="history-actions">
                            <span className="history-score">{item.score}</span>
                            <button className="secondary" disabled={busyId === item.id} onClick={() => open(item.id)}>
                                View
                            </button>
                            <button className="danger" disabled={busyId === item.id} onClick={() => remove(item.id)}>
                                Delete
                            </button>
                        </div>
                    </div>
                ))
            )}
        </div>
    )
}