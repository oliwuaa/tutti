import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom'; 
import api from '../api/axios';
import '../styles/LandingPage.css';

function LandingPage({ onLogin }) {
    const navigate = useNavigate(); 
    const [step, setStep] = useState(0);
    const [isRegistering, setIsRegistering] = useState(false);

    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirmPassword: ''
    });
    const [error, setError] = useState("");

    const heroRef = useRef(null);
    const featuresRef = useRef(null);
    const rolesRef = useRef(null);
    const ctaRef = useRef(null);

    useEffect(() => {
        setTimeout(() => setStep(1), 500);
        setTimeout(() => setStep(2), 1000);
        setTimeout(() => setStep(3), 2200);

        const observerOptions = { threshold: 0.25 };
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('reveal-visible');
                }
            });
        }, observerOptions);

        if (featuresRef.current) observer.observe(featuresRef.current);
        if (rolesRef.current) observer.observe(rolesRef.current);
        if (ctaRef.current) observer.observe(ctaRef.current);

        return () => observer.disconnect();
    }, []);

    const scrollToFeatures = () => featuresRef.current?.scrollIntoView({ behavior: 'smooth' });
    const scrollToRoles = () => rolesRef.current?.scrollIntoView({ behavior: 'smooth' });
    const scrollToCTA = () => ctaRef.current?.scrollIntoView({ behavior: 'smooth' });

    const handleRegisterClick = () => {
        heroRef.current?.scrollIntoView({ behavior: 'smooth' });
        setTimeout(() => {
            setIsRegistering(true);
        }, 500);
    };

    const handleInputChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
        setError("");
    };

    const handleAuth = async (e) => {
        e.preventDefault();
        setError("");

        try {
            if (isRegistering) {
                if (formData.password !== formData.confirmPassword) {
                    setError("Hasła nie są takie same!");
                    return;
                }

                await api.post('/users', {
                    firstName: formData.firstName,
                    lastName: formData.lastName,
                    email: formData.email,
                    password: formData.password
                });

                alert("Konto utworzone pomyślnie! Możesz się teraz zalogować.");
                setIsRegistering(false);
                setFormData({ ...formData, password: '', confirmPassword: '' });
            } else {
                const response = await api.post('/auth/login', {
                    email: formData.email,
                    password: formData.password
                });

                localStorage.setItem('accessToken', response.data.accessToken);
                localStorage.setItem('refreshToken', response.data.refreshToken);

                onLogin(response.data.accessToken);

                navigate('/dashboard'); 
            }
        }  catch (err) { setError(err.message); }
    };

    return (
        <div className="landing-wrapper">
            <section className="hero" ref={heroRef}>
                <div className={`hero-container ${step === 3 ? 'split-view' : 'centered-view'}`}>
                    <div className={`hero-text ${step >= 1 ? 'visible' : 'hidden'}`}>
                        <h1 className="main-logo">tutti.</h1>
                        <p className={`subtitle ${step >= 2 ? 'visible' : 'hidden'}`}>
                            <i>cała orkiestra w jednym miejscu</i>
                        </p>
                    </div>

                    <div className="login-box">
                        <div className="auth-header">
                            <h2 className="auth-title">{isRegistering ? "DOŁĄCZ" : "WITAJ!"}</h2>
                            <div className="auth-switch" onClick={() => { setIsRegistering(!isRegistering); setError(""); }}>
                                {isRegistering ? "mam już konto →" : "nie mam konta →"}
                            </div>
                        </div>

                        <div className="auth-body">
                            {error && <div style={{ color: '#ff6b6b', fontSize: '0.8rem', marginBottom: '10px' }}>{error}</div>}

                            {!isRegistering ? (
                                <form className="auth-fade-in" key="login-form" onSubmit={handleAuth}>
                                    <input
                                        type="email"
                                        name="email"
                                        placeholder="Email"
                                        className="tutti-input"
                                        required
                                        value={formData.email}
                                        onChange={handleInputChange}
                                    />
                                    <input
                                        type="password"
                                        name="password"
                                        placeholder="Hasło"
                                        className="tutti-input"
                                        required
                                        value={formData.password}
                                        onChange={handleInputChange}
                                    />
                                    <button type="submit" className="auth-btn">Zaloguj się</button>
                                </form>
                            ) : (
                                <form className="auth-fade-in" key="register-form" onSubmit={handleAuth}>
                                    <div className="input-row">
                                        <input
                                            type="text"
                                            name="firstName"
                                            placeholder="Imię"
                                            className="tutti-input"
                                            required
                                            value={formData.firstName}
                                            onChange={handleInputChange}
                                        />
                                        <input
                                            type="text"
                                            name="lastName"
                                            placeholder="Nazwisko"
                                            className="tutti-input"
                                            required
                                            value={formData.lastName}
                                            onChange={handleInputChange}
                                        />
                                    </div>
                                    <input
                                        type="email"
                                        name="email"
                                        placeholder="Email"
                                        className="tutti-input"
                                        required
                                        value={formData.email}
                                        onChange={handleInputChange}
                                    />
                                    <input
                                        type="password"
                                        name="password"
                                        placeholder="Hasło"
                                        className="tutti-input"
                                        required
                                        value={formData.password}
                                        onChange={handleInputChange}
                                    />
                                    <input
                                        type="password"
                                        name="confirmPassword"
                                        placeholder="Powtórz hasło"
                                        className="tutti-input"
                                        required
                                        value={formData.confirmPassword}
                                        onChange={handleInputChange}
                                    />
                                    <button type="submit" className="auth-btn">Załóż konto</button>
                                </form>
                            )}
                        </div>
                    </div>
                </div>
                <div className={`center-paper-peel ${step === 3 ? 'visible' : ''}`} onClick={scrollToFeatures}>
                    <div className="peel-content"><span>Poznaj możliwości</span></div>
                </div>
            </section>

            <section className="features-section-basic" ref={featuresRef}>
                <h2>Możliwości Tutti</h2>
                <div className="basic-grid">
                    <div className="basic-card">
                        <i className="fa-solid fa-music"></i>
                        <h3>Biblioteka Nut</h3>
                        <p>Cyfrowy dostęp do partytur i głosów. Filtruj według instrumentu i miej swoje nuty pod ręką.</p>
                    </div>
                    <div className="basic-card">
                        <i className="fa-solid fa-calendar-check"></i>
                        <h3>Wydarzenia</h3>
                        <p>Pełny harmonogram prób i koncertów. Lokalizacja, godzina i lista utworów w jednym miejscu.</p>
                    </div>
                    <div className="basic-card">
                        <i className="fa-solid fa-boxes-stacked"></i>
                        <h3>Zasoby</h3>
                        <p>Ewidencja instrumentów i strojów. Kontroluj wspólny majątek orkiestry.</p>
                    </div>
                </div>
                <div className="center-paper-peel visible roles-trigger" onClick={scrollToRoles}>
                    <div className="peel-content"><span>Dla kogo jest tutti?</span></div>
                </div>
            </section>

            <section className="roles-section" ref={rolesRef}>
                <h2>Dostosowane do Twojej roli</h2>
                <div className="roles-container">
                    <div className="role-item">
                        <div className="role-icon"><i className="fa-solid fa-crown"></i></div>
                        <div className="role-text">
                            <h3>Zarząd & Właściciel</h3>
                            <p>Pełna kontrola nad strukturą orkiestry i uprawnieniami.</p>
                        </div>
                    </div>
                    <div className="role-item">
                        <div className="role-icon"><i className="fa-solid fa-wand-sparkles"></i></div>
                        <div className="role-text">
                            <h3>Dyrygent</h3>
                            <p>Zarządzanie repertuarem, tworzenie planów prób i koncertów.</p>
                        </div>
                    </div>
                    <div className="role-item">
                        <div className="role-icon"><i className="fa-solid fa-book-open"></i></div>
                        <div className="role-text">
                            <h3>Bibliotekarz</h3>
                            <p>Dodawanie nut, segregowanie głosów i dbanie o gotowość pulpitów.</p>
                        </div>
                    </div>
                    <div className="role-item">
                        <div className="role-icon"><i className="fa-solid fa-drum"></i></div>
                        <div className="role-text">
                            <h3>Muzyk</h3>
                            <p>Błyskawiczny dostęp do przypisanych nut i harmonogramu.</p>
                        </div>
                    </div>
                </div>
                <div className="center-paper-peel visible cta-trigger" onClick={scrollToCTA}>
                    <div className="peel-content"><span>Dołącz teraz</span></div>
                </div>
            </section>

            <section className="cta-section" ref={ctaRef}>
                <div className="cta-content">
                    <h2 className="cta-title">Na co czekasz?</h2>
                    <p className="cta-subtitle">Twoja orkiestra zasługuje na najlepsze narzędzia.</p>

                    <button className="main-cta-btn" onClick={handleRegisterClick}>
                        Zarejestruj się
                    </button>

                    <div className="cta-footer-note">
                        <p>Dołącz do setek muzyków, którzy już grają z <strong>tutti.</strong></p>
                    </div>
                </div>
                <footer className="simple-footer">
                    © 2025 tutti. Wszystkie prawa zastrzeżone.
                </footer>
            </section>
        </div>
    );
}

export default LandingPage;