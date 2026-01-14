import React, { useState, useEffect } from 'react';
import Modal from '../EditBox.jsx';
import api from '../../api/axios';

const CLOTH_TYPES = {
  SHIRT: "Koszula",
  POLO: "Polo",
  COAT: "Płaszcz/Marynarka",
  BAG: "Pokrowiec/Torebka"
};

const SEX_TYPES = {
  UNISEX: "Unisex",
  MALE: "Męskie",
  FEMALE: "Damskie"
};

function ClothesTab({ orchestraId, members = [] }) {
  const [clothes, setClothes] = useState([]);
  const [expandedType, setExpandedType] = useState(null);
  const [expandedSex, setExpandedSex] = useState(null);
  const [editingCloth, setEditingCloth] = useState(null);
  const [newTypeModal, setNewTypeModal] = useState(false);
  const [localCategories, setLocalCategories] = useState([]);

  const fetchClothes = async () => {
    if (!orchestraId || orchestraId === "undefined") return;
    try {
      const res = await api.get(`/clothes/orchestra/${orchestraId}`);
      setClothes(res.data);
    } catch (err) {
      alert(err.message);
    }
  };

  useEffect(() => { fetchClothes(); }, [orchestraId]);

  const groupedClothes = clothes.reduce((acc, curr) => {
    const typeKey = curr.type;
    const sexKey = curr.sex;

    if (!acc[typeKey]) {
      acc[typeKey] = { id: typeKey, label: CLOTH_TYPES[typeKey] || typeKey, sexes: {} };
    }
    if (!acc[typeKey].sexes[sexKey]) {
      acc[typeKey].sexes[sexKey] = { label: SEX_TYPES[sexKey] || sexKey, items: [] };
    }
    acc[typeKey].sexes[sexKey].items.push(curr);
    return acc;
  }, {});

  localCategories.forEach(catKey => {
    if (!groupedClothes[catKey]) {
      groupedClothes[catKey] = { id: catKey, label: CLOTH_TYPES[catKey] || catKey, sexes: {} };
    }
  });

  const handleQuickAdd = async (clothTemplate) => {
    try {
      const payload = { ...clothTemplate, status: 'AVAILABLE' };
      await api.post(`/clothes/orchestra/${orchestraId}`, payload);
      fetchClothes();
    } catch (err) { alert(err.message); }
  };

  const handleDeleteOne = async (items) => {
    const available = items.filter(i => i.status === 'AVAILABLE');
    if (available.length === 0) {
      alert("Brak wolnych sztuk do usunięcia. Najpierw 'Zabierz' ubranie od muzyka.");
      return;
    }
    const toDelete = available[0];
    if (!window.confirm(`Usunąć jedną wolną sztukę rozmiaru ${toDelete.size}?`)) return;
    try {
      await api.delete(`/clothes/${toDelete.id}`);
      fetchClothes();
    } catch (err) { alert(err.message); }
  };

  const handleAssign = async (clothId, memberId) => {
    if (!memberId) return;
    try {
      await api.post(`/clothes/${clothId}/assign/${memberId}`);
      fetchClothes();
    } catch (err) { alert(err.message); }
  };

  const handleUnassign = async (clothId) => {
    if (!window.confirm("Odebrać ubranie muzykowi?")) return;
    try {
      await api.post(`/clothes/${clothId}/unassign`);
      fetchClothes();
    } catch (err) { alert(err.message); }
  };

  const handleSaveNew = async () => {
    try {
      await api.post(`/clothes/orchestra/${orchestraId}`, { ...editingCloth, status: 'AVAILABLE' });
      setLocalCategories(prev => prev.filter(c => c !== editingCloth.type));
      setEditingCloth(null);
      fetchClothes();
    } catch (err) { alert(err.message); }
  };

  return (
    <div className="management-container">
      <div className="global-actions">
        <button className="text-action-btn edit-btn" onClick={() => setNewTypeModal(true)}>
          + NOWY TYP UBRANIA
        </button>
      </div>

      <div className="folders-list">
        {Object.values(groupedClothes).map(typeGroup => {
          const isTypeExpanded = expandedType === typeGroup.id;
          return (
            <div key={typeGroup.id} className={`folder-row ${isTypeExpanded ? 'active' : ''}`}>
              <div className="folder-main" onClick={() => setExpandedType(isTypeExpanded ? null : typeGroup.id)}>
                <div className="folder-info">
                  <span className="primary-text">{typeGroup.label}</span>
                </div>
                <span className="arrow">{isTypeExpanded ? '−' : '+'}</span>
              </div>

              {isTypeExpanded && (
                <div className="folder-sub-content">
                  {Object.entries(typeGroup.sexes).map(([sexKey, sexData]) => {
                    const sexUniqueId = `${typeGroup.id}-${sexKey}`;
                    const isSexExpanded = expandedSex === sexUniqueId;

                    const sizes = sexData.items.reduce((acc, curr) => {
                      if (!acc[curr.size]) acc[curr.size] = [];
                      acc[curr.size].push(curr);
                      return acc;
                    }, {});

                    return (
                      <div key={sexKey} className="instrument-group">
                        <div className={`instrument-header ${isSexExpanded ? 'expanded' : ''}`}
                          onClick={() => setExpandedSex(isSexExpanded ? null : sexUniqueId)}>
                          <span>{sexData.label}</span>
                          <span className="count-pill">{sexData.items.length} szt.</span>
                        </div>

                        {isSexExpanded && (
                          <div className="instrument-voices">
                            {Object.entries(sizes).sort().map(([size, items]) => {
                              const assigned = items.filter(i => i.status === 'ASSIGNED');
                              const available = items.filter(i => i.status === 'AVAILABLE');

                              return (
                                <div key={size} className="voice-item" style={{ flexDirection: 'column', alignItems: 'stretch', gap: '10px' }}>
                                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                    <span className="voice-name">Rozmiar {size.replace('SIZE_', '')}</span>
                                    <div className="voice-management">
                                      <div className="chip clickable">
                                        <span className="count-text">
                                          {items.length} SZT. <small>({available.length} wolnych)</small>
                                        </span>
                                        <div className="mini-actions">
                                          <button className="mini-btn plus" onClick={() => handleQuickAdd(items[0])}>+</button>
                                          <button className="mini-btn minus" onClick={() => handleDeleteOne(items)}>−</button>
                                        </div>
                                      </div>
                                    </div>
                                  </div>

                                  <div className="owners-list" style={{ background: 'rgba(0,0,0,0.03)', padding: '5px' }}>
                                    {assigned.map(item => {
                                      const member = members.find(m => Number(m.id) === Number(item.membershipId));
                                      return (
                                        <div key={item.id} className="owner-item" style={{ marginBottom: '4px' }}>
                                          <span className="owner-name" style={{ fontSize: '0.8rem', color: '#d32f2f', fontWeight: '600' }}>
                                            • {member ? member.name : `Muzyk (ID: ${item.membershipId})`}
                                          </span>
                                          <button className="btn-simple delete" style={{ padding: '2px 6px', fontSize: '0.65rem' }} onClick={() => handleUnassign(item.id)}>
                                            ZABIERZ
                                          </button>
                                        </div>
                                      );
                                    })}

                                    {available.length > 0 && (
                                      <div className="owner-item">
                                        <select
                                          className="brutal-select-mini"
                                          style={{ width: '100%' }}
                                          key={`assign-select-${available[0].id}`}
                                          defaultValue=""
                                          onChange={(e) => {
                                            handleAssign(available[0].id, e.target.value);
                                            e.target.value = "";
                                          }}
                                        >
                                          <option value="" disabled>Przypisz sztukę do...</option>
                                          {[...members]
                                            .filter(m => {
                                              const alreadyHasExactMatch = clothes.some(cloth =>
                                                Number(cloth.membershipId) === Number(m.id) &&
                                                cloth.type === typeGroup.id &&
                                                cloth.size === size &&
                                                cloth.status === 'ASSIGNED'
                                              );

                                              return !alreadyHasExactMatch;
                                            })
                                            .sort((a, b) => a.name.localeCompare(b.name))
                                            .map(m => (
                                              <option key={m.id} value={m.id}>
                                                {m.name}
                                              </option>
                                            ))
                                          }
                                        </select>
                                      </div>
                                    )}
                                  </div>
                                </div>
                              );
                            })}
                          </div>
                        )}
                      </div>
                    );
                  })}
                  <button className="btn-simple edit full-width"
                    onClick={() => setEditingCloth({ type: typeGroup.id, sex: 'UNISEX', size: 'M' })}>
                    + DODAJ NOWY ROZMIAR/PŁEĆ
                  </button>
                </div>
              )}
            </div>
          );
        })}
      </div>

      <Modal isOpen={newTypeModal} onClose={() => setNewTypeModal(false)} title="DODAJ TYP UBRANIA">
        <div className="modal-form">
          <label className="form-label-small">WYBIERZ KATEGORIĘ</label>
          <select
            className="brutal-input brutal-select"
            value=""
            onChange={e => {
              const val = e.target.value;
              if (!localCategories.includes(val)) setLocalCategories([...localCategories, val]);
              setExpandedType(val);
              setNewTypeModal(false);
            }}
          >
            <option value="" disabled>-- WYBIERZ --</option>
            {Object.entries(CLOTH_TYPES)
              .filter(([key]) => !groupedClothes[key])
              .map(([k, v]) => (
                <option key={k} value={k}>{v}</option>
              ))
            }
          </select>
          {Object.keys(CLOTH_TYPES).every(key => groupedClothes[key]) && (
            <p style={{ fontSize: '0.7rem', color: '#666', marginTop: '10px' }}>
              Wszystkie dostępne typy ubrań zostały już dodane.
            </p>
          )}
        </div>
      </Modal>

      <Modal isOpen={!!editingCloth} onClose={() => setEditingCloth(null)} title="DODAJ ROZMIAR">
        <div className="modal-form">
          <label className="form-label-small">PŁEĆ / KRÓJ</label>
          <select className="brutal-input brutal-select" value={editingCloth?.sex} onChange={e => setEditingCloth({ ...editingCloth, sex: e.target.value })}>
            {Object.entries(SEX_TYPES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
          <label className="form-label-small" style={{ marginTop: '10px' }}>ROZMIAR</label>
          <select className="brutal-input brutal-select" value={editingCloth?.size} onChange={e => setEditingCloth({ ...editingCloth, size: e.target.value })}>
            {['XS', 'S', 'M', 'L', 'XL', 'XXL'].map(s => <option key={s} value={s}>{s}</option>)}
            {[36, 38, 40, 42, 44, 46, 48, 50, 52].map(n => <option key={n} value={`SIZE_${n}`}>{n}</option>)}
          </select>
          <button className="btn-simple edit" style={{ marginTop: '20px' }} onClick={handleSaveNew}>ZAPISZ</button>
        </div>
      </Modal>
    </div>
  );
}

export default ClothesTab;