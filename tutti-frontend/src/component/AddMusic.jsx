import { useState, useEffect } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';
import api from '../api/axios';
import '../styles/AddMusic.css';

import 'react-pdf/dist/esm/Page/AnnotationLayer.css';
import 'react-pdf/dist/esm/Page/TextLayer.css';

pdfjs.GlobalWorkerOptions.workerSrc = `//cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.min.mjs`;

const INSTRUMENTS = [
    { val: 'PICCOLO', label: 'Flet piccolo' },
    { val: 'FLUTE', label: 'Flet poprzeczny' },
    { val: 'OBOE', label: 'Obój' },
    { val: 'ENGLISH_HORN', label: 'Rożek angielski' },
    { val: 'FAGOTT', label: 'Fagot' },
    { val: 'CLARINET', label: 'Klarnet' },
    { val: 'ALTO_CLARINET', label: 'Klarnet altowy' },
    { val: 'BASS_CLARINET', label: 'Klarnet basowy' },
    { val: 'SOPRANO_SAX', label: 'Saksofon sopranowy' },
    { val: 'ALTO_SAX', label: 'Saksofon altowy' },
    { val: 'TENOR_SAX', label: 'Saksofon tenorowy' },
    { val: 'BARITONE_SAX', label: 'Saksofon barytonowy' },
    { val: 'TRUMPET', label: 'Trąbka' },
    { val: 'CORNET', label: 'Kornet' },
    { val: 'FLUGEL_HORN', label: 'Skrzydłówka' },
    { val: 'FRENCH_HORN', label: 'Waltornia' },
    { val: 'TROMBONE', label: 'Puzon' },
    { val: 'EUPHONIUM', label: 'Eufonium' },
    { val: 'TUBA', label: 'Tuba' },
    { val: 'DRUM_KIT', label: 'Perkusja' },
    { val: 'CYMBALS', label: 'Talerze' },
    { val: 'BELLS', label: 'Dzwonki' },
    { val: 'AUX_PERCUSSION', label: 'Perkusjonalia' },
    { val: 'KEYBOARD', label: 'Instrumenty klawiszowe' },
    { val: 'BASS_GUITAR', label: 'Gitara basowa' },
    { val: 'ELECTRIC_GUITAR', label: 'Gitara elektryczna' },
    { val: 'NONE', label: 'Brak' }
].sort((a, b) => a.label.localeCompare(b.label, 'pl'));

const ROMAN_TO_NUM = { 'I': 1, 'II': 2, 'III': 3, 'IV': 4 };

function AddMusic({ isOpen, onClose, onSave, initialData = null }) {
    const [fileUrl, setFileUrl] = useState(null);
    const [numPages, setNumPages] = useState(null);
    const [activeVoiceIdx, setActiveVoiceIdx] = useState(0);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [pdfLoading, setPdfLoading] = useState(false);

    const [musicData, setMusicData] = useState({
        title: '',
        composer: '',
        voices: [{ instrument: 'FLUTE', voiceNumbers: ['I'], startPage: null, endPage: null }],
        file: null
    });

    const isTitleEmpty = !musicData.title || musicData.title.trim() === '';
    const isFileMissing = !fileUrl && !initialData;

    useEffect(() => {
        let currentUrl = null;

        const init = async () => {
            if (isOpen) {
                if (initialData) {
                    const groupedMap = {};
                    initialData.parts?.forEach(p => {
                        const key = `${p.type}-${p.pageStart}-${p.pageEnd}`;
                        const roman = p.partNumber === 1 ? 'I' : p.partNumber === 2 ? 'II' : p.partNumber === 3 ? 'III' : 'IV';

                        if (!groupedMap[key]) {
                            groupedMap[key] = {
                                instrument: p.type,
                                voiceNumbers: [roman],
                                startPage: p.pageStart,
                                endPage: p.pageEnd
                            };
                        } else {
                            if (!groupedMap[key].voiceNumbers.includes(roman)) {
                                groupedMap[key].voiceNumbers.push(roman);
                            }
                        }
                    });

                    setMusicData({
                        title: initialData.title || '',
                        composer: initialData.composer || '',
                        voices: Object.values(groupedMap).length > 0
                            ? Object.values(groupedMap).map(v => ({ ...v, voiceNumbers: v.voiceNumbers.sort() }))
                            : [{ instrument: 'FLUTE', voiceNumbers: ['I'], startPage: null, endPage: null }],
                        file: null
                    });

                    if (initialData.id) {
                        setPdfLoading(true);
                        try {
                            const response = await api.get(`/scores/${initialData.id}/file`, { responseType: 'blob' });
                            currentUrl = URL.createObjectURL(response.data);
                            setFileUrl(currentUrl);
                        } catch (err) {
                            console.error("Błąd ładowania PDF:", err);
                        } finally {
                            setPdfLoading(false);
                        }
                    }
                } else {
                    setMusicData({
                        title: '', composer: '',
                        voices: [{ instrument: 'FLUTE', voiceNumbers: ['I'], startPage: null, endPage: null }],
                        file: null
                    });
                    setFileUrl(null);
                }
                setActiveVoiceIdx(0);
                setIsSubmitting(false);
            }
        };

        init();
        return () => { if (currentUrl) URL.revokeObjectURL(currentUrl); };
    }, [isOpen, initialData]);

    const handleSaveInternal = () => {
        setIsSubmitting(true);
        const flattenedVoices = [];
        musicData.voices.forEach(v => {
            v.voiceNumbers.forEach(numStr => {
                flattenedVoices.push({
                    type: v.instrument,
                    partNumber: ROMAN_TO_NUM[numStr],
                    pageStart: v.startPage,
                    pageEnd: v.endPage || v.startPage
                });
            });
        });

        const dataToSave = {
            ...musicData,
            voices: flattenedVoices
        };
        onSave(dataToSave);
    };

    const handlePageClick = (pageNum) => {
        const newVoices = [...musicData.voices];
        const current = newVoices[activeVoiceIdx];
        if (!current) return;

        if (!current.startPage || (current.startPage && current.endPage)) {
            current.startPage = pageNum;
            current.endPage = null;
        } else if (pageNum < current.startPage) {
            current.startPage = pageNum;
        } else {
            current.endPage = pageNum;
        }
        setMusicData({ ...musicData, voices: newVoices });
    };

    const toggleVoiceNumber = (idx, num) => {
        const nv = [...musicData.voices];
        const current = nv[idx].voiceNumbers;
        nv[idx].voiceNumbers = current.includes(num)
            ? (current.length > 1 ? current.filter(n => n !== num) : current)
            : [...current, num].sort();
        setMusicData({ ...musicData, voices: nv });
    };

    const getPageStatus = (pageNum) => {
        const v = musicData.voices[activeVoiceIdx];
        if (!v || !v.startPage) return '';
        if (v.startPage === pageNum && !v.endPage) return 'selected-start';
        if (v.startPage === pageNum || v.endPage === pageNum) return 'range-edge';
        if (pageNum > v.startPage && v.endPage && pageNum < v.endPage) return 'range-inside';
        return '';
    };

    if (!isOpen) return null;

    return (
        <div className="music-modal-overlay">
            <div className="music-modal-window">
                <div className="music-editor-container">
                    <div className="music-form-side">
                        <div className="music-modal-header">
                            <h2>{initialData ? 'EDYCJA UTWORU' : 'DODAWANIE NUT'}</h2>
                            <button type="button" className="music-close-x" onClick={onClose}>&times;</button>
                        </div>

                        <label className="music-section-title">TYTUŁ *</label>
                        <input
                            className="music-main-input"
                            value={musicData.title}
                            required
                            onChange={e => setMusicData({ ...musicData, title: e.target.value })}
                        />

                        <label className="music-section-title">KOMPOZYTOR</label>
                        <input
                            className="music-main-input"
                            value={musicData.composer}
                            onChange={e => setMusicData({ ...musicData, composer: e.target.value })}
                        />

                        <input
                            type="file" id="pdf-upload" hidden accept=".pdf"
                            onChange={(e) => {
                                const file = e.target.files[0];
                                if (file) {
                                    if (fileUrl) URL.revokeObjectURL(fileUrl);
                                    setFileUrl(URL.createObjectURL(file));
                                    setMusicData(prev => ({ ...prev, file }));
                                }
                            }}
                        />
                        <label htmlFor="pdf-upload" className="music-upload-label">
                            {musicData.file || (initialData && initialData.pdfPath) ? "✅ PLIK ZAŁADOWANY" : "📁 WGRAJ PLIK PDF"}
                        </label>

                        <div className="music-voices-list">
                            <label className="music-section-title">KONFIGURACJA GŁOSÓW</label>
                            {musicData.voices.map((v, idx) => (
                                <div
                                    key={idx}
                                    className={`music-voice-item ${activeVoiceIdx === idx ? 'is-active' : ''}`}
                                    onClick={() => setActiveVoiceIdx(idx)}
                                >
                                    <div className="music-voice-row-top">
                                        <select
                                            value={v.instrument}
                                            onChange={e => {
                                                const nv = [...musicData.voices];
                                                nv[idx].instrument = e.target.value;
                                                setMusicData({ ...musicData, voices: nv });
                                            }}
                                            onClick={e => e.stopPropagation()}
                                        >
                                            {INSTRUMENTS.map(ins => <option key={ins.val} value={ins.val}>{ins.label}</option>)}
                                        </select>
                                        <button className="music-voice-remove" onClick={(e) => {
                                            e.stopPropagation();
                                            const nv = musicData.voices.filter((_, i) => i !== idx);
                                            setMusicData({ ...musicData, voices: nv });
                                            setActiveVoiceIdx(0);
                                        }}>&times;</button>
                                    </div>

                                    <div className="music-voice-meta">
                                        <div className="music-voice-selector">
                                            {['I', 'II', 'III', 'IV'].map(num => (
                                                <button key={num} type="button"
                                                    className={`music-voice-num-btn ${v.voiceNumbers.includes(num) ? 'selected' : ''}`}
                                                    onClick={e => { e.stopPropagation(); toggleVoiceNumber(idx, num); }}>
                                                    {num}
                                                </button>
                                            ))}
                                        </div>
                                        <span className="music-page-info">
                                            {v.startPage ? `STR. ${v.startPage}${v.endPage ? '-' + v.endPage : ''}` : 'ZAZNACZ STRONY'}
                                        </span>
                                    </div>
                                </div>
                            ))}
                            <button
                                type="button"
                                className="music-add-voice-btn"
                                onClick={() => setMusicData({ ...musicData, voices: [...musicData.voices, { instrument: 'FLUTE', voiceNumbers: ['I'], startPage: null, endPage: null }] })}
                            >
                                + DODAJ KOLEJNY GŁOS
                            </button>
                        </div>

                        <div className="music-modal-actions">
                            <button type="button" className="music-btn-cancel" onClick={onClose}>ANULUJ</button>
                            <button
                                type="button"
                                className="music-btn-save"
                                onClick={handleSaveInternal}
                                disabled={isSubmitting || isTitleEmpty || isFileMissing}
                            >
                                {isSubmitting ? 'ZAPISYWANIE...' : 'ZAPISZ WSZYSTKO'}
                            </button>
                        </div>
                    </div>

                    <div className="music-pdf-side">
                        {pdfLoading ? (
                            <div className="music-pdf-placeholder">POBIERANIE PLIKU...</div>
                        ) : fileUrl ? (
                            <div className="pdf-scroll-container">
                                <Document
                                    file={fileUrl}
                                    onLoadSuccess={({ numPages }) => setNumPages(numPages)}
                                    loading={<div className="music-pdf-placeholder">ŁADOWANIE...</div>}
                                >
                                    {Array.from(new Array(numPages), (el, i) => (
                                        <div
                                            key={i}
                                            className={`music-pdf-page-box ${getPageStatus(i + 1)}`}
                                            onClick={() => handlePageClick(i + 1)}
                                        >
                                            <Page
                                                pageNumber={i + 1}
                                                width={450}
                                                renderTextLayer={false}
                                                renderAnnotationLayer={false}
                                            />
                                            <div className="page-num-indicator">{i + 1}</div>
                                        </div>
                                    ))}
                                </Document>
                            </div>
                        ) : (
                            <div className="music-pdf-placeholder">WGRAJ PDF, ABY PODZIELIĆ NA GŁOSY</div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default AddMusic;