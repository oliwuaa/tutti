import { useState, useEffect } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import api from '../api/axios';
import Navbar from '../component/Navbar';
import '../styles/MainDashboard.css';

function MainDashboard({ onLogout }) {
  const navigate = useNavigate();
  const location = useLocation();

  const [user, setUser] = useState(null);
  const [userOrchestra, setUserOrchestra] = useState(null);
  const [myMemberships, setMyMemberships] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [recentScores, setRecentScores] = useState([]);
  const [newestScores, setNewestScores] = useState([]);
  const [upcomingEvents, setUpcomingEvents] = useState([]);

  const isHome = location.pathname === '/dashboard' || location.pathname === '/dashboard/';

  useEffect(() => {
    fetchInitialData();
  }, []);

  const fetchInitialData = async () => {
    try {
      setLoading(true);
      const userRes = await api.get('/users/me');
      setUser(userRes.data);

      const membershipRes = await api.get('/memberships/my-memberships/active');
      const memberships = membershipRes.data;
      setMyMemberships(memberships);

      if (!memberships || memberships.length === 0) {
        navigate('/dashboard/profile');
        return;
      }

      const savedOrchestra = localStorage.getItem('selectedOrchestra');

      if (savedOrchestra) {
        const parsedOrch = JSON.parse(savedOrchestra);
        const stillMember = memberships.find(m => (m.orchestraId || m.id) === parsedOrch.id);

        if (stillMember) {
          handleSelectOrchestra(stillMember);
        } else {
          localStorage.removeItem('selectedOrchestra');
        }
      } else if (memberships.length === 1) {
        handleSelectOrchestra(memberships[0]);
      }

    } catch (err) {
      console.error("Błąd ładowania danych:", err);
      if (err.response?.status === 401) onLogout();
    } finally {
      setLoading(false);
    }
  };

  const handleSelectOrchestra = async (membership) => {
    if (!membership) return;
    const orchId = membership.orchestraId || membership.id;

    const orchData = {
      id: orchId,
      orchestraId: orchId,
      orchestraName: membership.orchestraName,
      role: membership.role || membership.orchestraRole,
      instrument: membership.instrumentType,
      partNumber: membership.partNumber
    };

    localStorage.setItem('selectedOrchestra', JSON.stringify(orchData));
    setUserOrchestra(orchData);

    fetchDashboardEvents(orchId);
    fetchDashboardScores(orchId);

    const canManage = ['OWNER', 'ADMIN', 'CONDUCTOR'].includes(orchData.role);
    if (canManage) {
      try {
        const res = await api.get(`/memberships/orchestra/${orchId}/status/PENDING`);
        setPendingRequests(res.data);
      } catch (e) { setPendingRequests([]); }
    }
  };

  const fetchDashboardEvents = async (orchId) => {
    try {
      const res = await api.get(`/orchestras/${orchId}/events`, {
        params: { scope: 'upcoming' }
      });
      setUpcomingEvents(res.data.slice(0, 2));
    } catch (err) { console.error(err); }
  };

  const fetchDashboardScores = async (orchId) => {
    try {
      const res = await api.get(`/scores`, { params: { orchestraId: orchId } });
      const rawScores = res.data;

      const allScores = await Promise.all(rawScores.map(async (score) => {
        try {
          const partsRes = await api.get(`/parts/score/${score.id}/orchestra/${orchId}`);
          return { ...score, parts: partsRes.data };
        } catch (e) { return { ...score, parts: [] }; }
      }));

      const history = JSON.parse(localStorage.getItem(`history_orch_${orchId}`) || '[]');
      const historicalScores = allScores
        .filter(score => history.includes(score.id))
        .sort((a, b) => history.indexOf(a.id) - history.indexOf(b.id));

      setRecentScores(historicalScores.slice(0, 1));
      const recentId = historicalScores[0]?.id;
      const newest = allScores
        .filter(score => score.id !== recentId)
        .sort((a, b) => b.id - a.id);

      setNewestScores(newest.slice(0, 2));
    } catch (err) { console.error(err); }
  };

  const openScore = async (score) => {
    if (!userOrchestra) return;
    const orchId = userOrchestra.id;
    let history = JSON.parse(localStorage.getItem(`history_orch_${orchId}`) || '[]');
    history = [score.id, ...history.filter(id => id !== score.id)].slice(0, 10);
    localStorage.setItem(`history_orch_${orchId}`, JSON.stringify(history));

    const myVoice = score.parts?.find(p =>
      p.type === userOrchestra.instrument &&
      p.partNumber === userOrchestra.partNumber
    );

    const endpoint = myVoice ? `/parts/${myVoice.id}/pdf` : `/scores/${score.id}/file`;

    try {
      const response = await api.get(endpoint, { responseType: 'blob' });
      const file = new Blob([response.data], { type: 'application/pdf' });
      window.open(URL.createObjectURL(file));
    }  catch (err) { alert(err.message); }
  };

  const isManagement = userOrchestra && ['OWNER', 'ADMIN', 'CONDUCTOR'].includes(userOrchestra.role);
  const isSystemAdmin = user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN';

  if (loading) return <div className="loading-screen">Ładowanie...</div>;

  return (
    <div className="main-dashboard-app">
      <Navbar
        user={user}
        userOrchestra={userOrchestra}
        myMemberships={myMemberships}
        onLogout={onLogout}
        onNavigate={(path) => navigate(`/dashboard/${path}`)}
        onChangeOrchestra={handleSelectOrchestra}
        isSystemAdmin={isSystemAdmin}
      />

      <div className="overview-container">
        {isHome && !userOrchestra && !isSystemAdmin && (
          <div className="selection-screen">
            <h1 className="selection-title">Witaj, {user?.firstName}!</h1>
            <div className="selection-grid">
              {myMemberships.map((m) => (
                <div key={m.id} className="selection-card" onClick={() => handleSelectOrchestra(m)}>
                  <h3 className="selection-card-name">{m.orchestraName}</h3>
                  <button className="selection-btn">WEJDŹ</button>
                </div>
              ))}
            </div>
          </div>
        )}

        {isHome && (userOrchestra || isSystemAdmin) && (
          <>
            <header className="welcome-section">
              <h1 className="welcome-title">WITAJ, {user?.firstName}</h1>
              <p className="welcome-subtitle">
                {userOrchestra ? `TWOJA ORKIESTRA: ${userOrchestra.orchestraName}` : "PANEL ADMINISTRATORA SYSTEMU"}
              </p>
            </header>

            {(isManagement || isSystemAdmin) && pendingRequests.length > 0 && (
              <div className="registration-alert">
                <div className="alert-content">
                  <span className="alert-icon">🔔</span>
                  <p>Masz <strong>{pendingRequests.length} nowe zgłoszenia</strong>!</p>
                </div>
                <button className="alert-action-btn" onClick={() => navigate('/dashboard/management')}>
                  ZOBACZ I ZATWIERDŹ
                </button>
              </div>
            )}

            <div className="overview-grid">
              <div className="info-card">
                <div className="card-header"><h3>NADCHODZĄCE WYDARZENIA</h3></div>
                <div className="events-list">
                  {upcomingEvents.length > 0 ? (
                    upcomingEvents.map(event => (
                      <div key={event.id} className="event-row interactive" onClick={() => navigate('/dashboard/calendar')}>
                        <div className="event-date-column">
                          <div className="event-day">{new Date(event.date).toLocaleDateString('pl-PL', { day: '2-digit', month: '2-digit' })}</div>
                          <div className="event-hour">{new Date(event.date).toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' })}</div>
                        </div>
                        <div className="event-details-column">
                          <div className="event-name">{event.name?.toUpperCase() || event.title?.toUpperCase()}</div>
                          <div className="event-location">{event.address?.toUpperCase()}</div>
                        </div>
                      </div>
                    ))
                  ) : <p className="no-data-text">Brak nadchodzących wydarzeń.</p>}
                </div>
                <button className="main-card-btn" onClick={() => navigate('/dashboard/calendar')}>PEŁNY KALENDARZ</button>
              </div>

              <div className="info-card">
                <div className="card-header"><h3>TWOJE NUTY</h3></div>
                <div className="sheets-list">
                  {newestScores.length > 0 || recentScores.length > 0 ? (
                    <>
                      {recentScores.map(score => (
                        <div key={`recent-${score.id}`} className="sheet-row interactive last-opened" onClick={() => openScore(score)}>
                          <div className="sheet-info">
                            <span className="sheet-title">{score.title.toUpperCase()}</span>
                            <span className="sheet-instrument">{score.composer.toUpperCase()}</span>
                          </div>
                        </div>
                      ))}
                      {newestScores.map(score => (
                        <div key={`new-${score.id}`} className="sheet-row interactive" onClick={() => openScore(score)}>
                          <div className="sheet-info">
                            <span className="sheet-title">{score.title.toUpperCase()}</span>
                            <span className="sheet-instrument">{score.composer.toUpperCase()}</span>
                          </div>
                        </div>
                      ))}
                    </>
                  ) : <p className="no-data-text">Brak utworów w bibliotece.</p>}
                </div>
                <button className="main-card-btn" onClick={() => navigate('/dashboard/library')}>PEŁNA BIBLIOTEKA</button>
              </div>
            </div>
          </>
        )}

        <Outlet context={{
          user,
          setUser,
          userOrchestra,
          onSelectOrchestra: handleSelectOrchestra,
          pendingRequests,
          setPendingRequests,
          isSystemAdmin,
          refreshDashboard: fetchInitialData
        }} />
      </div>
    </div>
  );
}

export default MainDashboard;