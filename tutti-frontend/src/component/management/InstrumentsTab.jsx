import React, { useState } from 'react';
import Modal from '../EditBox.jsx';
import api from '../../api/axios';

const INSTRUMENTS_LIST = {
  PICCOLO: "Piccolo", FLUTE: "Flet poprzeczny", OBOE: "Obój", ENGLISH_HORN: "Rożek angielski",
  FAGOTT: "Fagot", CLARINET: "Klarnet", ALTO_CLARINET: "Klarnet altowy", BASS_CLARINET: "Klarnet basowy",
  SOPRANO_SAX: "Saksofon sopranowy", ALTO_SAX: "Saksofon altowy", TENOR_SAX: "Saksofon Tenorowy",
  BARITONE_SAX: "Saksofon barytonowy", TRUMPET: "Trąbka", CORNET: "Cornet", FLUGEL_HORN: "Skrzydłówka",
  FRENCH_HORN: "Waltornia", TROMBONE: "Puzon", EUPHONIUM: "Eufonium", TUBA: "Tuba",
  DRUM_KIT: "Perkusja", CYMBALS: "Talerze", BELLS: "Dzwonki", AUX_PERCUSSION: "Perkusonalia",
  KEYBOARD: "Klawisze", BASS_GUITAR: "Gitara basowa", ELECTRIC_GUITAR: "Gitara elektryczna", NONE: "Brak"
};

function InstrumentsTab({ instruments = [], refresh, members = [], orchestraId }) {
  const [instSearch, setInstSearch] = useState('');
  const [editingInstrument, setEditingInstrument] = useState(null);

  const handleQuickUnassign = async (id) => {
    if (window.confirm("Czy chcesz odebrać ten instrument i przenieść go do magazynu?")) {
      try {
        await api.post(`/instruments/${id}/unassign`);
        refresh();
      } catch (err) { alert(err.message); }
    }
  };

  const handleSaveInstrument = async () => {
    try {
      const payload = {
        orchestraId: orchestraId,
        instrumentType: editingInstrument.type,
        brand: editingInstrument.brand,
        number: editingInstrument.number,
      };

      let instrumentId = editingInstrument.id;

      if (instrumentId) {
        await api.put(`/instruments/${instrumentId}`, payload);
      } else {
        const res = await api.post('/instruments', payload);
        instrumentId = res.data.id;
      }

      if (editingInstrument.newAssignmentId !== editingInstrument.oldAssignmentId) {
        if (editingInstrument.oldAssignmentId) {
          await api.post(`/instruments/${instrumentId}/unassign`);
        }
        if (editingInstrument.newAssignmentId) {
          await api.post(`/instruments/${instrumentId}/member/${editingInstrument.newAssignmentId}/assign`);
        }
      }

      setEditingInstrument(null);
      refresh();
    }  catch (err) { alert(err.message); }
    
  };

  const handleDelete = async (id) => {
    if (window.confirm("Czy na pewno chcesz trwale usunąć ten instrument?")) {
      try {
        await api.delete(`/instruments/${id}`);
        refresh();
      }  catch (err) { alert(err.message); }
    }
  };

  return (
    <div className="tab-container">
      <div className="management-actions-bar">
        <input
          placeholder="SZUKAJ PO TYPIE, MARCE LUB NUMERZE..."
          value={instSearch}
          onChange={(e) => setInstSearch(e.target.value)}
          className="brutal-input search-input"
        />
        <button
          className="text-action-btn edit-btn"
          onClick={() => setEditingInstrument({ type: 'TRUMPET', brand: '', number: '', newAssignmentId: '', oldAssignmentId: '' })}
        >
          DODAJ INSTRUMENT
        </button>
      </div>

      <div className="table-wrapper">
        <table className="flat-table">
          <thead>
            <tr>
              <th>TYP</th>
              <th>MARKA / MODEL</th>
              <th>NUMER</th>
              <th>POSIADACZ</th>
              <th style={{ textAlign: 'right' }}>AKCJE</th>
            </tr>
          </thead>
          <tbody>
            {instruments
              ?.filter(i => {
                const term = instSearch.toLowerCase();
                const type = (INSTRUMENTS_LIST[i.instrumentType] || i.instrumentType || "").toString().toLowerCase();
                const brand = (i.brand || "").toString().toLowerCase();
                const serial = (i.number || "").toString().toLowerCase();
                const owner = (i.ownerFullName || "").toString().toLowerCase();
                return type.includes(term) || brand.includes(term) || serial.includes(term) || owner.includes(term);
              })
              .sort((a, b) => {
                const nameA = (INSTRUMENTS_LIST[a.instrumentType] || a.instrumentType || "").toString();
                const nameB = (INSTRUMENTS_LIST[b.instrumentType] || b.instrumentType || "").toString();
                if (nameA === nameB) {
                  return (a.brand || "").localeCompare(b.brand || "");
                }
                return nameA.localeCompare(nameB);
              })
              .map(inst => {
                const rowClass = inst.ownerId ? "row-assigned" : "row-in-storage";

                return (
                  <tr key={inst.id} className={rowClass}>
                    <td className="cell-name primary-text">
                      {INSTRUMENTS_LIST[inst.instrumentType] || inst.instrumentType}
                    </td>
                    <td className="cell-instrument tag-text">{inst.brand}</td>
                    <td className="cell-voice voice-text">#{inst.number || 'brak'}</td>
                    <td className="cell-loan">
                      {inst.ownerId ? (
                        <div className="owner-display">
                          <span className="status-badge assigned">WYDANY</span>
                          <span className="status-text-sub">{inst.ownerFullName}</span>
                        </div>
                      ) : (
                        <span className="status-badge storage">W MAGAZYNIE</span>
                      )}
                    </td>
                    <td className="cell-actions">
                      <div className="action-cell-group">
                        {inst.ownerId && (
                          <button
                            className="btn-simple unassign"
                            onClick={() => handleQuickUnassign(inst.id)}
                            title="Odbierz instrument"
                          >
                            ODBIERZ
                          </button>
                        )}
                        <button className="btn-simple edit" onClick={() => setEditingInstrument({
                          id: inst.id,
                          type: inst.instrumentType,
                          brand: inst.brand,
                          number: inst.number || '',
                          newAssignmentId: inst.ownerId || '',
                          oldAssignmentId: inst.ownerId || ''
                        })}>EDYTUJ</button>
                        <button className="btn-simple delete" onClick={() => handleDelete(inst.id)}>USUŃ</button>
                      </div>
                    </td>
                  </tr>
                );
              })}
          </tbody>
        </table>
      </div>

      <Modal
        isOpen={!!editingInstrument}
        onClose={() => setEditingInstrument(null)}
        title={editingInstrument?.id ? "EDYTUJ INSTRUMENT" : "DODAJ NOWY INSTRUMENT"}
      >
        <div className="modal-form">
          <div className="form-group">
            <label>TYP INSTRUMENTU</label>
            <select
              className="brutal-input brutal-select"
              value={editingInstrument?.type || 'NONE'}
              onChange={e => setEditingInstrument({ ...editingInstrument, type: e.target.value })}
            >
              {Object.entries(INSTRUMENTS_LIST).map(([key, val]) => (
                <option key={key} value={key}>{val}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>MARKA / MODEL</label>
            <input
              className="brutal-input"
              value={editingInstrument?.brand || ''}
              onChange={e => setEditingInstrument({ ...editingInstrument, brand: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label>NUMER SERYJNY</label>
            <input
              className="brutal-input"
              value={editingInstrument?.number || ''}
              onChange={e => setEditingInstrument({ ...editingInstrument, number: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label>POSIADACZ</label>
            <select
              className="brutal-input brutal-select"
              value={editingInstrument?.newAssignmentId || ''}
              onChange={e => setEditingInstrument({ ...editingInstrument, newAssignmentId: e.target.value })}
            >
              <option value="">— W MAGAZYNIE —</option>
              {members?.map(m => (
                <option key={m.id} value={m.id}>{m.name}</option>
              ))}
            </select>
          </div>

          <div className="modal-actions">
            <button className="btn-simple edit" onClick={handleSaveInstrument}>ZAPISZ</button>
            <button className="btn-simple delete" onClick={() => setEditingInstrument(null)}>ANULUJ</button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

export default InstrumentsTab;