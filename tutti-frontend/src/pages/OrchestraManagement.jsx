import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useOutletContext, useParams } from 'react-router-dom';
import api from '../api/axios';
import MembersTab from '../component/management/MembersTab';
import InstrumentsTab from '../component/management/InstrumentsTab';
import ClothesTab from '../component/management/ClothesTab';
import FoldersTab from '../component/management/FoldersTab';
import RequestsTab from '../component/management/RequestsTab';
import '../styles/Management.css';

function OrchestraManagement() {
    const navigate = useNavigate();
    const { orchId } = useParams(); 
    const { userOrchestra, setPendingRequests } = useOutletContext();

    const [activeTab, setActiveTab] = useState('members');
    const [loading, setLoading] = useState(false);
    const [adminOrchestraName, setAdminOrchestraName] = useState(null);

    const [members, setMembers] = useState([]);
    const [pendingRequestsList, setPendingRequestsList] = useState([]);
    const [instruments, setInstruments] = useState([]);

    const effectiveOrchId = orchId || userOrchestra?.id;

    const fetchData = useCallback(async () => {
        if (!effectiveOrchId) return;

        setLoading(true);
        try {
            if (orchId) {
                const infoRes = await api.get(`/orchestras/${orchId}`);
                setAdminOrchestraName(infoRes.data.name);
            }

            const [activeRes, requestsRes, instRes] = await Promise.all([
                api.get(`/memberships/orchestra/${effectiveOrchId}/active`),
                api.get(`/memberships/orchestra/${effectiveOrchId}/status/PENDING`),
                api.get(`/instruments/orchestra/${effectiveOrchId}`)
            ]);

            const mappedMembers = activeRes.data.map(m => ({
                id: m.id,
                name: (m.firstName && m.lastName)
                    ? `${m.firstName} ${m.lastName}`.toUpperCase()
                    : (m.firstName || m.lastName || "NIEZNANY CZŁONEK"),
                instrument: (!m.instrumentType || m.instrumentType.toLowerCase() === 'none') ? "BRAK" : m.instrumentType,
                voice: m.partNumber ? m.partNumber.toString() : "I",
                orchestraRole: m.orchestraRole || "MUSICIAN"
            }));
            
            setMembers(mappedMembers);
            setPendingRequestsList(requestsRes.data);
            setInstruments(instRes.data || []);

            if (setPendingRequests && !orchId) {
                setPendingRequests(requestsRes.data);
            }

        }  catch (err) { alert(err.message); } finally {
            setLoading(false);
        }
    }, [effectiveOrchId, orchId, setPendingRequests]);

    useEffect(() => {
        if (effectiveOrchId) {
            fetchData();
        } else {
            const timeout = setTimeout(() => {
                if (!effectiveOrchId) navigate('/dashboard');
            }, 500);
            return () => clearTimeout(timeout);
        }
    }, [effectiveOrchId, fetchData, navigate]);

    if (!effectiveOrchId) return null;

    const displayName = orchId 
        ? adminOrchestraName 
        : (userOrchestra?.orchestraName || userOrchestra?.name);

    return (
        <div className="management-view">
            <header className="view-header">
                <div className="header-left">
                    <button className="back-link" onClick={() => navigate(-1)}>← POWRÓT</button>
                    <h1 className="welcome-title">ZARZĄDZANIE ORKIESTRĄ</h1>
                    <p className="welcome-subtitle">
                        ORKIESTRA: {displayName || "ŁADOWANIE..."}
                        {loading && <span className="sync-text"> (SYNCHRONIZACJA...)</span>}
                    </p>
                </div>
            </header>

            <nav className="management-tabs">
                <button
                    className={activeTab === 'members' ? 'active' : ''}
                    onClick={() => setActiveTab('members')}
                >
                    CZŁONKOWIE ({members.length})
                </button>
                <button
                    className={activeTab === 'requests' ? 'active' : ''}
                    onClick={() => setActiveTab('requests')}
                >
                    ZGŁOSZENIA ({pendingRequestsList.length})
                </button>
                <button className={activeTab === 'instruments' ? 'active' : ''} onClick={() => setActiveTab('instruments')}>INSTRUMENTY</button>
                <button className={activeTab === 'clothes' ? 'active' : ''} onClick={() => setActiveTab('clothes')}>UBRANIA</button>
                <button className={activeTab === 'folders' ? 'active' : ''} onClick={() => setActiveTab('folders')}>TECZKI</button>
            </nav>

            <div className="management-content">
                {activeTab === 'members' && (
                    <MembersTab members={members} refresh={fetchData} />
                )}

                {activeTab === 'requests' && (
                    <RequestsTab requests={pendingRequestsList} refresh={fetchData} />
                )}

                {activeTab === 'instruments' && (
                    <InstrumentsTab
                        instruments={instruments}
                        refresh={fetchData}
                        members={members}
                        orchestraId={effectiveOrchId}
                    />
                )}

                {activeTab === 'clothes' && (
                    <ClothesTab
                        orchestraId={effectiveOrchId}
                        refresh={fetchData}
                        members={members}
                    />
                )}

                {activeTab === 'folders' && (
                    <FoldersTab
                        orchestraId={effectiveOrchId}
                        refresh={fetchData}
                    />
                )}
            </div>
        </div>
    );
}

export default OrchestraManagement;