import React from 'react';

function ProfileInfo({ localIsEditing, formData, setFormData, user }) {
    return (
        <div className="full-width-event">
            <div className="info-section"><span className="detail-label">DANE OSOBOWE</span></div>
            <div className="form-group">
                <label>IMIĘ</label>
                <input 
                    className={`up-form-input ${!localIsEditing ? 'locked' : 'editing'}`} 
                    value={formData.firstName} 
                    readOnly={!localIsEditing} 
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })} 
                />
            </div>
            <div className="form-group">
                <label>NAZWISKO</label>
                <input 
                    className={`up-form-input ${!localIsEditing ? 'locked' : 'editing'}`} 
                    value={formData.lastName} 
                    readOnly={!localIsEditing} 
                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })} 
                />
            </div>
            <div className="form-group">
                <label>EMAIL</label>
                <input className="up-form-input locked" value={user?.email || ''} readOnly />
            </div>
        </div>
    );
}

export default ProfileInfo;