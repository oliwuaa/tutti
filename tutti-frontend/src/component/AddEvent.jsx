import { useState, useEffect } from 'react';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import api from '../api/axios';
import '../styles/Events.css';

function AddEvent({ isOpen, onClose, onSave, initialData, orchestraId }) {
  const [eventData, setEventData] = useState({
    name: '',
    type: 'REHEARSAL',
    date: '',
    address: '',
    plan: '',
    setlist: []
  });

  const [availableScores, setAvailableScores] = useState([]);

  const eventTypes = [
    { value: 'REHEARSAL', label: 'PRÓBA' },
    { value: 'CONCERT', label: 'KONCERT' },
    { value: 'GIG', label: 'GRANIE' },
    { value: 'OTHER', label: 'INNE' }
  ];

  useEffect(() => {
    const fetchScores = async () => {
      if (!orchestraId || !isOpen) return;
      try {
        const res = await api.get('/scores', { params: { orchestraId } });
        setAvailableScores(res.data);
      }  catch (err) { alert(err.message); }
    };
    fetchScores();
  }, [isOpen, orchestraId]);

  useEffect(() => {
    if (initialData) {
      setEventData({
        ...initialData,
        address: initialData.location || initialData.address || '',
        plan: initialData.description || initialData.desc || initialData.plan || '',
        type: initialData.type || 'REHEARSAL',
        setlist: (initialData.setlist || []).map(item => ({
          ...item,
          customTitle: item.scoreTitle || item.customTitle || ''
        }))
      });
    } else {
      setEventData({
        name: '', type: 'REHEARSAL', date: '', address: '', plan: '', setlist: []
      });
    }
  }, [initialData, isOpen]);

  const handleSetlistChange = (index, field, value) => {
    const newSetlist = [...eventData.setlist];
    newSetlist[index][field] = value;

    if (field === 'customTitle') {
      const matchedScore = availableScores.find(s => s.title === value);
      newSetlist[index].scoreId = matchedScore ? matchedScore.id : null;
    }
    setEventData({ ...eventData, setlist: newSetlist });
  };

  const handleAddSetlistItem = () => {
    setEventData({
      ...eventData,
      setlist: [...eventData.setlist, {
        scoreId: null,
        customTitle: '',
        notes: '',
        position: eventData.setlist.length + 1
      }]
    });
  };

  const onDragEnd = (result) => {
    if (!result.destination) return;
    const items = Array.from(eventData.setlist);
    const [reorderedItem] = items.splice(result.source.index, 1);
    items.splice(result.destination.index, 0, reorderedItem);
    const updatedItems = items.map((item, index) => ({ ...item, position: index + 1 }));
    setEventData({ ...eventData, setlist: updatedItems });
  };

  const handleRemoveSetlistItem = (index) => {
    const newSetlist = eventData.setlist.filter((_, i) => i !== index).map((item, i) => ({
      ...item, position: i + 1
    }));
    setEventData({ ...eventData, setlist: newSetlist });
  };

  const handleSubmit = (e) => {
    e.preventDefault();


    const validSetlist = eventData.setlist.filter(item => item.customTitle?.trim());

    const cleanedSetlist = validSetlist.map(item => ({
      scoreId: item.scoreId || null,
      customTitle: item.scoreId ? null : item.customTitle,
      notes: item.notes || "",
      position: item.position
    }));

    const dataToSend = {
      ...eventData,
      location: eventData.address,
      description: eventData.plan,
      setlist: cleanedSetlist
    };

    onSave(dataToSend);
  };

  const isFormInvalid = !eventData.name.trim() || !eventData.date || !eventData.address.trim();

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="card-header">
          <h3>{initialData ? 'EDYTUJ WYDARZENIE' : 'NOWE WYDARZENIE'}</h3>
          <button className="close-btn" type="button" onClick={onClose}>×</button>
        </div>

        <form className="event-form" onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-group">
              <label>NAZWA WYDARZENIA *</label>
              <input 
                type="text" 
                required 
                placeholder="np. Próba sekcyjna"
                value={eventData.name} 
                onChange={e => setEventData({ ...eventData, name: e.target.value })} 
              />
            </div>
            <div className="form-group">
              <label>TYP WYDARZENIA</label>
              <select value={eventData.type} onChange={e => setEventData({ ...eventData, type: e.target.value })}>
                {eventTypes.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>DATA I GODZINA *</label>
              <input 
                type="datetime-local" 
                required 
                value={eventData.date} 
                onChange={e => setEventData({ ...eventData, date: e.target.value })} 
              />
            </div>
            <div className="form-group">
              <label>LOKALIZACJA *</label>
              <input 
                type="text" 
                required 
                placeholder="np. Sala prób OSP"
                value={eventData.address} 
                onChange={e => setEventData({ ...eventData, address: e.target.value })} 
              />
            </div>
          </div>

          <div className="form-group">
            <label>PLAN / KOMENTARZE</label>
            <textarea 
              rows="4" 
              placeholder="Wpisz plan dnia lub uwagi..."
              value={eventData.plan} 
              onChange={e => setEventData({ ...eventData, plan: e.target.value })}
            ></textarea>
          </div>

          <div className="setlist-section">
            <div className="setlist-header">
              <label>SETLISTA</label>
              <button type="button" className="add-setlist-btn" onClick={handleAddSetlistItem}>+ DODAJ UTWÓR</button>
            </div>

            <datalist id="orchestra-scores-list">
              {availableScores.map(score => (
                <option key={score.id} value={score.title} />
              ))}
            </datalist>

            <DragDropContext onDragEnd={onDragEnd}>
              <Droppable droppableId="setlist">
                {(provided) => (
                  <div {...provided.droppableProps} ref={provided.innerRef} className="setlist-container">
                    {eventData.setlist.map((item, index) => (
                      <Draggable key={`item-${index}`} draggableId={`draggable-${index}`} index={index}>
                        {(provided, snapshot) => (
                          <div ref={provided.innerRef} {...provided.draggableProps} className={`setlist-item-row ${snapshot.isDragging ? 'dragging' : ''}`}>
                            <div className="drag-handle" {...provided.dragHandleProps}>☰</div>
                            <span className="pos">{index + 1}.</span>

                            <input
                              list="orchestra-scores-list"
                              placeholder="Tytuł utworu (wymagane w tym wierszu)..."
                              value={item.customTitle || ''}
                              onChange={e => handleSetlistChange(index, 'customTitle', e.target.value)}
                              className="setlist-input-main"
                              autoComplete="off"
                            />

                            <input
                              placeholder="Notatki"
                              value={item.notes || ''}
                              onChange={e => handleSetlistChange(index, 'notes', e.target.value)}
                              className="setlist-input-notes"
                            />

                            <button type="button" className="remove-item-btn" onClick={() => handleRemoveSetlistItem(index)}>×</button>
                          </div>
                        )}
                      </Draggable>
                    ))}
                    {provided.placeholder}
                  </div>
                )}
              </Droppable>
            </DragDropContext>
          </div>

          <div className="modal-footer">
            <button type="button" className="main-card-btn cancel" onClick={onClose}>ANULUJ</button>
            <button 
              type="submit" 
              className={`alert-action-btn ${isFormInvalid ? 'disabled-btn' : ''}`}
              disabled={isFormInvalid}
            >
              {initialData ? 'ZAPISZ ZMIANY' : 'DODAJ'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddEvent;