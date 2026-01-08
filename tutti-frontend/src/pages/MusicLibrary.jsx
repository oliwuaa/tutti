import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import AddMusic from '../component/AddMusic';
import ScoreCard from '../component/ScoreCard';
import api from '../api/axios';
import '../styles/Events.css';
import '../styles/Music.css';

const INSTRUMENT_LABELS = {
  PICCOLO: "Piccolo", FLUTE: "Flet poprzeczny", OBOE: "Obój", ENGLISH_HORN: "Rożek angielski",
  FAGOTT: "Fagot", CLARINET: "Klarnet", ALTO_CLARINET: "Klarnet altowy", BASS_CLARINET: "Klarnet basowy",
  SOPRANO_SAX: "Saksofon sopranowy", ALTO_SAX: "Saksofon altowy", TENOR_SAX: "Saksofon tenorowy",
  BARITONE_SAX: "Saksofon barytonowy", TRUMPET: "Trąbka", CORNET: "Kornet", FLUGEL_HORN: "Skrzydłówka",
  FRENCH_HORN: "Waltornia", TROMBONE: "Puzon", EUPHONIUM: "Eufonium", TUBA: "Tuba",
  DRUM_KIT: "Perkusja", CYMBALS: "Talerze", BELLS: "Dzwonki", AUX_PERCUSSION: "Perkusjonalia",
  KEYBOARD: "Klawisze", BASS_GUITAR: "Gitara basowa", ELECTRIC_GUITAR: "Gitara elektryczna", NONE: "Brak"
};

function MusicLibrary() {
  const navigate = useNavigate();
  const { user, userOrchestra } = useOutletContext();

  const isManagement = ['OWNER', 'ADMIN', 'CONDUCTOR', 'LIBRARIAN'].includes(userOrchestra?.role);

  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingSong, setEditingSong] = useState(null);
  const [library, setLibrary] = useState([]);
  const [loading, setLoading] = useState(false);
  const [userPartInfo, setUserPartInfo] = useState({ type: null, number: null });

  const groupPartsByInstrument = (parts) => {
    if (!parts) return [];

    const grouped = parts.reduce((acc, part) => {
      const instrument = part.type || "INNE";
      if (!acc[instrument]) acc[instrument] = [];
      acc[instrument].push(part);
      return acc;
    }, {});

    return Object.entries(grouped).sort((a, b) => {
      const labelA = INSTRUMENT_LABELS[a[0]] || a[0];
      const labelB = INSTRUMENT_LABELS[b[0]] || b[0];
      return labelA.localeCompare(labelB, 'pl');
    });
  };

  const fetchScores = useCallback(async () => {
    if (!userOrchestra?.id) return;
    try {
      setLoading(true);
      const scoresRes = await api.get('/scores', {
        params: { orchestraId: userOrchestra.id }
      });
      const scores = scoresRes.data;

      const scoresWithParts = await Promise.all(scores.map(async (score) => {
        try {
          const partsRes = await api.get(`/parts/score/${score.id}/orchestra/${userOrchestra.id}`);
          return { ...score, parts: partsRes.data };
        } catch (e) {
          return { ...score, parts: [] };
        }
      }));
      setLibrary(scoresWithParts);
    }  catch (err) { alert(err.message); } finally {
      setLoading(false);
    }
  }, [userOrchestra?.id]);

  const fetchUserInstrumentInfo = useCallback(async () => {
    if (!userOrchestra?.id) return;
    try {
      const res = await api.get('/memberships/my-memberships/active');
      const currentMembership = res.data.find(m => m.orchestraId === userOrchestra.id);
      if (currentMembership) {
        setUserPartInfo({
          type: currentMembership.instrumentType,
          number: currentMembership.partNumber
        });
      }
    } catch (err) {
      console.error("Błąd pobierania danych o instrumencie:", err);
    }
  }, [userOrchestra?.id]);

  useEffect(() => {
    fetchScores();
    fetchUserInstrumentInfo();
  }, [fetchScores, fetchUserInstrumentInfo]);

  const handleAddClick = () => {
    setEditingSong(null);
    setIsModalOpen(true);
  };

  const handleEditClick = (song) => {
    setEditingSong(song);
    setIsModalOpen(true);
  };

  const handleDeleteClick = async (id) => {
    if (window.confirm("CZY NA PEWNO CHCESZ TRWALE USUNĄĆ TEN UTWÓR I WSZYSTKIE GŁOSY?")) {
      try {
        await api.delete(`/scores/${id}`);
        setLibrary(prev => prev.filter(song => song.id !== id));
      } catch (err) {
        console.error("Błąd podczas usuwania:", err);
        alert("Nie udało się usunąć utworu.");
      }
    }
  };

  const handleSaveMusic = async (musicData) => {
    try {
      const formData = new FormData();
      const scoreRequest = {
        title: musicData.title,
        composer: musicData.composer,
        orchestraId: userOrchestra.id
      };

      formData.append('request', new Blob([JSON.stringify(scoreRequest)], { type: 'application/json' }));
      if (musicData.file) formData.append('file', musicData.file);

      const scoreRes = editingSong
        ? await api.put(`/scores/${editingSong.id}`, formData)
        : await api.post('/scores', formData);

      const scoreId = scoreRes.data.id;

      if (editingSong) {
        const oldPartsRes = await api.get(`/parts/score/${scoreId}/orchestra/${userOrchestra.id}`);
        const deletePromises = oldPartsRes.data.map(p => api.delete(`/parts/${p.id}`));
        await Promise.all(deletePromises);
      }

      const partPromises = musicData.voices.map(v => {
        return api.post('/parts', {
          scoreId: scoreId,
          orchestraId: userOrchestra.id,
          type: v.type,
          partNumber: v.partNumber,
          pageStart: v.pageStart,
          pageEnd: v.pageEnd
        });
      });

      await Promise.all(partPromises);
      setIsModalOpen(false);
      setEditingSong(null);
      fetchScores();
      alert("Zapisano pomyślnie!");
    } catch (err) {
      console.error("Błąd zapisu:", err);
      alert("Błąd: " + (err.response?.data?.message || "Wystąpił błąd"));
    }
  };

  const handleOpenPdf = async (endpoint) => {
    try {
      const response = await api.get(endpoint, { responseType: 'blob' });
      const file = new Blob([response.data], { type: 'application/pdf' });
      const fileURL = URL.createObjectURL(file);
      window.open(fileURL);
    } catch (error) {
      console.error("Nie udało się otworzyć pliku PDF:", error);
      alert("Błąd podczas pobierania pliku.");
    }
  };

  const filteredSheets = library.filter(sheet =>
    sheet.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (sheet.composer || "").toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="calendar-view">
      <header className="view-header">
        <div className="header-left">
          <button className="back-link" onClick={() => navigate('/dashboard')}>← WRÓĆ DO PULPITU</button>
          <h1 className="welcome-title">BIBLIOTEKA NUT</h1>
          <p className="welcome-subtitle">
            TWOJA PARTIA: <strong>{INSTRUMENT_LABELS[userPartInfo.type] || "NIEPRZYPISANO"} {userPartInfo.number ? `(Głos ${userPartInfo.number})` : ""}</strong>
          </p>
        </div>
        {isManagement && (
          <button className="alert-action-btn" onClick={handleAddClick}>DODAJ UTWÓR</button>
        )}
      </header>

      <div className="filter-bar full-width-event">
        <div className="form-group" style={{ marginBottom: 0, width: '100%' }}>
          <label>WYSZUKAJ UTWÓR</label>
          <input
            type="text"
            placeholder="Wpisz tytuł lub kompozytora..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <div className="full-events-list">
        {loading ? (
          <p style={{ color: 'white', textAlign: 'center' }}>Ładowanie biblioteki...</p>
        ) : (
          filteredSheets.map(sheet => (
            <ScoreCard
              key={sheet.id}
              sheet={sheet}
              userPartInfo={userPartInfo}
              isManagement={isManagement}
              onEdit={handleEditClick}
              onDelete={handleDeleteClick}
              onOpenPdf={handleOpenPdf}
              groupPartsFn={groupPartsByInstrument}
              instrumentLabels={INSTRUMENT_LABELS}
            />
          ))
        )}
      </div>

      <AddMusic
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveMusic}
        initialData={editingSong ? { ...editingSong, pdfPath: `/scores/${editingSong.id}/file` } : null}
      />
    </div>
  );
}

export default MusicLibrary;