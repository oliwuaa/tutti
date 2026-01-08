import React from 'react';

function Memberships({ 
    activeMemberships, 
    pendingRequests, 
    expandedId, 
    handleToggleExpand, 
    equipmentData, 
    handleRemoveMembership, 
    isAddingOrchestra, 
    setIsAddingOrchestra,
    availableOrchestras, 
    joinForm, 
    setJoinForm, 
    handleSendJoinRequest, 
    INSTRUMENT_TYPES, 
    toRoman 
}) {
    return (
        <>
            <div className="full-width-event">
                <div className="info-section"><span className="detail-label">MOJE CZŁONKOSTWA</span></div>
                <div className="display-setlist">
                    {activeMemberships.length > 0 ? activeMemberships.map(m => {
                        const eq = equipmentData[m.id] || { clothes: [], instruments: [] };
                        return (
                            <div key={m.id} className="up-accordion-item owner-item-card">
                                <div className="setlist-display-row interactive" onClick={() => handleToggleExpand(m.id)}>
                                    <div className="pos">•</div>
                                    <div className="item-details">
                                        <div className="item-title">{m.orchestraName}</div>
                                        <div className="item-sub" style={{ fontSize: '0.8rem', color: '#544013', fontWeight: 'bold' }}>
                                            {INSTRUMENT_TYPES[m.instrumentType]} | Głos: {toRoman(m.partNumber)}
                                        </div>
                                    </div>
                                    <div className="event-actions" style={{ marginRight: '10px' }}>
                                        <div className={`arrow-icon ${expandedId === m.id ? 'rotated' : ''}`} style={{ transition: '0.3s' }}>▼</div>
                                    </div>
                                </div>
                                {expandedId === m.id && (
                                    <div className="up-expanded-content" style={{ padding: '15px', borderTop: '1px solid rgba(0,0,0,0.05)', background: 'rgba(255,255,255,0.3)' }}>
                                        <div className="equipment-section">
                                            <span className="tiny-label" style={{ color: '#a39071', fontWeight: 'bold', fontSize: '0.7rem' }}>MOJE WYPOSAŻENIE:</span>
                                            <div className="equipment-list" style={{ marginTop: '10px' }}>
                                                {eq.instruments?.length > 0 ? eq.instruments.map(i => (
                                                    <div key={i.id} className="eq-item" style={{ padding: '4px 0', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                        <strong>{i.instrumentType}</strong> (Marka: {i.brand}, Numer: {i.number}) {i.description && `- ${i.description}`}
                                                    </div>
                                                )) : <div className="eq-empty" style={{ fontSize: '0.8rem', color: '#999' }}>Brak przypisanych instrumentów</div>}
                                                {eq.clothes?.length > 0 ? eq.clothes.map(c => (
                                                    <div key={c.id} className="eq-item" style={{ padding: '4px 0', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '8px', marginTop: '5px' }}>
                                                        {c.type} (Rozmiar: {c.size})
                                                    </div>
                                                )) : <div className="eq-empty" style={{ fontSize: '0.8rem', color: '#999', marginTop: '5px' }}>Brak ubrań</div>}
                                            </div>
                                        </div>
                                        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '15px' }}>
                                            <button className="text-action-btn delete-btn" onClick={(e) => { e.stopPropagation(); handleRemoveMembership(m.id, m.orchestraName, false); }}>
                                                OPUŚĆ ORKIESTRĘ
                                            </button>
                                        </div>
                                    </div>
                                )}
                            </div>
                        );
                    }) : <p className="no-data">Brak aktywnych członkostw.</p>}
                </div>
                {!isAddingOrchestra ? (
                    <button className="add-setlist-btn" onClick={() => setIsAddingOrchestra(true)}>+ DOŁĄCZ DO NOWEJ ORKIESTRY</button>
                ) : (
                    <div className="add-orchestra-form" style={{ borderTop: '1px solid #ddd', paddingTop: '15px', marginTop: '10px' }}>
                        <select className="up-form-input editing" value={joinForm.orchestraId} onChange={(e) => setJoinForm({ ...joinForm, orchestraId: e.target.value })}>
                            <option value="">-- WYBIERZ ORKIESTRĘ --</option>
                            {availableOrchestras.map(orch => <option key={orch.id} value={orch.id}>{orch.name}</option>)}
                        </select>
                        <select className="up-form-input editing" style={{ marginTop: '10px' }} value={joinForm.instrumentType} onChange={(e) => setJoinForm({ ...joinForm, instrumentType: e.target.value })}>
                            {Object.entries(INSTRUMENT_TYPES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
                        </select>
                        <div className="form-actions" style={{ marginTop: '10px' }}>
                            <button className="confirm-btn" onClick={handleSendJoinRequest}>WYŚLIJ PROŚBĘ</button>
                            <button className="cancel-btn" onClick={() => setIsAddingOrchestra(false)}>ANULUJ</button>
                        </div>
                    </div>
                )}
            </div>

            {pendingRequests.length > 0 && (
                <div className="full-width-event pending-section">
                    <div className="info-section"><span className="detail-label pending-label">OCZEKUJĄCE PROŚBY</span></div>
                    <div className="display-setlist">
                        {pendingRequests.map(p => (
                            <div key={p.id} className="up-accordion-item pending-card">
                                <div className="setlist-display-row">
                                    <div className="pending-status-indicator"><div className="pulse-dot"></div></div>
                                    <div className="item-details">
                                        <div className="item-title pending-name">{p.orchestraName}</div>
                                        <div className="item-notes pending-sub">Wysłano prośę o dołączenie jako: <strong>{INSTRUMENT_TYPES[p.instrumentType]}</strong></div>
                                    </div>
                                    <div className="event-actions">
                                        <button className="text-action-btn cancel-request-btn" onClick={() => handleRemoveMembership(p.id, p.orchestraName, true)}>WYCOFAJ</button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </>
    );
}

export default Memberships;