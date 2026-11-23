import { useState } from 'react'
import { analyze } from './api'

const MAX_BYTES = 5 * 1024 * 1024

export default function AnalyzeForm({ onResult }) {
    const [file, setFile] = useState(null)
    const [jobTitle, setJobTitle] = useState('')
    const [jobDescription, setJobDescription] = useState('')
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    function pickFile(e) {
        const chosen = e.target.files[0] || null
        setError('')
        if (chosen) {
            const name = chosen.name.toLowerCase()
            if (!name.endsWith('.pdf') && !name.endsWith('.docx')) {
                setError('Please choose a PDF or DOCX file.')
                e.target.value = ''
                setFile(null)
                return
            }
            if (chosen.size > MAX_BYTES) {
                setError('The file is larger than 5 MB.')
                e.target.value = ''
                setFile(null)
                return
            }
        }
        setFile(chosen)
    }

    async function submit(e) {
        e.preventDefault()
        setError('')
        if (!file) {
            setError('Please choose your resume first.')
            return
        }
        setBusy(true)
        try {
            const result = await analyze(file, jobDescription, jobTitle.trim())
            onResult(result)
        } catch (err) {
            setError(err.message)
        } finally {
            setBusy(false)
        }
    }

    return (
        <div className="card">
            <h2>Analyze a resume</h2>
            <form onSubmit={submit}>
                <label>
                    Resume (PDF or DOCX, max 5 MB)
                    <input type="file" accept=".pdf,.docx" onChange={pickFile} />
                </label>

                <label>
                    Job title (optional)
                    <input
                        type="text"
                        value={jobTitle}
                        onChange={(e) => setJobTitle(e.target.value)}
                        maxLength={200}
                        placeholder="Data Analyst Intern"
                    />
                </label>

                <label>
                    Job description
                    <textarea
                        value={jobDescription}
                        onChange={(e) => setJobDescription(e.target.value)}
                        placeholder={'Required: Python, SQL, Pandas.\nPreferred: Docker, Tableau.'}
                        required
                    />
                </label>
                <p className="muted small">
                    Tip: put "Required" and "Preferred" (or "Nice to have") before the skills. Without those
                    words, every skill is treated as required.
                </p>

                {error && <p className="error">{error}</p>}
                <button type="submit" disabled={busy}>
                    {busy ? 'Analyzing...' : 'Analyze'}
                </button>
            </form>
        </div>
    )
}