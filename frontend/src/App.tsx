import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Sidebar } from './components/Sidebar';
import { PromptRunner } from './components/PromptRunner';
import { PromptEditor } from './components/PromptEditor';
import { getPrompts, type Prompt } from './api/promptApi';

function App() {
  const [prompts, setPrompts] = useState<Prompt[]>([]);

  useEffect(() => {
    getPrompts().then(setPrompts).catch(console.error);
  }, []);

  return (
    <Router>
      <div className="app-container">
        <Sidebar prompts={prompts} />
        <Routes>
          <Route path="/" element={<div className="main-content"><div className="card"><h1>Welcome to PromptLib</h1><p>Select a prompt from the sidebar or create a new one to get started.</p></div></div>} />
          <Route path="/new" element={<PromptEditor />} />
          <Route path="/edit/:id" element={<PromptEditor />} />
          <Route path="/prompt/:id" element={<PromptRunner />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
