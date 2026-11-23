function scoreColor(score) {
    if (score >= 80) return 'var(--good)'
    if (score >= 60) return 'var(--accent)'
    if (score >= 40) return 'var(--warn)'
    return 'var(--bad)'
}

function Bar({ name, weight, value }) {
    const pct = Math.round(value * 100)
    return (
        <div className="bar-row">
            <div className="bar-label">
                <span>{name}</span>
                <span className="muted">{pct}% (weight {weight}%)</span>
            </div>
            <div className="bar">
                <span style={{ width: `${pct}%` }} />
            </div>
        </div>
    )
}

function Chips({ items, kind }) {
    if (!items || items.length === 0) return <span className="muted small">None</span>
    return (
        <div className="chips">
            {items.map((item) => (
                <span key={item} className={`chip ${kind}`}>{item}</span>
            ))}
        </div>
    )
}

function SkillGroup({ title, group }) {
    const partial = Object.entries(group.partial || {})
    return (
        <div className="card">
            <h2>{title}</h2>
            <p className="small"><strong>Matched</strong></p>
            <Chips items={group.matched} kind="match" />
            <p className="small"><strong>Related skill found (half credit)</strong></p>
            {partial.length === 0 ? (
                <span className="muted small">None</span>
            ) : (
                <div className="chips">
                    {partial.map(([wanted, found]) => (
                        <span key={wanted} className="chip partial">{wanted} ← {found}</span>
                    ))}
                </div>
            )}
            <p className="small"><strong>Missing</strong></p>
            <Chips items={group.missing} kind="missing" />
        </div>
    )
}

export default function Results({ result, onBack, backLabel }) {
    return (
        <>
            <div className="card">
                <div className="score-box">
                    <div className="score-num" style={{ color: scoreColor(result.score) }}>
                        {result.score}<span className="score-of">/100</span>
                    </div>
                    <div>
                        <div className="score-label">{result.label}</div>
                        <div className="muted small">{result.formula}</div>
                        <div className="muted small">
                            A custom compatibility score, not a real ATS score.
                        </div>
                    </div>
                </div>

                <h2>How the score was calculated</h2>
                <Bar name="S: skill coverage" weight={70} value={result.skillCoverage} />
                <Bar name="T: evidence in projects/experience" weight={20} value={result.evidence} />
                <Bar name="C: resume completeness" weight={10} value={result.completeness} />
            </div>

            <SkillGroup title="Required skills" group={result.required} />
            <SkillGroup title="Preferred skills" group={result.preferred} />

            <div className="card">
                <h2>Evidence</h2>
                <p className="small"><strong>Shown in projects or experience</strong></p>
                <Chips items={result.demonstratedSkills} kind="match" />
                <p className="small"><strong>Only listed (no project evidence)</strong></p>
                <Chips items={result.listedOnlySkills} kind="neutral" />
                {result.missingSections?.length > 0 && (
                    <>
                        <p className="small"><strong>Missing resume sections</strong></p>
                        <Chips items={result.missingSections} kind="missing" />
                    </>
                )}
            </div>

            <div className="card">
                <h2>Suggestions</h2>
                {result.suggestions?.length ? (
                    <ol className="suggestions">
                        {result.suggestions.map((s, i) => (
                            <li key={i}>{s}</li>
                        ))}
                    </ol>
                ) : (
                    <p className="muted">No suggestions. Nice work.</p>
                )}
            </div>

            {onBack && <button className="secondary" onClick={onBack}>{backLabel}</button>}
        </>
    )
}