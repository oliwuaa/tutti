import React, { useState } from 'react';
import api from '../../api/axios';
import '../../styles/Request.css';

const INSTRUMENT_LABELS = {
    PICCOLO: "Piccolo", FLUTE: "Flet poprzeczny", OBOE: "Obój", ENGLISH_HORN: "Rożek angielski",
    FAGOTT: "Fagot", CLARINET: "Klarnet", ALTO_CLARINET: "Klarnet altowy", BASS_CLARINET: "Klarnet basowy",
    SOPRANO_SAX: "Saksofon sopranowy", ALTO_SAX: "Saksofon altowy", TENOR_SAX: "Saksofon Tenorowy",
    BARITONE_SAX: "Saksofon barytonowy", TRUMPET: "Trąbka", CORNET: "Cornet", FLUGEL_HORN: "Skrzydłówka",
    FRENCH_HORN: "Waltornia", TROMBONE: "Puzon", EUPHONIUM: "Eufonium", TUBA: "Tuba",
    DRUM_KIT: "Perkusja", CYMBALS: "Talerze", BELLS: "Dzwonki", AUX_PERCUSSION: "Perkusonalia",
    KEYBOARD: "Klawisze", BASS_GUITAR: "Gitara basowa", ELECTRIC_GUITAR: "Gitara elektryczna", NONE: "Brak"
};

function RequestsTab({ requests, refresh }) {
    const [loadingId, setLoadingId] = useState(null);

    const handleAction = async (membershipId, action) => {
        const confirmMsg = action === 'approve' 
            ? "Zaakceptować nowego członka?" 
            : "Odrzucić to zgłoszenie?";

        if (!window.confirm(confirmMsg)) return;

        setLoadingId(membershipId); 

        try {
            await api.post(`/memberships/${membershipId}/${action}`);
            
            alert(action === 'approve' ? "Członkostwo zaakceptowane!" : "Zgłoszenie odrzucone.");
            
            if (refresh) refresh(); 
        }  catch (err) { alert(err.message); } finally {
            setLoadingId(null);
        }
    };

    return (
        <div className="requests-view">
            {requests.length > 0 ? (
                <div className="requests-grid">
                    {requests.map(req => (
                        <div key={req.id} className={`brutal-request-card ${loadingId === req.id ? 'processing' : ''}`}>
                            <div className="request-content">
                                <div className="user-info">
                                    <span className="tiny-label">KANDYDAT:</span>
                                    <h3 className="user-full-name">
                                        {req.firstName} {req.lastName}
                                    </h3>
                                </div>
                                
                                <div className="instrument-info">
                                    <span className="tiny-label">DEKLAROWANY INSTRUMENT:</span>
                                    <div className="instrument-badge">
                                        {INSTRUMENT_LABELS[req.instrumentType] || req.instrumentType}
                                    </div>
                                </div>

                                <div className="request-footer">
                                    <span className="date-text">Status: <strong>{req.status}</strong></span>
                                </div>
                            </div>

                            <div className="request-actions-bar">
                                <button 
                                    className="btn-action approve-full" 
                                    disabled={loadingId !== null}
                                    onClick={() => handleAction(req.id, 'approve')}
                                >
                                    {loadingId === req.id ? "..." : "AKCEPTUJ"}
                                </button>
                                <button 
                                    className="btn-action reject-full" 
                                    disabled={loadingId !== null}
                                    onClick={() => handleAction(req.id, 'reject')}
                                >
                                    {loadingId === req.id ? "..." : "ODRZUĆ"}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="empty-state">
                    <div className="no-data-icon">📩</div>
                    <p className="no-data">BRAK NOWYCH ZGŁOSZEŃ</p>
                </div>
            )}
        </div>
    );
}

export default RequestsTab;