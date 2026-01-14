import { useState } from 'react';
import api from '../api/axios';
import '../styles/Events.css';

const EVENT_TYPE_TRANSLATIONS = {
    'CONCERT': 'Koncert',
    'REHEARSAL': 'Próba',
    'GIG': 'Granie',
    'OTHER': 'Inne'
};

function EventDetails({ event, onBack, userOrchestra }) {
  const [loadingPdf, setLoadingPdf] = useState(null);

  if (!event) return null;

  const userInstrument = userOrchestra?.instrument;
  const orchestraId = userOrchestra?.orchestraId || userOrchestra?.id;
  const translateType = (type) => EVENT_TYPE_TRANSLATIONS[type] || type;

  const handleViewPdf = async (item) => {
    const scoreId = item.score?.id || item.scoreId;
    if (!scoreId) return;

    setLoadingPdf(item.id || item.position);

    try {
      let pdfEndpoint = '';
      if (userInstrument && userInstrument !== 'NONE') {
        try {
          const partRes = await api.get(`/parts/score/${scoreId}/type/${userInstrument}/number/1/orchestra/${orchestraId}`);
          pdfEndpoint = `/parts/${partRes.data.id}/pdf`;
        } catch (err) { alert(err.message); }
      }

      if (!pdfEndpoint) {
        pdfEndpoint = `/scores/${scoreId}/file`;
      }

      const response = await api.get(pdfEndpoint, { responseType: 'blob' });
      const file = new Blob([response.data], { type: 'application/pdf' });
      const fileURL = URL.createObjectURL(file);
      window.open(fileURL, '_blank');
      setTimeout(() => URL.revokeObjectURL(fileURL), 100);
    }  catch (err) { alert(err.message); } finally {
      setLoadingPdf(null);
    }
  };

  return (
    <div className="calendar-view details-view">
      <header className="view-header">
        <div className="header-left">
          <button className="back-link" onClick={onBack}>← WRÓĆ DO HARMONOGRAMU</button>
          <h1 className="welcome-title">{event.name}</h1>
          <p className="welcome-subtitle">
            {translateType(event.type)}
          </p>
        </div>
      </header>

      <div className="details-grid">
        <div className="details-info-card full-width-event">
          <div className="info-section">
            <label className="detail-label">KIEDY</label>
            <div className="detail-value">
              {new Date(event.date).toLocaleDateString('pl-PL', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' })}
              <br />
              Godzina: {new Date(event.date).toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' })}
            </div>
          </div>

          <div className="info-section">
            <label className="detail-label">PLAN I UWAGI</label>
            <p className="event-description" style={{ whiteSpace: 'pre-wrap' }}>
              {event.desc || event.plan || event.description || "Brak dodatkowych uwag."}
            </p>
          </div>
        </div>

        <div className="details-setlist-card full-width-event">
          <label className="detail-label">SETLISTA</label>
          {event.setlist && event.setlist.length > 0 ? (
            <div className="display-setlist">
              {[...event.setlist]
                .sort((a, b) => a.position - b.position)
                .map((item, index) => {
                  const hasScore = item.score?.id || item.scoreId;
                  const isCurrentLoading = loadingPdf === (item.id || item.position);

                  return (
                    <div key={item.id || index} className={`setlist-display-row ${hasScore ? 'has-score-link' : ''}`}>
                      <span className="pos">{index + 1}.</span>
                      <div className="item-content">
                        <div className="item-main-line">
                          <div className="item-title">
                            {item.scoreTitle || (item.score ? item.score.title : item.customTitle)}
                          </div>
                          {hasScore && (
                            <button
                              className="pdf-view-button"
                              onClick={() => handleViewPdf(item)}
                              disabled={loadingPdf !== null}
                            >
                              {isCurrentLoading ? 'ŁADOWANIE...' : '📄 NUTY'}
                            </button>
                          )}
                        </div>
                        {item.notes && item.notes.trim() !== "" && (
                          <div className="item-notes-secondary" style={{ whiteSpace: 'pre-wrap' }}>
                            {item.notes}
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
            </div>
          ) : (
            <p className="no-data">Brak przypisanych utworów.</p>
          )}
        </div>
      </div>
    </div>
  );
}

export default EventDetails;