import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import api from '../api/axios';
import OrchestraTable from '../component/admin/OrchestraTable';
import UserTable from '../component/admin/UserTable';
import EditUserModal from '../component/admin/EditUserModal';
import '../styles/Management.css';
import '../styles/Panel.css';

function SystemAdminPanel() {
    const navigate = useNavigate();
    const { user } = useOutletContext();

    const [activeTab, setActiveTab] = useState('orchestras');
    const [expandedOrchId, setExpandedOrchId] = useState(null);
    const [expandedUserOrchs, setExpandedUserOrchs] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [userSearchTerm, setUserSearchTerm] = useState('');
    const [roleFilter, setRoleFilter] = useState('ALL');
    const [editingMembershipId, setEditingMembershipId] = useState(null);
    const [editingUser, setEditingUser] = useState(null);

    const [selectedUserToAdd, setSelectedUserToAdd] = useState('');
    const [selectedRoleToAdd, setSelectedRoleToAdd] = useState('MUSICIAN');

    const [allOrchestras, setAllOrchestras] = useState([]);
    const [allUsers, setAllUsers] = useState([]);
    const [allMemberships, setAllMemberships] = useState([]);
    const [loading, setLoading] = useState(true);

    const roleWeights = { 'OWNER': 1, 'CONDUCTOR': 2, 'ADMIN': 3, 'LIBRARIAN': 4, 'MUSICIAN': 5 };

    const fetchData = useCallback(async () => {
        try {
            setLoading(true);
            const [orchRes, userRes, membRes] = await Promise.all([
                api.get('/orchestras'),
                api.get('/users'),
                api.get('/memberships/active')
            ]);
            setAllOrchestras(orchRes.data);
            setAllUsers(userRes.data);
            setAllMemberships(membRes.data);
        }  catch (err) { alert(err.message); 
            if (error.response?.status === 403) navigate('/profile');
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        if (user && user.role !== 'ADMIN') { navigate('/profile'); return; }
        fetchData();
    }, [fetchData, user, navigate]);

    const updateMemberRole = async (membershipId, newRole) => {

        try {
            await api.put(`/memberships/${membershipId}/change-role`, {
                orchestraRole: newRole
            });
            await fetchData();

        }  catch (err) { alert(err.message); }

    };



    const addNewMember = async (orchestraId) => {
        if (!selectedUserToAdd) return alert("Wybierz użytkownika!");
        try {
            await api.post(`/memberships`, {
                orchestraId: orchestraId,
                userId: selectedUserToAdd,
                orchestraRole: selectedRoleToAdd,
                status: 'ACTIVE'
            });
            setSelectedUserToAdd('');
            await fetchData();

        }  catch (err) { alert(err.message); }
    };

    const deleteOrchestra = async (id) => {
        if (window.confirm("CZY NA PEWNO CHCESZ USUNĄĆ TĘ ORKIESTRĘ?")) {
            try {
                await api.delete(`/orchestras/${id}`);
                setAllOrchestras(prev => prev.filter(o => o.id !== id));
            }  catch (err) { alert(err.message); }
        }
    };



    const removeUserFromOrchestra = async (membershipId) => {
        if (window.confirm("USUNĄĆ UŻYTKOWNIKA Z TEJ ORKIESTRY?")) {
            try {
                await api.delete(`/memberships/${membershipId}`);
                await fetchData();
            }  catch (err) { alert(err.message); }
        }
    };

    const deleteUserGlobally = async (userId) => {
        if (window.confirm("CZY NA PEWNO CHCESZ CAŁKOWICIE USUNĄĆ TEGO UŻYTKOWNIKA?")) {
            try {
                await api.delete(`/users/${userId}`);
                setAllUsers(prev => prev.filter(u => u.id !== userId));
                setAllMemberships(prev => prev.filter(m => m.userId !== userId));
            }  catch (err) { alert(err.message); }
        }
    };

    const handleSaveEdit = async (e) => {
        e.preventDefault();
        try {
            await api.put(`/users/${editingUser.id}`, {
                firstName: editingUser.firstName,
                lastName: editingUser.lastName,
                role: editingUser.role
            });
            setEditingUser(null);
            await fetchData();
        }  catch (err) { alert(err.message); }

    };

    const filteredOrchestras = allOrchestras.filter(o => {
        const term = searchTerm.toLowerCase();
        return o.name.toLowerCase().includes(term) || allMemberships.some(m => m.orchestraId === o.id && (`${m.firstName} ${m.lastName}`).toLowerCase().includes(term));
    }).sort((a, b) => a.name.localeCompare(b.name));

    const filteredUsers = allUsers.filter(u => {
        const fullName = `${u.firstName} ${u.lastName}`.toLowerCase();
        const matchesSearch = u.email.toLowerCase().includes(userSearchTerm.toLowerCase()) || fullName.includes(userSearchTerm.toLowerCase());
        const matchesRole = roleFilter === 'ALL' || u.role === roleFilter;
        return matchesSearch && matchesRole;
    });

    if (loading) return <div className="management-view"><h1 className="welcome-title">ŁADOWANIE DANYCH...</h1></div>;

    return (
        <div className="management-view">
            <header className="view-header">
                <div className="header-left">
                    <h1 className="welcome-title">PANEL ADMINA TUTTI.</h1>
                    <p className="welcome-subtitle">ZARZĄDZASZ CAŁĄ PLATFORMĄ</p>
                </div>
            </header>

            <div className="admin-stats-container">
                <div className="stat-card-simple">
                    <small>ORKIESTRY</small>
                    <div className="primary-text-large">{allOrchestras.length}</div>
                </div>
                <div className="stat-card-simple">
                    <small>MUZYCY</small>
                    <div className="primary-text-large">{allUsers.length}</div>
                </div>
            </div>

            <nav className="management-tabs">
                <button className={activeTab === 'orchestras' ? 'active' : ''} onClick={() => setActiveTab('orchestras')}>ORKIESTRY</button>
                <button className={activeTab === 'users' ? 'active' : ''} onClick={() => setActiveTab('users')}>UŻYTKOWNICY</button>
            </nav>

            <div className="management-content">
                {activeTab === 'orchestras' && (
                    <div className="tab-container">
                        <div className="management-actions-bar">
                            <input type="text" placeholder="SZUKAJ..." className="brutal-input search-input" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
                        </div>
                        <OrchestraTable
                            orchestras={filteredOrchestras}
                            memberships={allMemberships}
                            expandedId={expandedOrchId}
                            toggleExpand={(id) => setExpandedOrchId(expandedOrchId === id ? null : id)}
                            onDelete={deleteOrchestra}
                            onManage={(id) => navigate(`/dashboard/management/${id}`)}
                            onUpdateRole={updateMemberRole}
                            onRemoveMember={removeUserFromOrchestra}
                            onAddMember={addNewMember}
                            users={allUsers}
                            selectedUserToAdd={selectedUserToAdd}
                            setSelectedUserToAdd={setSelectedUserToAdd}
                            selectedRoleToAdd={selectedRoleToAdd}
                            setSelectedRoleToAdd={setSelectedRoleToAdd}
                            roleWeights={roleWeights}
                            editingMembershipId={editingMembershipId}
                            setEditingMembershipId={setEditingMembershipId}
                        />
                    </div>
                )}

                {activeTab === 'users' && (
                    <div className="tab-container">
                        <div className="management-actions-bar user-bar-layout">
                            <input type="text" placeholder="SZUKAJ..." className="brutal-input search-input flex-grow-2" value={userSearchTerm} onChange={(e) => setUserSearchTerm(e.target.value)} />
                            <select className="brutal-input flex-grow-1" value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
                                <option value="ALL">WSZYSTKIE ROLE</option>
                                <option value="USER">MUZYK (USER)</option>
                                <option value="ADMIN">ADMIN SYSTEMU</option>
                            </select>
                        </div>
                        <UserTable
                            users={filteredUsers}
                            memberships={allMemberships}
                            expandedUserOrchs={expandedUserOrchs}
                            toggleUserOrchs={(id) => setExpandedUserOrchs(expandedUserOrchs === id ? null : id)}
                            onEdit={setEditingUser}
                            onDelete={deleteUserGlobally}
                            onRemoveFromOrchestra={removeUserFromOrchestra}
                        />
                    </div>
                )}
            </div>

            <EditUserModal
                user={editingUser}
                onSave={handleSaveEdit}
                onCancel={() => setEditingUser(null)}
                onChange={setEditingUser}
            />
        </div>
    );
}

export default SystemAdminPanel;