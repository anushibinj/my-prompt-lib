import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createPrompt, getPrompt, updatePrompt, type Prompt } from '../api/promptApi';

export function PromptEditor() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        if (id) {
            getPrompt(id).then(data => {
                setTitle(data.title);
                setContent(data.content);
            });
        }
    }, [id]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSaving(true);
        try {
            const payload: Prompt = { title, content };
            if (id) {
                await updatePrompt(id, payload);
            } else {
                await createPrompt(payload);
            }
            navigate('/');
            window.location.reload();
        } catch (err) {
            console.error('Error saving:', err);
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="main-content">
            <div className="card">
                <h1>{id ? 'Edit Prompt' : 'Create New Prompt'}</h1>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Title</label>
                        <input 
                            type="text" 
                            required 
                            className="input-field" 
                            placeholder="e.g. Midjourney Animal Prompt"
                            value={title} 
                            onChange={e => setTitle(e.target.value)} 
                        />
                    </div>
                    
                    <div className="form-group">
                        <label>Prompt Template</label>
                        <p style={{fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '-0.3rem', marginBottom: '0.5rem'}}>
                            Use {'{{variable_name}}'} syntax to add template variables.
                        </p>
                        <textarea 
                            required 
                            className="input-field" 
                            placeholder="Create an image of an {{animal}} eating {{food}}..."
                            value={content} 
                            onChange={e => setContent(e.target.value)} 
                        />
                    </div>

                    <div style={{ display: 'flex', gap: '1rem' }}>
                        <button type="submit" className="btn btn-primary" disabled={isSaving}>
                            {isSaving ? 'Saving...' : 'Save Prompt'}
                        </button>
                        <button type="button" className="btn" onClick={() => navigate(-1)}>
                            Cancel
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
