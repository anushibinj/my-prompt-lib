import { useState } from 'react';
import { login, register } from '../api/promptApi';

interface AuthPageProps {
    onAuth: (token: string, username: string) => void;
}

export function AuthPage({ onAuth }: AuthPageProps) {
    const [isLogin, setIsLogin] = useState(true);
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

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
                <h1>🧩 PromptLib</h1>
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
            </div>
        </div>
    );
}
