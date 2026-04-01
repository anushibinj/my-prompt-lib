import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useEffect, useState, useCallback } from 'react';
import { Sidebar } from './components/Sidebar';
import { PromptRunner } from './components/PromptRunner';
import { PromptEditor } from './components/PromptEditor';
import { PromptHistory } from './components/PromptHistory';
import { SharedPromptRunner } from './components/SharedPromptRunner';
import { AuthPage } from './components/AuthPage';
import { getPrompts, logout, setNetworkErrorHandler, pingBackend, type Prompt } from './api/promptApi';

function App() {
  const [prompts, setPrompts] = useState<Prompt[]>([]);
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [username, setUsername] = useState<string | null>(localStorage.getItem('username'));
  const [promptsLoading, setPromptsLoading] = useState(false);
  const [promptsError, setPromptsError] = useState(false);
  const [backendDown, setBackendDown] = useState(false);

  const handleLogout = useCallback(() => {
    logout();
    setToken(null);
    setUsername(null);
    setPrompts([]);
  }, []);

  const refreshPrompts = useCallback(() => {
    if (token) {
      setPromptsLoading(true);
      setPromptsError(false);
      getPrompts()
        .then(data => { setPrompts(data); setPromptsLoading(false); })
        .catch(err => {
          setPromptsLoading(false);
          if (err.response?.status === 401) {
            handleLogout();
          } else {
            setPromptsError(true);
          }
        });
    }
  }, [token, handleLogout]);

  useEffect(() => {
    setNetworkErrorHandler(() => setBackendDown(true));
  }, []);

  // Poll health endpoint every 5 s while banner is shown; auto-dismiss when backend recovers
  useEffect(() => {
    if (!backendDown) return;
    const interval = setInterval(async () => {
      const ok = await pingBackend();
      if (ok) {
        setBackendDown(false);
        refreshPrompts();
      }
    }, 5000);
    return () => clearInterval(interval);
  }, [backendDown, refreshPrompts]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    refreshPrompts();
  }, [refreshPrompts]);

  const handleAuth = (newToken: string, newUsername: string) => {
    setToken(newToken);
    setUsername(newUsername);
  };

  return (
    <>
      {backendDown && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, zIndex: 9999,
          background: '#b91c1c', color: '#fff', textAlign: 'center',
          padding: '0.75rem 1rem', fontSize: '0.95rem', fontWeight: 500,
        }}>
          🔧 My Prompt Lib is currently down for maintenance. Please try again later.
          <button
            onClick={() => setBackendDown(false)}
            style={{ marginLeft: '1rem', background: 'transparent', border: '1px solid rgba(255,255,255,0.5)', color: '#fff', borderRadius: '4px', padding: '0.2rem 0.6rem', cursor: 'pointer', fontFamily: 'inherit' }}
          >
            Dismiss
          </button>
        </div>
      )}
      <Router>
      <Routes>
        {/* Shared prompt route - accessible without auth */}
        <Route path="/shared/:id" element={<SharedPromptRunner />} />

        {/* All other routes require auth */}
        <Route path="*" element={
          !token ? (
            <AuthPage onAuth={handleAuth} />
          ) : (
            <div className="app-container">
              <Sidebar prompts={prompts} username={username} onLogout={handleLogout} loading={promptsLoading} error={promptsError} onRetry={refreshPrompts} />
              <Routes>
                <Route path="/" element={<div className="main-content"><div className="card"><h1>Welcome to My Prompt Lib</h1><p>Select a prompt from the sidebar or create a new one to get started.</p></div></div>} />
                <Route path="/new" element={<PromptEditor onSaved={refreshPrompts} />} />
                <Route path="/edit/:id" element={<PromptEditor onSaved={refreshPrompts} />} />
                <Route path="/prompt/:id" element={<PromptRunner onDeleted={refreshPrompts} />} />
                <Route path="/prompt/:id/history" element={<PromptHistory />} />
              </Routes>
            </div>
          )
        } />
      </Routes>
    </Router>
    </>
  );
}


export default App;
