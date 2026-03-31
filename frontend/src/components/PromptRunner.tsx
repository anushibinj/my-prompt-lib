import { useEffect, useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getPrompt, deletePrompt, type Prompt } from '../api/promptApi';
import { FiCopy, FiEdit2, FiTrash2 } from 'react-icons/fi';

export function PromptRunner() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [prompt, setPrompt] = useState<Prompt | null>(null);
  const [variables, setVariables] = useState<Record<string, string>>({});
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (id) {
      getPrompt(id).then(data => {
        setPrompt(data);
        // Initialize empty variable state
        const initialVars: Record<string, string> = {};
        const matches = [...data.content.matchAll(/\{\{([^}]+)\}\}/g)];
        matches.forEach(match => {
           initialVars[match[1].trim()] = '';
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

  const handleDelete = async () => {
    if (id && window.confirm("Are you sure you want to delete this prompt?")) {
        await deletePrompt(id);
        navigate('/');
        // Ideal logic would refresh parent state too.
        window.location.reload(); 
    }
  };

  if (!prompt) return <div className="main-content">Loading...</div>;

  return (
    <div className="main-content">
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <h1>{prompt.title}</h1>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button className="btn" onClick={() => navigate(`/edit/${id}`)}><FiEdit2 /> Edit</button>
                <button className="btn btn-danger" onClick={handleDelete}><FiTrash2 /> Delete</button>
            </div>
        </div>

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
  );
}
