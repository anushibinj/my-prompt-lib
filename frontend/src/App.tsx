import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useEffect, useState, useCallback } from 'react';
import { Sidebar } from './components/Sidebar';
import { PromptRunner } from './components/PromptRunner';
import { PromptEditor } from './components/PromptEditor';
import { PromptHistory } from './components/PromptHistory';
import { SharedPromptRunner } from './components/SharedPromptRunner';
import { AuthPage } from './components/AuthPage';
import { getPrompts, logout, type Prompt } from './api/promptApi';

function App() {
  const [prompts, setPrompts] = useState<Prompt[]>([]);
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [username, setUsername] = useState<string | null>(localStorage.getItem('username'));

  const handleLogout = useCallback(() => {
    logout();
    setToken(null);
    setUsername(null);
    setPrompts([]);
  }, []);

  const refreshPrompts = useCallback(() => {
    if (token) {
      getPrompts().then(setPrompts).catch(() => {
        // Token may be expired/invalid
        handleLogout();
      });
    }
  }, [token, handleLogout]);

  useEffect(() => {
    refreshPrompts();
  }, [refreshPrompts]);

  const handleAuth = (newToken: string, newUsername: string) => {
    setToken(newToken);
    setUsername(newUsername);
  };

  return (
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
              <Sidebar prompts={prompts} username={username} onLogout={handleLogout} />
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
  );
}

export default App;
