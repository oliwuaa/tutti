import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useState } from 'react';
import LandingPage from './pages/LandingPage';
import MainDashboard from './pages/MainDashboard';
import EventsCalendar from './pages/Events';
import MusicLibrary from './pages/MusicLibrary';
import OrchestraManagement from './pages/OrchestraManagement';
import MyProfile from './pages/MyProfile';
import SystemAdminPanel from './pages/AdminPanel';

function App() {
  const [token, setToken] = useState(() => {
    const savedToken = localStorage.getItem('accessToken');
    return (savedToken && savedToken !== "null") ? savedToken : null;
  });

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setToken(null);
  };

  return (
    <Router>
      <Routes>
        <Route path="/" element={!token ? <LandingPage onLogin={(t) => setToken(t)} /> : <Navigate to="/dashboard" />} />

        <Route path="/dashboard" element={token ? <MainDashboard onLogout={handleLogout} /> : <Navigate to="/" />}>
          <Route path="calendar" element={<EventsCalendar />} />
          <Route path="library" element={<MusicLibrary />} />
          <Route path="management" element={<OrchestraManagement />} />
          <Route path="management/:orchId" element={<OrchestraManagement />} />
          <Route path="profile" element={<MyProfile />} />
          <Route path="system-admin" element={<SystemAdminPanel />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;