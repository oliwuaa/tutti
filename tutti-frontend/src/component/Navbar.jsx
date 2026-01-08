import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Navbar.css';

function Navbar({ 
  userOrchestra, 
  myMemberships = [], 
  onLogout, 
  onNavigate, 
  onChangeOrchestra, 
  isSystemAdmin,
  currentView 
}) {
  const navigate = useNavigate(); 
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isOrchOpen, setIsOrchOpen] = useState(false);
  
  const orchRef = useRef(null);
  const menuRef = useRef(null);

  const isManagement = ['OWNER', 'CONDUCTOR'].includes(userOrchestra?.role);

  const closeAll = () => {
    setIsMenuOpen(false);
    setIsOrchOpen(false);
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (orchRef.current && !orchRef.current.contains(event.target) &&
          menuRef.current && !menuRef.current.contains(event.target)) {
        closeAll();
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleNavClick = (path) => {
    if (path === 'dashboard') {
      if (isSystemAdmin && !userOrchestra) {
        navigate('/dashboard/system-admin');
      } else {
        navigate('/dashboard');
      }
    } else {
      onNavigate(path);
    }
    closeAll();
  };

  return (
    <nav className="top-navbar">
      <div className="nav-logo" onClick={() => handleNavClick('dashboard')}>
        tutti<span>.</span>
      </div>

      <div className="nav-actions">
        {myMemberships.length > 0 && (
          <div className="nav-user-section" ref={orchRef}>
             <div 
              className={`nav-user-trigger ${isOrchOpen ? 'active' : ''}`} 
              onClick={() => { setIsOrchOpen(!isOrchOpen); setIsMenuOpen(false); }}
            >
              <span className="current-orch-name">
                  {userOrchestra?.orchestraName || "Wybierz..."}
              </span>
              <span className={`chevron ${isOrchOpen ? 'up' : 'down'}`}>▾</span>
            </div>
            {isOrchOpen && (
              <div className="nav-dropdown orch-dropdown">
                {myMemberships.map((orch) => {
                    const orchId = orch.orchestraId || orch.id;
                    const currentId = userOrchestra?.orchestraId || userOrchestra?.id;
                    return (
                        <button
                            key={`nav-orch-${orchId}`}
                            className={`dropdown-item ${orchId === currentId ? 'current' : ''}`}
                            onClick={() => { onChangeOrchestra(orch); closeAll(); }}
                        >
                            <span className="orch-name-text">{orch.orchestraName}</span>
                            {orchId === currentId && <span className="active-dot"> ●</span>}
                        </button>
                    );
                })}
              </div>
            )}
          </div>
        )}

        <div className="nav-user-section" ref={menuRef}>
          <div
            className={`nav-user-trigger menu-trigger ${isMenuOpen ? 'active' : ''}`}
            onClick={() => { setIsMenuOpen(!isMenuOpen); setIsOrchOpen(false); }}
          >
            MENU <span className={`chevron ${isMenuOpen ? 'up' : 'down'}`}>▾</span>
          </div>

          {isMenuOpen && (
            <div className="nav-dropdown main-menu-dropdown">
              {isSystemAdmin && (
                <>
                  <span className="dropdown-label admin-label">ADMINISTRACJA</span>
                  <button 
                      className={`dropdown-item ${currentView === 'system-admin' ? 'active' : ''}`} 
                      onClick={() => handleNavClick('system-admin')}
                  >
                      PANEL ADMINA
                  </button>
                  <div className="dropdown-divider"></div>
                </>
              )}

              {userOrchestra && (
                <>
                  <span className="dropdown-label">ORKIESTRA</span>
                  <button 
                      className={`dropdown-item ${currentView === 'dashboard' ? 'active' : ''}`} 
                      onClick={() => handleNavClick('dashboard')}
                  >
                      PULPIT
                  </button>
                  <button onClick={() => handleNavClick('library')} className="dropdown-item">NUTY</button>
                  <button onClick={() => handleNavClick('calendar')} className="dropdown-item">WYDARZENIA</button>
                  
                  {isManagement && (
                    <button onClick={() => handleNavClick('management')} className="dropdown-item">ZARZĄDZANIE</button>
                  )}
                  <div className="dropdown-divider"></div>
                </>
              )}

              <span className="dropdown-label">KONTO</span>
              <button onClick={() => handleNavClick('profile')} className="dropdown-item">USTAWIENIA</button>
              <button className="dropdown-item danger" onClick={onLogout}>WYLOGUJ SIĘ</button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;