import { useState } from 'react';
import { Link } from 'react-router-dom';
import type { Prompt } from '../api/promptApi';
import { FiLogOut, FiMenu, FiX, FiRefreshCw, FiChevronsLeft, FiChevronsRight, FiPlus } from 'react-icons/fi';

interface SidebarProps {
  prompts: Prompt[];
  username: string | null;
  onLogout: () => void;
  loading?: boolean;
  error?: boolean;
  onRetry?: () => void;
}

export function Sidebar({ prompts, username, onLogout, loading, error, onRetry }: SidebarProps) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(() => localStorage.getItem('sidebar_collapsed') === 'true');

  const toggleCollapse = () => {
    setCollapsed(prev => {
      const next = !prev;
      localStorage.setItem('sidebar_collapsed', String(next));
      return next;
    });
  };

  return (
    <>
      <button className="mobile-menu-btn" onClick={() => setMobileOpen(true)}>
        <FiMenu />
      </button>
      {mobileOpen && <div className="sidebar-overlay" onClick={() => setMobileOpen(false)} />}
      <div className={`sidebar ${mobileOpen ? 'sidebar-open' : ''} ${collapsed ? 'sidebar-collapsed' : ''}`}>
        {collapsed ? (
          <div className="sidebar-rail">
            <button className="btn btn-icon" onClick={toggleCollapse} title="Expand sidebar">
              <FiChevronsRight />
            </button>
            <Link to="/new" className="btn btn-primary sidebar-rail-new" onClick={() => setMobileOpen(false)} title="New prompt">
              <FiPlus />
            </Link>
          </div>
        ) : (
          <>
            <div className="sidebar-header">
              <Link to="/" style={{ textDecoration: 'none', color: 'inherit' }} onClick={() => setMobileOpen(false)}>
                🧩 My Prompt Lib
              </Link>
              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                <Link to="/new" className="btn btn-primary" style={{ padding: '0.4rem 0.6rem', fontSize: '1rem' }} onClick={() => setMobileOpen(false)}>+</Link>
                <button className="btn btn-icon" onClick={toggleCollapse} title="Collapse sidebar">
                  <FiChevronsLeft />
                </button>
                <button className="btn btn-icon sidebar-close-btn" onClick={() => setMobileOpen(false)}>
                  <FiX />
                </button>
              </div>
            </div>
            <div className="prompt-list">
              {loading ? (
                <div style={{ padding: '1rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span className="sidebar-spinner" /> Loading prompts...
                </div>
              ) : error ? (
                <div style={{ padding: '1rem' }}>
                  <div style={{ color: 'var(--accent-danger)', marginBottom: '0.5rem', fontSize: '0.85rem' }}>Failed to load prompts.</div>
                  <button className="btn" style={{ fontSize: '0.8rem', padding: '0.4rem 0.75rem' }} onClick={onRetry}>
                    <FiRefreshCw /> Retry
                  </button>
                </div>
              ) : (
                <>
                  {prompts.map(prompt => (
                    <Link key={prompt.id} to={`/prompt/${prompt.id}`} className="prompt-item" onClick={() => setMobileOpen(false)}>
                      <div className="title">{prompt.title}</div>
                      <div className="snippet">{prompt.content}</div>
                    </Link>
                  ))}
                  {prompts.length === 0 && (
                    <div style={{ padding: '1rem', color: 'var(--text-muted)' }}>No prompts found.</div>
                  )}
                </>
              )}
            </div>
            <div className="sidebar-footer">
              <span className="sidebar-username">{username}</span>
              <button className="btn btn-icon" onClick={onLogout} title="Sign out">
                <FiLogOut />
              </button>
            </div>
          </>
        )}
      </div>
    </>
  );
}
