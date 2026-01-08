function OwnedOrchestras({ 
    ownedOrchestras, 
    onManageOrchestra, 
    handleStartEditOrchestra, 
    handleDeleteOrchestra, 
    isCreatingOrchestra, 
    setIsCreatingOrchestra,
    createForm, 
    setCreateForm, 
    handleCreateOrUpdateOrchestra, 
    setEditingOrchestraId 
}) {
    return (
        <div className="full-width-event">
            <div className="info-section">
                <span className="detail-label">TWOJE ORKIESTRY (ZARZĄDZANIE)</span>
            </div>
            <div className="display-setlist">
                {ownedOrchestras.length > 0 ? ownedOrchestras.map(orch => (
                    <div 
                        key={orch.id} 
                        className="up-accordion-item owner-item-card" 
                        onClick={() => onManageOrchestra(orch)}
                        style={{ cursor: 'pointer' }}
                    >
                        <div className="setlist-display-row interactive">
                            <div className="pos"></div>
                            <div className="item-details">
                              
                                <div className="item-title">{orch.name}</div>
                                <div className="item-notes">{orch.address}</div>
                            </div>
                            <div className="event-actions">
                                <button
                                    className="text-action-btn edit-btn"
                                    onClick={(e) => {
                                        e.stopPropagation(); 
                                        handleStartEditOrchestra(e, orch);
                                    }}
                                >
                                    EDYTUJ
                                </button>
                                <button
                                    className="text-action-btn delete-btn"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        handleDeleteOrchestra(e, orch.id, orch.name);
                                    }}
                                >
                                    USUŃ
                                </button>
                            </div>
                        </div>
                    </div>
                )) : <p className="no-data">Brak zarządzanych jednostek.</p>}
            </div>

            {!isCreatingOrchestra ? (
                <button 
                    className="add-setlist-btn" 
                    onClick={() => { 
                        setEditingOrchestraId(null); 
                        setCreateForm({ name: "", address: "" }); 
                        setIsCreatingOrchestra(true); 
                    }}
                >
                    + ZAŁÓŻ NOWĄ ORKIESTRĘ
                </button>
            ) : (
                <div className="add-orchestra-form">
                    <input 
                        className="up-form-input editing" 
                        placeholder="NAZWA ORKIESTRY..." 
                        value={createForm.name} 
                        onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })} 
                    />
                    <input 
                        className="up-form-input editing" 
                        style={{ marginTop: '5px' }} 
                        placeholder="ADRES..." 
                        value={createForm.address} 
                        onChange={(e) => setCreateForm({ ...createForm, address: e.target.value })} 
                    />
                    <div className="form-actions" style={{ marginTop: '10px' }}>
                        <button className="confirm-btn" onClick={handleCreateOrUpdateOrchestra}>ZAPISZ</button>
                        <button className="cancel-btn" onClick={() => setIsCreatingOrchestra(false)}>ANULUJ</button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default OwnedOrchestras;