import { Link } from 'react-router-dom';
import type { Prompt } from '../api/promptApi';

interface SidebarProps {
  prompts: Prompt[];
}

export function Sidebar({ prompts }: SidebarProps) {
  return (
    <div className="sidebar">
      <div className="sidebar-header">
        🧩 PromptLib
        <Link to="/new" className="btn btn-primary" style={{ padding: '0.4rem 0.6rem', fontSize: '1rem' }}>+</Link>
      </div>
      <div className="prompt-list">
        {prompts.map(prompt => (
          <Link key={prompt.id} to={`/prompt/${prompt.id}`} className="prompt-item">
            <div className="title">{prompt.title}</div>
            <div className="snippet">{prompt.content}</div>
          </Link>
        ))}
        {prompts.length === 0 && (
          <div style={{ padding: '1rem', color: 'var(--text-muted)' }}>No prompts found.</div>
        )}
      </div>
    </div>
  );
}
