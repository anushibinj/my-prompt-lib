import { useEffect, useState, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { getSharedPrompt, type Prompt } from '../api/promptApi';
import { FiCopy } from 'react-icons/fi';

export function SharedPromptRunner() {
  const { id } = useParams<{ id: string }>();
  const [prompt, setPrompt] = useState<Prompt | null>(null);
  const [variables, setVariables] = useState<Record<string, string>>({});
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (id) {
      getSharedPrompt(id).then(data => {
        setPrompt(data);
        const initialVars: Record<string, string> = {};
        const matches = [...data.content.matchAll(/\{\{([^}]+)\}\}/g)];
        matches.forEach(match => {
          initialVars[match[1].trim()] = '';
        });
        setVariables(initialVars);
      }).catch(() => setError('Prompt not found or is not public.'));
    }
  }, [id]);

  const uniqueVars = useMemo(() => {
    if (!prompt) return [];
    return Array.from(new Set([...prompt.content.matchAll(/\{\{([^}]+)\}\}/g)].map(m => m[1].trim())));
  }, [prompt]);

  const handleVarChange = (prop: string, value: string) => {
    setVariables(prev => ({ ...prev, [prop]: value }));
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

  if (error) {
    return (
      <div className="shared-page">
        <div className="card" style={{ maxWidth: '600px', margin: '2rem auto', textAlign: 'center' }}>
          <h1>🧩 My Prompt Lib</h1>
          <p style={{ color: 'var(--accent-danger)' }}>{error}</p>
          <a href="/" className="btn btn-primary">Go to My Prompt Lib</a>
        </div>
      </div>
    );
  }

  if (!prompt) return <div className="shared-page"><div className="main-content">Loading...</div></div>;

  return (
    <div className="shared-page">
      <div style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem' }}>
        <div style={{ marginBottom: '1rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          🧩 Shared via <a href="/" style={{ color: 'var(--text-primary)' }}>My Prompt Lib</a>
        </div>
        <div className="card">
          <h1>{prompt.title}</h1>

          {uniqueVars.length > 0 && (
            <div className="variables-section">
              <h3 style={{ color: 'var(--text-primary)', marginTop: 0 }}>Variables</h3>
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
    </div>
  );
}
