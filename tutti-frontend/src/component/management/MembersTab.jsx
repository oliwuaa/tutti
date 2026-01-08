import React, { useState } from 'react';
import Modal from '../EditBox.jsx';
import api from '../../api/axios';

const ROLE_TRANSLATIONS = {
    OWNER: 'WŁAŚCICIEL',
    ADMIN: 'ADMINISTRATOR',
    CONDUCTOR: 'DYRYGENT',
    LIBRARIAN: 'BIBLIOTEKARZ',
    MUSICIAN: 'MUZYK'
};

const ROLE_ORDER = {
    OWNER: 1,
    CONDUCTOR: 2,
    ADMIN: 3,
    LIBRARIAN: 4,
    MUSICIAN: 5
};

const INSTRUMENTS_LIST = {
    NONE: "Brak",
    PICCOLO: "Piccolo", FLUTE: "Flet poprzeczny", OBOE: "Obój", ENGLISH_HORN: "Rożek angielski",
    FAGOTT: "Fagot", CLARINET: "Klarnet", ALTO_CLARINET: "Klarnet altowy", BASS_CLARINET: "Klarnet basowy",
    SOPRANO_SAX: "Saksofon sopranowy", ALTO_SAX: "Saksofon altowy", TENOR_SAX: "Saksofon Tenorowy",
    BARITONE_SAX: "Saksofon barytonowy", TRUMPET: "Trąbka", CORNET: "Cornet", FLUGEL_HORN: "Skrzydłówka",
    FRENCH_HORN: "Waltornia", TROMBONE: "Puzon", EUPHONIUM: "Eufonium", TUBA: "Tuba",
    DRUM_KIT: "Perkusja", CYMBALS: "Talerze", BELLS: "Dzwonki", AUX_PERCUSSION: "Perkusonalia",
    KEYBOARD: "Klawisze", BASS_GUITAR: "Gitara basowa", ELECTRIC_GUITAR: "Gitara elektryczna"
};

const toRoman = (num) => {
    const romanMap = { 1: 'I', 2: 'II', 3: 'III', 4: 'IV' };
    return romanMap[num] || num;
};

function MembersTab({ members, refresh }) {
    const [memberSearch, setMemberSearch] = useState('');
    const [editingMember, setEditingMember] = useState(null);

    const ownerCount = members.filter(m => m.orchestraRole === 'OWNER').length;

    const sanitizeInstrument = (val) => {
        if (!val || val === "Brak" || val === "BRAK") return "NONE";
        return val;
    };

    const handleSaveMember = async () => {
        try {
            const instrumentType = sanitizeInstrument(editingMember.instrument);
            await Promise.all([
                api.put(`/memberships/${editingMember.id}/change-instrument`, {
                    instrumentType: instrumentType, 
                    partNumber: parseInt(editingMember.voice, 10)
                }),
                api.put(`/memberships/${editingMember.id}/change-role`, {
                    orchestraRole: editingMember.orchestraRole
                })
            ]);
            setEditingMember(null);
            refresh();
            alert("Zmiany zostały zapisane!");
        }  catch (err) { alert(err.message); }
    };

    const handleDelete = async (id) => {
        if (window.confirm("Czy na pewno chcesz usunąć tego członka?")) {
            try {
                await api.delete(`/memberships/${id}`);
                refresh();
            } catch (err) { alert(err.message); }
        }
    };

    const filteredMembers = [...members]
        .filter(m => (m.name || "").toLowerCase().includes(memberSearch.toLowerCase()))
        .sort((a, b) => {
            const weightA = ROLE_ORDER[a.orchestraRole] || 99;
            const weightB = ROLE_ORDER[b.orchestraRole] || 99;
            if (weightA !== weightB) return weightA - weightB;
            return (a.name || "").localeCompare(b.name || "");
        });

    return (
        <div className="tab-container">
            <div className="management-actions-bar">
                <input
                    type="text"
                    placeholder="SZUKAJ CZŁONKA..."
                    value={memberSearch}
                    onChange={(e) => setMemberSearch(e.target.value)}
                    className="brutal-input search-input"
                />
            </div>

            <div className="table-wrapper">
                <table className="flat-table">
                    <thead>
                        <tr>
                            <th>IMIĘ I NAZWISKO</th>
                            <th>ROLA</th>
                            <th>INSTRUMENT</th>
                            <th>GŁOS</th>
                            <th style={{ textAlign: 'right' }}>AKCJE</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredMembers.map(m => (
                            <tr key={m.id} className={`role-row-${m.orchestraRole?.toLowerCase()}`}>
                                <td className="cell-name primary-text">{m.name}</td>
                                <td className="cell-role">
                                    <span className={`role-badge ${m.orchestraRole?.toLowerCase()}`}>
                                        {ROLE_TRANSLATIONS[m.orchestraRole] || m.orchestraRole}
                                    </span>
                                </td>
                                <td className="cell-instrument tag-text">
                                    {INSTRUMENTS_LIST[m.instrument] || m.instrument}
                                </td>
                                <td className="cell-voice voice-text">GŁOS {toRoman(m.voice)}</td>
                                <td className="cell-actions">
                                    <div className="action-cell-group">
                                        <button 
                                            className="btn-simple edit" 
                                            onClick={() => setEditingMember({
                                                ...m,
                                                instrument: m.instrument || 'NONE',
                                                voice: m.voice || 1
                                            })}
                                        >
                                            EDYTUJ
                                        </button>
                                        <button 
                                            className="btn-simple delete" 
                                            onClick={() => handleDelete(m.id)} 
                                            disabled={m.orchestraRole === 'OWNER' && ownerCount <= 1}
                                        >
                                            USUŃ
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            <Modal isOpen={!!editingMember} onClose={() => setEditingMember(null)} title="EDYTUJ CZŁONKA">
                {editingMember && (
                    <div className="modal-form">
                        <div className="form-group">
                            <label>INSTRUMENT</label>
                            <select
                                className="brutal-input"
                                value={editingMember.instrument}
                                onChange={e => setEditingMember({ ...editingMember, instrument: e.target.value })}
                            >
                                {Object.entries(INSTRUMENTS_LIST).map(([key, label]) => (
                                    <option key={key} value={key}>{label}</option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
                            <label>GŁOS (NUMER PARTII)</label>
                            <select
                                className="brutal-input"
                                value={editingMember.voice?.toString() || "1"}
                                onChange={e => setEditingMember({ ...editingMember, voice: e.target.value })}
                            >
                                <option value="1">I (PIERWSZY)</option>
                                <option value="2">II (DRUGI)</option>
                                <option value="3">III (TRZECI)</option>
                                <option value="4">IV (CZWARTY)</option>
                            </select>
                        </div>

                        <div className="form-group">
                            <label>ROLA W ORKIESTRZE</label>
                            <select
                                className="brutal-input"
                                value={editingMember.orchestraRole}
                                onChange={e => setEditingMember({ ...editingMember, orchestraRole: e.target.value })}
                                disabled={editingMember.orchestraRole === 'OWNER' && ownerCount <= 1}
                            >
                                {Object.entries(ROLE_TRANSLATIONS).map(([key, label]) => (
                                    <option key={key} value={key}>{label}</option>
                                ))}
                            </select>
                            {editingMember.orchestraRole === 'OWNER' && ownerCount <= 1 && (
                                <small style={{ color: 'red', marginTop: '5px', display: 'block' }}>
                                    Nie można zmienić roli ostatniego właściciela.
                                </small>
                            )}
                        </div>

                        <div className="modal-actions">
                            <button className="btn-simple edit" onClick={handleSaveMember}>ZAPISZ</button>
                            <button className="btn-simple delete" onClick={() => setEditingMember(null)}>ANULUJ</button>
                        </div>
                    </div>
                )}
            </Modal>
        </div>
    );
}

export default MembersTab;