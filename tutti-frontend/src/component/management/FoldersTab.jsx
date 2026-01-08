import React, { useState, useEffect } from 'react';
import Modal from '../EditBox.jsx';
import api from '../../api/axios';

const MARCHING_TYPES = {
  CHURCH_SONGS: "Pieśni kościelne",
  FUNERAL_SONGS: "Pieśni pogrzebowe",
  ENTERTAINMENT: "Utwory rozrywkowe",
  MARCHES: "Marsze"
};

const INSTRUMENTS_LIST = {
  PICCOLO: "Piccolo", FLUTE: "Flet poprzeczny", OBOE: "Obój", ENGLISH_HORN: "Rożek angielski",
  FAGOTT: "Fagot", CLARINET: "Klarnet", ALTO_CLARINET: "Klarnet altowy", BASS_CLARINET: "Klarnet basowy",
  SOPRANO_SAX: "Saksofon sopranowy", ALTO_SAX: "Saksofon altowy", TENOR_SAX: "Saksofon Tenorowy",
  BARITONE_SAX: "Saksofon barytonowy", TRUMPET: "Trąbka", CORNET: "Cornet", FLUGEL_HORN: "Skrzydłówka",
  FRENCH_HORN: "Waltornia", TROMBONE: "Puzon", EUPHONIUM: "Eufonium", TUBA: "Tuba",
  DRUM_KIT: "Perkusja", CYMBALS: "Talerze", BELLS: "Dzwonki", AUX_PERCUSSION: "Perkusonalia",
  KEYBOARD: "Klawisze", BASS_GUITAR: "Gitara basowa", ELECTRIC_GUITAR: "Gitara elektryczna", NONE: "Brak"
};

const toRoman = (num) => {
  const map = { "1": "I", "2": "II", "3": "III", "4": "IV" };
  return map[num?.toString()] || num;
};

function FoldersTab({ orchestraId }) {
  const [stats, setStats] = useState([]);
  const [expandedFolder, setExpandedFolder] = useState(null);
  const [expandedInstrument, setExpandedInstrument] = useState(null);
  const [editingVoice, setEditingVoice] = useState(null);
  const [newFolderModal, setNewFolderModal] = useState(false);
  const [localCategories, setLocalCategories] = useState([]);

  const fetchStats = async () => {
    if (!orchestraId) return;
    try {
      const res = await api.get(`/marching-folders/orchestra/${orchestraId}/stats`);
      setStats(res.data);
    }  catch (err) { alert(err.message); }
  };

  useEffect(() => { fetchStats(); }, [orchestraId]);

  const groupedFolders = stats.reduce((acc, curr) => {
    const typeKey = curr.marchingType;
    const instKey = curr.instrumentType;

    if (!acc[typeKey]) {
      acc[typeKey] = {
        id: typeKey,
        category: MARCHING_TYPES[typeKey] || typeKey,
        instruments: {}
      };
    }

    if (!acc[typeKey].instruments[instKey]) {
      acc[typeKey].instruments[instKey] = {
        name: INSTRUMENTS_LIST[instKey] || instKey,
        voices: []
      };
    }

    acc[typeKey].instruments[instKey].voices.push(curr);
    return acc;
  }, {});

  localCategories.forEach(catKey => {
    if (!groupedFolders[catKey]) {
      groupedFolders[catKey] = {
        id: catKey,
        category: MARCHING_TYPES[catKey] || catKey,
        instruments: {}
      };
    }
  });

  const handleUpdateQuantity = async (folderId, action) => {
    try {
      await api.patch(`/marching-folders/${folderId}/${action}`);
      fetchStats();
    } catch (err) { alert(err.message); }
  };

  const handleDeleteVoice = async (folderId) => {
    if (!window.confirm("Usunąć ten głos?")) return;
    try {
      await api.delete(`/marching-folders/${folderId}`);
      fetchStats();
    }  catch (err) { alert(err.message); }
  };

  const handleSaveVoice = async () => {
    try {
      const payload = {
        marchingType: editingVoice.marchingType,
        instrumentType: editingVoice.instrumentType,
        partNumber: parseInt(editingVoice.partNumber) || 1
      };
      await api.post(`/marching-folders/orchestra/${orchestraId}`, payload);
      setLocalCategories(prev => prev.filter(c => c !== editingVoice.marchingType));
      setEditingVoice(null);
      fetchStats();
    } catch (err) { alert(err.message); }
  };

  return (
    <div className="management-container">
      <div className="global-actions">
        <button className="text-action-btn edit-btn" onClick={() => setNewFolderModal(true)}>
          + NOWA KATEGORIA TECZEK
        </button>
      </div>

      <div className="folders-list">
        {Object.values(groupedFolders).map(folder => {
          const isFolderExpanded = expandedFolder === folder.id;
          return (
            <div key={folder.id} className={`folder-row ${isFolderExpanded ? 'active' : ''}`}>
              <div className="folder-main" onClick={() => setExpandedFolder(isFolderExpanded ? null : folder.id)}>
                <div className="folder-info">

                  <span className="primary-text">{folder.category}</span>
                </div>
                <span className="arrow">{isFolderExpanded ? '−' : '+'}</span>
              </div>

              {isFolderExpanded && (
                <div className="folder-sub-content">
                  {Object.entries(folder.instruments).length > 0 ? (
                    Object.entries(folder.instruments)
                      .sort(([keyA, dataA], [keyB, dataB]) => dataA.name.localeCompare(dataB.name))
                      .map(([instKey, instData]) => {
                        const instUniqueId = `${folder.id}-${instKey}`;
                        const isInstExpanded = expandedInstrument === instUniqueId;

                        return (
                          <div key={instKey} className="instrument-group">
                            <div
                              className={`instrument-header ${isInstExpanded ? 'expanded' : ''}`}
                              onClick={() => setExpandedInstrument(isInstExpanded ? null : instUniqueId)}
                            >
                              <span>{instData.name}</span>
                              <span className="count-pill">{instData.voices.length} głosy</span>
                            </div>

                            {isInstExpanded && (
                              <div className="instrument-voices">
                                {instData.voices
                                  .sort((a, b) => a.partNumber - b.partNumber)
                                  .map((v) => (
                                    <div key={v.id} className="voice-item">
                                      <span className="voice-name">Głos {toRoman(v.partNumber)}</span>
                                      <div className="voice-management">
                                        <div className="chip clickable">
                                          <span>{v.quantity} SZT.</span>
                                          <div className="mini-actions">
                                            <button className="mini-btn" onClick={() => handleUpdateQuantity(v.id, 'increment')}>+</button>
                                            <button className="mini-btn" onClick={() => handleUpdateQuantity(v.id, 'decrement')}>−</button>
                                            <button className="mini-btn delete" onClick={() => handleDeleteVoice(v.id)}>x</button>
                                          </div>
                                        </div>
                                      </div>
                                    </div>
                                  ))}
                              </div>
                            )}
                          </div>
                        );
                      })
                  ) : (
                    <p style={{ padding: '20px', fontSize: '0.8rem', opacity: 0.5, textAlign: 'center' }}>
                      Brak instrumentów w tej kategorii. Dodaj pierwszy głos.
                    </p>
                  )}

                  <button
                    className="btn-simple edit full-width"
                    onClick={() => setEditingVoice({ marchingType: folder.id, instrumentType: 'TRUMPET', partNumber: '1' })}
                  >
                    + DODAJ GŁOS DO {folder.category.toUpperCase()}
                  </button>
                </div>
              )}
            </div>
          );
        })}
      </div>

      <Modal isOpen={newFolderModal} onClose={() => setNewFolderModal(false)} title="DODAJ KATEGORIĘ">
        <div className="modal-form">
          <label className="form-label-small">WYBIERZ TYP MARSZU</label>
          <select
            className="brutal-input brutal-select"
            value="" 
            onChange={e => {
              const val = e.target.value;
              if (!val) return;
              if (!localCategories.includes(val)) setLocalCategories([...localCategories, val]);
              setExpandedFolder(val);
              setNewFolderModal(false);
            }}
          >
            <option value="" disabled>-- WYBIERZ --</option>
            {Object.entries(MARCHING_TYPES).map(([key, val]) => (
              <option key={key} value={key}>{val}</option>
            ))}
          </select>
        </div>
      </Modal>

      <Modal isOpen={!!editingVoice} onClose={() => setEditingVoice(null)} title="DODAJ NOWY GŁOS">
        <div className="modal-form">
          <div className="form-group">
            <label>INSTRUMENT</label>
            <select
              className="brutal-input brutal-select"
              value={editingVoice?.instrumentType}
              onChange={e => setEditingVoice({ ...editingVoice, instrumentType: e.target.value })}
            >
              {Object.entries(INSTRUMENTS_LIST).map(([key, val]) => (
                <option key={key} value={key}>{val}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>GŁOS (NUMER)</label>
            <select
              className="brutal-input brutal-select"
              value={editingVoice?.partNumber}
              onChange={e => setEditingVoice({ ...editingVoice, partNumber: e.target.value })}
            >
              <option value="1">I (Pierwszy)</option>
              <option value="2">II (Drugi)</option>
              <option value="3">III (Trzeci)</option>
              <option value="4">IV (Czwarty)</option>
            </select>
          </div>

          <div className="modal-actions">
            <button className="btn-simple edit" onClick={handleSaveVoice}>DODAJ NUTY</button>
            <button className="btn-simple delete" onClick={() => setEditingVoice(null)}>ANULUJ</button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

export default FoldersTab;