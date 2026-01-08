import React from 'react';

function OrchestraTable({ 
    orchestras, memberships, expandedId, toggleExpand, 
    onDelete, onManage, onUpdateRole, onRemoveMember, onAddMember,
    users, selectedUserToAdd, setSelectedUserToAdd, selectedRoleToAdd, setSelectedRoleToAdd,
    roleWeights, editingMembershipId, setEditingMembershipId
}) {
    return (
        <div className="table-wrapper">
            <table className="flat-table">
                <thead>
                    <tr>
                        <th>NAZWA ORKIESTRY</th>
                        <th>WŁAŚCICIEL</th>
                        <th>CZŁONKOWIE</th>
                        <th className="cell-actions-header">AKCJE</th>
                    </tr>
                </thead>
                <tbody>
                    {orchestras.map(o => {
                        const members = memberships.filter(m => m.orchestraId === o.id);
                        const isExpanded = expandedId === o.id;

                        return (
                            <React.Fragment key={o.id}>
                                <tr className={isExpanded ? "row-highlight" : ""}>
                                    <td className="cell-name primary-text clickable-cell" onClick={() => toggleExpand(o.id)}>
                                        {isExpanded ? "▼ " : "▶ "} {o.name}
                                    </td>
                                    <td className="cell-instrument tag-text">{o.ownerName || "BRAK"}</td>
                                    <td className="cell-voice voice-text">{members.length} OSÓB</td>
                                    <td className="cell-actions">
                                        <button className="btn-simple edit" onClick={() => onManage(o.id)} style={{ marginRight: '5px' }}>ZARZĄDZAJ</button>
                                        <button className="btn-simple delete" onClick={() => onDelete(o.id)}>USUŃ</button>
                                    </td>
                                </tr>
                                {isExpanded && (
                                    <tr>
                                        <td colSpan="4" className="expanded-row-container">
                                            <div className="members-management-box">
                                                <header className="box-header"><h4>CZŁONKOWIE: {o.name}</h4></header>
                                                <table className="inner-flat-table">
                                                    <thead>
                                                        <tr>
                                                            <th>MUZYK</th>
                                                            <th>ROLA W ORKIESTRZE</th>
                                                            <th className="cell-actions-header">AKCJE</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        {members.sort((a, b) => (roleWeights[a.orchestraRole] || 99) - (roleWeights[b.orchestraRole] || 99)).map(m => (
                                                            <tr key={m.id}>
                                                                <td><strong>{m.firstName} {m.lastName}</strong></td>
                                                                <td>
                                                                    {editingMembershipId === m.id ? (
                                                                        <select 
                                                                            autoFocus 
                                                                            className={`brutal-input badge-role-select ${(m.orchestraRole || 'musician').toLowerCase()}`}
                                                                            value={m.orchestraRole || ""}
                                                                            onChange={(e) => { onUpdateRole(m.id, e.target.value); setEditingMembershipId(null); }}
                                                                            onBlur={() => setEditingMembershipId(null)}
                                                                        >
                                                                            {Object.keys(roleWeights).map(r => <option key={r} value={r}>{r}</option>)}
                                                                        </select>
                                                                    ) : (
                                                                        <span className={`badge-role-clickable ${(m.orchestraRole || 'musician').toLowerCase()}`} onClick={() => setEditingMembershipId(m.id)}>
                                                                            {m.orchestraRole || "BRAK ROLI"} ✎
                                                                        </span>
                                                                    )}
                                                                </td>
                                                                <td className="cell-actions-right">
                                                                    <button className="btn-simple delete" onClick={() => onRemoveMember(m.id)}>WYPISZ</button>
                                                                </td>
                                                            </tr>
                                                        ))}
                                                    </tbody>
                                                </table>
                                                <div className="add-member-footer">
                                                    <span className="add-member-title">DODAJ NOWEGO CZŁONKA:</span>
                                                    <select className="brutal-input select-user-add" value={selectedUserToAdd} onChange={(e) => setSelectedUserToAdd(e.target.value)}>
                                                        <option value="">WYBIERZ UŻYTKOWNIKA...</option>
                                                        {users.filter(u => !members.some(m => m.userId === u.id)).map(u => (
                                                            <option key={u.id} value={u.id}>{u.firstName} {u.lastName} ({u.email})</option>
                                                        ))}
                                                    </select>
                                                    <select className="brutal-input select-role-add" value={selectedRoleToAdd} onChange={(e) => setSelectedRoleToAdd(e.target.value)}>
                                                        {Object.keys(roleWeights).map(r => <option key={r} value={r}>{r}</option>)}
                                                    </select>
                                                    <button className="btn-simple btn-confirm-add" onClick={() => onAddMember(o.id)}>POTWIERDŹ</button>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                )}
                            </React.Fragment>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
}
export default OrchestraTable;