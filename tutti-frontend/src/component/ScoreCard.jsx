function ScoreCard({ sheet, userPartInfo, isManagement, onEdit, onDelete, onOpenPdf, groupPartsFn, instrumentLabels }) {
    const myVoice = sheet.parts?.find(p => 
        p.type === userPartInfo.type && 
        p.partNumber === userPartInfo.number
    );

    return (
        <div className="event-row full-width-event sheet-card-v2">
            <div className="sheet-main-info">
                <div className="sheet-title-group">
                    <span className="category-tag">NUTY</span>
                    <h3 className="sheet-primary-title">{sheet.title}</h3>
                    <p className="sheet-author">{sheet.composer}</p>
                </div>

                <div className="sheet-quick-actions">
                    {isManagement && (
                        <>
                            <button className="score-btn" style={{ background: '#544013', color: 'white', marginRight: '10px' }} onClick={() => onEdit(sheet)}>EDYTUJ</button>
                            <button className="score-btn" style={{ background: '#8b0000', color: 'white', marginRight: '10px' }} onClick={() => onDelete(sheet.id)}>USUŃ</button>
                        </>
                    )}

                    {myVoice ? (
                        <button className="my-voice-btn" onClick={() => onOpenPdf(`/parts/${myVoice.id}/pdf`)}>
                            <div className="btn-text"><span className="btn-label">TWÓJ GŁOS</span></div>
                        </button>
                    ) : (
                        <div className="no-voice-tag" style={{ color: '#a39071', fontSize: '0.7rem', fontWeight: 800 }}>BRAK TWOJEJ PARTII</div>
                    )}
                    <button className="score-btn" onClick={() => onOpenPdf(`/scores/${sheet.id}/file`)}>PARTYTURA</button>
                </div>
            </div>

            <details className="all-voices-dropdown">
                <summary>Pokaż wszystkie głosy ({sheet.parts?.length || 0})</summary>
                <div className="voices-expanded-container">
                    {groupPartsFn(sheet.parts).map(([instrument, parts]) => (
                        <div key={instrument} className="instrument-group">
                            <h4 className="instrument-group-title">{instrumentLabels[instrument] || instrument}</h4>
                            <div className="voices-mini-grid">
                                {parts.sort((a, b) => a.partNumber - b.partNumber).map((p, i) => (
                                    <button key={i} onClick={() => onOpenPdf(`/parts/${p.id}/pdf`)} className="mini-voice-link">
                                        Głos {p.partNumber || "I"}
                                    </button>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>
            </details>
        </div>
    );
}

export default ScoreCard;