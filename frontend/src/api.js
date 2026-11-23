const TOKEN_KEY = 'resumelens_token'
const EMAIL_KEY = 'resumelens_email'

// sessionStorage: the login ends when the tab is closed
export const getToken = () => sessionStorage.getItem(TOKEN_KEY)
export const getEmail = () => sessionStorage.getItem(EMAIL_KEY)

export function saveSession({ token, email }) {
    sessionStorage.setItem(TOKEN_KEY, token)
    sessionStorage.setItem(EMAIL_KEY, email)
}

export function clearSession() {
    sessionStorage.removeItem(TOKEN_KEY)
    sessionStorage.removeItem(EMAIL_KEY)
}

async function request(path, options = {}) {
    const headers = { ...(options.headers || {}) }
    const token = getToken()
    if (token) headers.Authorization = `Bearer ${token}`

    let res
    try {
        res = await fetch(path, { ...options, headers })
    } catch {
        throw new Error('Cannot reach the server. Is the backend running?')
    }

    if (res.status === 204) return null
    const data = await res.json().catch(() => null)

    if (res.status === 401 && !data?.error) {
        clearSession()
        window.dispatchEvent(new Event('auth-expired'))
        throw new Error('Your session has expired. Please log in again.')
    }
    if (!res.ok) throw new Error(data?.error || `Request failed (${res.status})`)
    return data
}

const json = (body) => ({
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
})

export const register = (email, password) => request('/api/auth/register', json({ email, password }))
export const login = (email, password) => request('/api/auth/login', json({ email, password }))

export function analyze(file, jobDescription, jobTitle) {
    const form = new FormData()
    form.append('resume', file)
    form.append('jobDescription', jobDescription)
    if (jobTitle) form.append('jobTitle', jobTitle)
    return request('/api/analyze', { method: 'POST', body: form })
}

export const listAnalyses = () => request('/api/analyses')
export const getAnalysis = (id) => request(`/api/analyses/${id}`)
export const deleteAnalysis = (id) => request(`/api/analyses/${id}`, { method: 'DELETE' })