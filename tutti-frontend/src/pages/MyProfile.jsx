import React, { useState, useEffect } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import api from '../api/axios';
import ProfileInfo from '../component/profile/ProfileInfo';
import OwnedOrchestras from '../component/profile/OwnedOrchestras';
import Memberships from '../component/profile/Memberships';
import '../styles/Events.css';
import '../styles/MyProfiles.css';

const INSTRUMENT_TYPES = {
    PICCOLO: "Piccolo", FLUTE: "Flet poprzeczny", OBOE: "Obój", ENGLISH_HORN: "Rożek angielski",
    FAGOTT: "Fagot", CLARINET: "Klarnet", ALTO_CLARINET: "Klarnet altowy", BASS_CLARINET: "Klarnet basowy",
    SOPRANO_SAX: "Saksofon sopranowy", ALTO_SAX: "Saksofon altowy", TENOR_SAX: "Saksofon Tenorowy",
    BARITONE_SAX: "Saksofon barytonowy", TRUMPET: "Trąbka", CORNET: "Cornet", FLUGEL_HORN: "Skrzydłówka",
    FRENCH_HORN: "Waltornia", TROMBONE: "Puzon", EUPHONIUM: "Eufonium", TUBA: "Tuba",
    DRUM_KIT: "Perkusja", CYMBALS: "Talerze", BELLS: "Dzwonki", AUX_PERCUSSION: "Perkusonalia",
    KEYBOARD: "Klawisze", BASS_GUITAR: "Gitara basowa", ELECTRIC_GUITAR: "Gitara elektryczna", NONE: "Brak"
};

const toRoman = (num) => {
    const romanMap = { 1: 'I', 2: 'II', 3: 'III', 4: 'IV' };
    return romanMap[num] || num;
};

function MyProfile() {
    const navigate = useNavigate();
    const { user, setUser, onSelectOrchestra, isSystemAdmin, refreshDashboard } = useOutletContext();

    const [localIsEditing, setLocalIsEditing] = useState(false);
    const [formData, setFormData] = useState({ firstName: '', lastName: '' });
    const [ownedOrchestras, setOwnedOrchestras] = useState([]);
    const [allMemberships, setAllMemberships] = useState([]);
    const [expandedId, setExpandedId] = useState(null);
    const [equipmentData, setEquipmentData] = useState({});
    const [isAddingOrchestra, setIsAddingOrchestra] = useState(false);
    const [isCreatingOrchestra, setIsCreatingOrchestra] = useState(false);
    const [editingOrchestraId, setEditingOrchestraId] = useState(null);
    const [createForm, setCreateForm] = useState({ name: "", address: "" });
    const [joinForm, setJoinForm] = useState({ orchestraId: "", instrumentType: "NONE" });
    const [availableOrchestras, setAvailableOrchestras] = useState([]);

    useEffect(() => {
        if (user) {
            setFormData({ firstName: user.firstName || '', lastName: user.lastName || '' });
            fetchOwnedOrchestras();
            fetchAllMemberships();
        }
    }, [user]);

    useEffect(() => {
        if (isAddingOrchestra) {
            api.get('/orchestras').then(res => {

                const filtered = res.data.filter(orch =>
                    !ownedOrchestras.some(owned => owned.id === orch.id) &&
                    !allMemberships.some(m => m.orchestraId === orch.id && (m.status === 'ACTIVE' || m.status === 'PENDING'))
                );
                setAvailableOrchestras(filtered);
            }).catch(err => console.error(err));
        }
    }, [isAddingOrchestra, ownedOrchestras, allMemberships]);

    const fetchOwnedOrchestras = async () => {
        try {
            const res = await api.get(`/orchestras/owner/${user.id}`);
            setOwnedOrchestras(res.data);
        }  catch (err) { alert(err.message); }
    };

    const fetchAllMemberships = async () => {
        try {
            const res = await api.get(`/memberships/user/${user.id}/active-memberships`);
            setAllMemberships(res.data || []);
        } catch (err) { alert(err.message); }
    };

    const fetchEquipment = async (membershipId) => {
        if (equipmentData[membershipId]) return;
        try {
            const [clothesRes, instrumentsRes] = await Promise.all([
                api.get(`/clothes/membership/${membershipId}`),
                api.get(`/instruments/membership/${membershipId}`)
            ]);
            setEquipmentData(prev => ({
                ...prev, [membershipId]: { clothes: clothesRes.data, instruments: instrumentsRes.data }
            }));
        }  catch (err) { alert(err.message); }
    };

    const handleToggleEdit = async () => {
        if (localIsEditing) {
            try {
                const res = await api.put(`/users/${user.id}`, formData);
                if (setUser) setUser(res.data);
                setLocalIsEditing(false);
                alert("Dane zapisane!");
            }  catch (err) { alert(err.message);  }
        } else setLocalIsEditing(true);
    };

    const handleCreateOrUpdateOrchestra = async () => {
        if (!createForm.name.trim()) return;
        try {
            const payload = { name: createForm.name.toUpperCase(), address: createForm.address };
            if (editingOrchestraId) await api.put(`/orchestras/${editingOrchestraId}`, payload);
            else await api.post('/orchestras', payload);

            await fetchOwnedOrchestras();

            if (refreshDashboard) {
                await refreshDashboard();
            }

            setIsCreatingOrchestra(false);
            setCreateForm({ name: "", address: "" });
        }  catch (err) { alert(err.message); }
    };

    const handleDeleteOrchestra = async (e, id, name) => {
        e.stopPropagation();
        if (window.confirm(`Czy na pewno chcesz USUNĄĆ orkiestrę ${name}?`)) {
            await api.delete(`/orchestras/${id}`);

            await fetchOwnedOrchestras();
            if (refreshDashboard) {
                await refreshDashboard();
            }
        }
    };

    const handleManageOrchestra = (orch) => {
        if (onSelectOrchestra) onSelectOrchestra(orch);
        navigate('/dashboard/management');
    };

    const handleSendJoinRequest = async () => {
        if (!joinForm.orchestraId) return alert("Wybierz orkiestrę!");
        try {
            await api.post('/memberships', {
                orchestraId: parseInt(joinForm.orchestraId),
                userId: user.id,
                orchestraRole: "MUSICIAN",
                instrumentType: joinForm.instrumentType,
                partNumber: 1
            });
            fetchAllMemberships();
            setIsAddingOrchestra(false);
        }  catch (err) { alert(err.message); }
    };

    const handleRemoveMembership = async (id, name, isPending) => {
        const msg = isPending ? `Wycofać prośbę do ${name}?` : `Opuścić ${name}?`;
        if (window.confirm(msg)) {
            await api.delete(`/memberships/${id}`);
            fetchAllMemberships();
        }
    };

    return (
        <div className="calendar-view">
            <header className="view-header">
                <div className="header-left">
                    {!isSystemAdmin && (
                        <button className="back-link" onClick={() => navigate('/dashboard')}>
                            ← WRÓĆ DO PULPITU
                        </button>
                    )}
                    <h1 className="welcome-title">MÓJ PROFIL</h1>
                </div>
                <button
                    className="text-action-btn edit-btn"
                    onClick={handleToggleEdit}
                    style={{ background: localIsEditing ? '#544013' : '#a39071' }}
                >
                    {localIsEditing ? "ZAPISZ DANE" : "EDYTUJ PROFIL"}
                </button>
            </header>

            <div className="details-grid">
                <ProfileInfo
                    localIsEditing={localIsEditing}
                    formData={formData}
                    setFormData={setFormData}
                    user={user}
                />

                <div className="orchestras-column">
                    <OwnedOrchestras
                        ownedOrchestras={ownedOrchestras}
                        onManageOrchestra={handleManageOrchestra}
                        handleStartEditOrchestra={(e, orch) => {
                            setEditingOrchestraId(orch.id);
                            setCreateForm({ name: orch.name, address: orch.address });
                            setIsCreatingOrchestra(true);
                        }}
                        handleDeleteOrchestra={handleDeleteOrchestra}
                        isCreatingOrchestra={isCreatingOrchestra}
                        setIsCreatingOrchestra={setIsCreatingOrchestra}
                        createForm={createForm}
                        setCreateForm={setCreateForm}
                        handleCreateOrUpdateOrchestra={handleCreateOrUpdateOrchestra}
                        setEditingOrchestraId={setEditingOrchestraId}
                    />

                    <Memberships
                        activeMemberships={allMemberships.filter(m => m.status === 'ACTIVE')}
                        pendingRequests={allMemberships.filter(m => m.status === 'PENDING')}
                        expandedId={expandedId}
                        handleToggleExpand={(id) => {
                            if (expandedId === id) setExpandedId(null);
                            else { setExpandedId(id); fetchEquipment(id); }
                        }}
                        equipmentData={equipmentData}
                        handleRemoveMembership={handleRemoveMembership}
                        isAddingOrchestra={isAddingOrchestra}
                        setIsAddingOrchestra={setIsAddingOrchestra}
                        availableOrchestras={availableOrchestras}
                        joinForm={joinForm}
                        setJoinForm={setJoinForm}
                        handleSendJoinRequest={handleSendJoinRequest}
                        INSTRUMENT_TYPES={INSTRUMENT_TYPES}
                        toRoman={toRoman}
                    />
                </div>
            </div>
        </div>
    );
}

export default MyProfile;