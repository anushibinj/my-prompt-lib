import { useEffect, useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getPrompt, deletePrompt, type Prompt } from '../api/promptApi';
import { FiCopy, FiEdit2, FiTrash2, FiShare2, FiClock, FiRotateCcw } from 'react-icons/fi';

interface PromptRunnerProps {
  onDeleted?: () => void;
}

const varsStorageKey = (id: string) => `prompt_vars_${id}`;

export function PromptRunner({ onDeleted }: PromptRunnerProps) {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [prompt, setPrompt] = useState<Prompt | null>(null);
  const [variables, setVariables] = useState<Record<string, string>>({});
  const [copied, setCopied] = useState(false);
  const [linkCopied, setLinkCopied] = useState(false);

  useEffect(() => {
    if (id) {
      getPrompt(id).then(data => {
        setPrompt(data);
        const saved = (() => {
          try { return JSON.parse(localStorage.getItem(varsStorageKey(id)) || '{}'); }
          catch { return {}; }
        })();
        const initialVars: Record<string, string> = {};
        const matches = [...data.content.matchAll(/\{\{([^}]+)\}\}/g)];
        matches.forEach(match => {
          const key = match[1].trim();
          initialVars[key] = saved[key] ?? '';
        });
        setVariables(initialVars);
      }).catch(err => console.error(err));
    }
  }, [id]);

  const uniqueVars = useMemo(() => {
    if (!prompt) return [];
    return Array.from(new Set([...prompt.content.matchAll(/\{\{([^}]+)\}\}/g)].map(m => m[1].trim())));
  }, [prompt]);

  const handleVarChange = (prop: string, value: string) => {
    setVariables(prev => {
      const next = { ...prev, [prop]: value };
      if (id) localStorage.setItem(varsStorageKey(id), JSON.stringify(next));
      return next;
    });
  };

  const handleResetVars = () => {
    if (id) localStorage.removeItem(varsStorageKey(id));
    setVariables(prev => Object.fromEntries(Object.keys(prev).map(k => [k, ''])));
  };

  const finalPrompt = useMemo(() => {
    if (!prompt) return '';
    let res = prompt.content;
    Object.keys(variables).forEach(key => {
        const val = variables[key] || `{{${key}}}`;
        res = res.replaceAll(`{{${key}}}`, val);
    });
    return res;
  }, [prompt, variables]);

  const handleCopy = () => {
    navigator.clipboard.writeText(finalPrompt);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const [shareNote, setShareNote] = useState(false);

  const handleShareLink = () => {
    if (!prompt?.isPublic) {
      setShareNote(true);
      setTimeout(() => setShareNote(false), 3000);
      return;
    }
    const shareUrl = `${window.location.origin}/shared/${id}`;
    navigator.clipboard.writeText(shareUrl);
    setLinkCopied(true);
    setTimeout(() => setLinkCopied(false), 2000);
  };

  const handleDelete = async () => {
    if (id && window.confirm("Are you sure you want to delete this prompt?")) {
        await deletePrompt(id);
        onDeleted?.();
        navigate('/');
    }
  };

  if (!prompt) return <div className="main-content">Loading...</div>;

  return (
    <div className="main-content">
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '0.5rem' }}>
            <h1>{prompt.title}</h1>
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                <button className="btn" onClick={handleShareLink}>
                    <FiShare2 /> {linkCopied ? 'Link Copied!' : 'Share Link'}
                </button>
                <button className="btn" onClick={() => navigate(`/prompt/${id}/history`)}><FiClock /> History</button>
                <button className="btn" onClick={() => navigate(`/edit/${id}`)}><FiEdit2 /> Edit</button>
                <button className="btn btn-danger" onClick={handleDelete}><FiTrash2 /> Delete</button>
            </div>
        </div>

        {shareNote && (
            <div className="auth-error" style={{ marginBottom: '1rem' }}>
                This prompt is private. Make it public (via Edit) before sharing.
            </div>
        )}

        {prompt.isPublic && (
            <div className="public-badge">Public prompt</div>
        )}

        {uniqueVars.length > 0 && (
            <div className="variables-section">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                    <h3 style={{ color: 'var(--text-primary)', margin: 0 }}>Variables</h3>
                    <button className="btn btn-icon" onClick={handleResetVars} title="Reset all variables">
                        <FiRotateCcw /> Reset
                    </button>
                </div>
                <div className="variables-grid">
                    {uniqueVars.map(v => (
                        <div key={v} className="variable-input-wrapper">
                            <label>{v}</label>
                            <input
                                type="text"
                                className="input-field"
                                value={variables[v] || ''}
                                onChange={(e) => handleVarChange(v, e.target.value)}
                                placeholder={`Enter ${v}...`}
                                style={{ marginTop: '0.5rem' }}
                            />
                        </div>
                    ))}
                </div>
            </div>
        )}

        <h3 style={{ color: 'var(--text-muted)' }}>Output Preview</h3>
        <div className="output-preview">
            {finalPrompt}
        </div>
        
        <button className="btn btn-primary" onClick={handleCopy}>
            <FiCopy /> {copied ? 'Copied!' : 'Copy to Clipboard'}
        </button>
      </div>
    </div>
  );
}
