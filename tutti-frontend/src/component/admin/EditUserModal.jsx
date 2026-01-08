import React from 'react';

function EditUserModal({ user, onSave, onCancel, onChange }) {
    if (!user) return null;

    return (
        <div className="modal-overlay">
            <div className="brutal-modal">
                <h2 className="modal-title">EDYTUJ UŻYTKOWNIKA</h2>
                <form onSubmit={onSave}>
                    <div className="form-group">
                        <label>IMIĘ:</label>
                        <input className="brutal-input input-full-width" value={user.firstName} onChange={(e) => onChange({ ...user, firstName: e.target.value })} />
                    </div>
                    <div className="form-group">
                        <label>NAZWISKO:</label>
                        <input className="brutal-input input-full-width" value={user.lastName} onChange={(e) => onChange({ ...user, lastName: e.target.value })} />
                    </div>
                    <div className="form-group">
                        <label>ROLA SYSTEMOWA:</label>
                        <select className="brutal-input input-full-width" value={user.role} onChange={(e) => onChange({ ...user, role: e.target.value })}>
                            <option value="USER">USER</option>
                            <option value="ADMIN">ADMIN</option>
                        </select>
                    </div>
                    <div className="modal-footer">
                        <button type="submit" className="btn-simple btn-save">ZAPISZ</button>
                        <button type="button" className="btn-simple btn-cancel" onClick={onCancel}>ANULUJ</button>
                    </div>
                </form>
            </div>
        </div>
    );
}
export default EditUserModal;