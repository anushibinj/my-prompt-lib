import { useState, useEffect, useRef } from 'react';
import { login, register, googleLogin, getGoogleClientId } from '../api/promptApi';

interface AuthPageProps {
    onAuth: (token: string, username: string) => void;
}

function loadGoogleScript(): Promise<void> {
    return new Promise((resolve) => {
        if (window.google?.accounts) {
            resolve();
            return;
        }
        const script = document.createElement('script');
        script.src = 'https://accounts.google.com/gsi/client';
        script.async = true;
        script.defer = true;
        script.onload = () => resolve();
        document.head.appendChild(script);
    });
}

export function AuthPage({ onAuth }: AuthPageProps) {
    const [isLogin, setIsLogin] = useState(true);
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const googleBtnRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        let cancelled = false;
        (async () => {
            try {
                const clientId = await getGoogleClientId();
                if (cancelled || !clientId) return;
                await loadGoogleScript();
                if (cancelled || !window.google || !googleBtnRef.current) return;
                window.google.accounts.id.initialize({
                    client_id: clientId,
                    callback: async (response: { credential: string }) => {
                        setError('');
                        setLoading(true);
                        try {
                            const result = await googleLogin(response.credential);
                            localStorage.setItem('token', result.token);
                            localStorage.setItem('username', result.username);
                            onAuth(result.token, result.username);
                        } catch {
                            setError('Google sign-in failed. Please try again.');
                        } finally {
                            setLoading(false);
                        }
                    },
                });
                window.google.accounts.id.renderButton(googleBtnRef.current, {
                    theme: 'filled_black',
                    size: 'large',
                    width: '100%',
                    text: 'signin_with',
                });
            } catch {
                // Google Sign-In not available — silently degrade
            }
        })();
        return () => { cancelled = true; };
    }, [onAuth]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const result = isLogin
                ? await login(username, password)
                : await register(username, password);
            localStorage.setItem('token', result.token);
            localStorage.setItem('username', result.username);
            onAuth(result.token, result.username);
        } catch (err: unknown) {
            if (err && typeof err === 'object' && 'response' in err) {
                const axiosErr = err as { response?: { data?: string } };
                setError(axiosErr.response?.data || (isLogin ? 'Invalid credentials' : 'Registration failed'));
            } else {
                setError('Network error. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-card">
                <h1>🧩 My Prompt Lib</h1>
                <p className="auth-subtitle">Save, manage, and run your prompt templates</p>
                <div className="auth-tabs">
                    <button
                        className={`auth-tab ${isLogin ? 'active' : ''}`}
                        onClick={() => { setIsLogin(true); setError(''); }}
                    >
                        Sign In
                    </button>
                    <button
                        className={`auth-tab ${!isLogin ? 'active' : ''}`}
                        onClick={() => { setIsLogin(false); setError(''); }}
                    >
                        Register
                    </button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Username</label>
                        <input
                            type="text"
                            className="input-field"
                            placeholder="Enter username"
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label>Password</label>
                        <input
                            type="password"
                            className="input-field"
                            placeholder="Enter password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    {error && <div className="auth-error">{error}</div>}
                    <button type="submit" className="btn btn-primary auth-submit" disabled={loading}>
                        {loading ? 'Please wait...' : (isLogin ? 'Sign In' : 'Create Account')}
                    </button>
                </form>
                <div className="auth-divider">
                    <span>or</span>
                </div>
                <div ref={googleBtnRef} className="google-btn-container" />
            </div>
        </div>
    );
}
