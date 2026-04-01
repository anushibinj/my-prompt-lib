import { useState } from 'react';
import { Link } from 'react-router-dom';
import type { Prompt } from '../api/promptApi';
import { FiLogOut, FiMenu, FiX } from 'react-icons/fi';

interface SidebarProps {
  prompts: Prompt[];
  username: string | null;
  onLogout: () => void;
}

export function Sidebar({ prompts, username, onLogout }: SidebarProps) {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <>
      <button className="mobile-menu-btn" onClick={() => setMobileOpen(true)}>
        <FiMenu />
      </button>
      {mobileOpen && <div className="sidebar-overlay" onClick={() => setMobileOpen(false)} />}
      <div className={`sidebar ${mobileOpen ? 'sidebar-open' : ''}`}>
        <div className="sidebar-header">
          🧩 PromptLib
          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
            <Link to="/new" className="btn btn-primary" style={{ padding: '0.4rem 0.6rem', fontSize: '1rem' }} onClick={() => setMobileOpen(false)}>+</Link>
            <button className="btn btn-icon sidebar-close-btn" onClick={() => setMobileOpen(false)}>
              <FiX />
            </button>
          </div>
        </div>
        <div className="prompt-list">
          {prompts.map(prompt => (
            <Link key={prompt.id} to={`/prompt/${prompt.id}`} className="prompt-item" onClick={() => setMobileOpen(false)}>
              <div className="title">{prompt.title}</div>
              <div className="snippet">{prompt.content}</div>
            </Link>
          ))}
          {prompts.length === 0 && (
            <div style={{ padding: '1rem', color: 'var(--text-muted)' }}>No prompts found.</div>
          )}
        </div>
        <div className="sidebar-footer">
          <span className="sidebar-username">{username}</span>
          <button className="btn btn-icon" onClick={onLogout} title="Sign out">
            <FiLogOut />
          </button>
        </div>
      </div>
    </>
  );
}
