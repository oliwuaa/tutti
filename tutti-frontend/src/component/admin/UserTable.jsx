import React from 'react';

function UserTable({ users, memberships, expandedUserOrchs, toggleUserOrchs, onEdit, onDelete, onRemoveFromOrchestra }) {
    return (
        <div className="table-wrapper">
            <table className="flat-table">
                <thead>
                    <tr>
                        <th>IMIĘ I NAZWISKO</th>
                        <th>EMAIL</th>
                        <th>ROLA SYSTEMOWA</th>
                        <th>CZŁONKOSTWA</th>
                        <th className="cell-actions-header">AKCJE ROOT</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(u => {
                        const userMemberships = memberships.filter(m => m.userId === u.id);
                        return (
                            <tr key={u.id}>
                                <td className="cell-name primary-text">{u.firstName} {u.lastName}</td>
                                <td className="cell-instrument tag-text">{u.email}</td>
                                <td className="cell-voice">
                                    <span className={`badge-role ${u.role?.toLowerCase()}`}>{u.role || "USER"}</span>
                                </td>
                                <td className="cell-memberships-relative">
                                    <button className="btn-simple btn-small-text" onClick={() => toggleUserOrchs(u.id)}>
                                        {userMemberships.length} GRUPY {expandedUserOrchs === u.id ? '▴' : '▾'}
                                    </button>
                                    {expandedUserOrchs === u.id && (
                                        <div className="floating-memberships-list">
                                            {userMemberships.length > 0 ? userMemberships.map(m => (
                                                <div key={m.id} className="membership-item-row">
                                                    <span className="membership-orch-name">{m.orchestraName}</span>
                                                    <button onClick={() => onRemoveFromOrchestra(m.id)} className="btn-remove-icon">×</button>
                                                </div>
                                            )) : <div className="no-memberships-hint">BRAK PRZYPISANYCH GRUP</div>}
                                        </div>
                                    )}
                                </td>
                                <td className="cell-actions">
                                    <div className="actions-container">
                                        <button className="btn-simple edit" onClick={() => onEdit(u)}>EDYTUJ</button>
                                        <button className="btn-simple delete" onClick={() => onDelete(u.id)}>USUŃ</button>
                                    </div>
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
}
export default UserTable;