import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import api from '../api/axios';
import '../styles/MainDashboard.css';
import '../styles/Events.css';
import AddEvent from '../component/AddEvent';
import EventDetails from '../component/EventDetails';

function EventsCalendar() {
  const navigate = useNavigate();
  const { userOrchestra } = useOutletContext();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState(null);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [allEvents, setAllEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [scope, setScope] = useState('upcoming');

  const isManagement = ['OWNER', 'ADMIN', 'CONDUCTOR'].includes(userOrchestra?.role);
  const orchestraId = userOrchestra?.orchestraId || userOrchestra?.id;

  const getEventTypeName = (type) => {
    const types = { 'REHEARSAL': 'Próba', 'CONCERT': 'Koncert', 'GIG': 'Granie', 'OTHER': 'Inne' };
    return types[type] || type;
  };

  const fetchEvents = useCallback(async () => {
    if (!orchestraId) return;
    try {
      setLoading(true);
      const response = await api.get(`/orchestras/${orchestraId}/events`, { params: { scope } });
      setAllEvents(response.data);
    }  catch (err) { alert(err.message); } finally {
      setLoading(false);
    }
  }, [orchestraId, scope]);

  useEffect(() => { fetchEvents(); }, [fetchEvents]);

  const handleSaveEvent = async (eventData) => {
    try {
      if (editingEvent) {
        await api.put(`/orchestras/${orchestraId}/events/${editingEvent.id}`, eventData);
      } else {
        await api.post(`/orchestras/${orchestraId}/events`, eventData);
      }
      setIsModalOpen(false);
      fetchEvents();
    }  catch (err) { alert(err.message); }
  };

  const handleDelete = async (id) => {
    if (window.confirm("Czy na pewno chcesz usunąć to wydarzenie?")) {
      try {
        await api.delete(`/orchestras/${orchestraId}/events/${id}`);
        fetchEvents();
      }  catch (err) { alert(err.message); }
    }
  };

  if (selectedEvent) {
    return <EventDetails event={selectedEvent} onBack={() => setSelectedEvent(null)} userOrchestra={userOrchestra} />;
  }

  return (
    <div className="calendar-view">
      <header className="view-header">
        <div className="header-left">
          <button className="back-link" onClick={() => navigate('/dashboard')}>← WRÓĆ DO PULPITU</button>
          <h1 className="welcome-title">HARMONOGRAM</h1>
          <p className="welcome-subtitle">WSZYSTKIE WYDARZENIA TWOJEJ ORKIESTRY</p>
          <div className="scope-tabs-simple" style={{ marginTop: '15px' }}>
            <button onClick={() => setScope('upcoming')} className={`tab-btn ${scope === 'upcoming' ? 'active' : ''}`} style={{ color: scope === 'upcoming' ? '#f28c38' : '#a35618ff', background: 'none', border: 'none', marginRight: '15px', cursor: 'pointer', fontWeight: 'bold' }}>NADCHODZĄCE</button>
            <button onClick={() => setScope('past')} className={`tab-btn ${scope === 'past' ? 'active' : ''}`} style={{ color: scope === 'past' ? '#f28c38' : '#a35618ff', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>ARCHIWUM</button>
          </div>
        </div>
        {isManagement && (
          <button className="alert-action-btn" onClick={() => { setEditingEvent(null); setIsModalOpen(true); }}>DODAJ NOWE WYDARZENIE +</button>
        )}
      </header>

      <div className="full-events-list">
        {loading ? <p>Ładowanie...</p> : allEvents.length === 0 ? <p>Brak wydarzeń.</p> : (
          allEvents.map(event => (
            <div key={event.id} className="event-row interactive full-width-event">
              <div className="event-date-column">
                <div className="event-day">{new Date(event.date).toLocaleDateString('pl-PL', { day: '2-digit', month: '2-digit' })}</div>
                <div className="event-hour">{new Date(event.date).toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' })}</div>
                <div className="event-type-badge" style={{ fontSize: '10px', marginTop: '5px', color: '#f28c38' }}>{getEventTypeName(event.type)}</div>
              </div>

              <div className="event-details-column">
                <div className="event-name">{event.name}</div>
                <div className="event-location">{event.location || event.address}</div>
                <p className="event-description">
                  {(event.description || event.desc || event.plan)?.split('\n')[0]}
                </p>
              </div>

              <div className="event-actions">
                <button className="text-action-btn" onClick={() => setSelectedEvent(event)}>SZCZEGÓŁY</button>
                {isManagement && (
                  <>
                    <button className="text-action-btn edit-btn" onClick={() => { setEditingEvent(event); setIsModalOpen(true); }}>EDYTUJ</button>
                    <button className="text-action-btn delete-btn" onClick={() => handleDelete(event.id)}>USUŃ</button>
                  </>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      <AddEvent
        isOpen={isModalOpen} onClose={() => setIsModalOpen(false)}
        onSave={handleSaveEvent} initialData={editingEvent} orchestraId={orchestraId}
      />
    </div>
  );
}

export default EventsCalendar;