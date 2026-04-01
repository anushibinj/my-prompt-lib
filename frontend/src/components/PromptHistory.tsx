import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getPromptHistory, type PromptVersion } from '../api/promptApi';
import { FiArrowLeft } from 'react-icons/fi';

export function PromptHistory() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [versions, setVersions] = useState<PromptVersion[]>([]);
  const [selectedIdx, setSelectedIdx] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      getPromptHistory(id)
        .then(data => {
          setVersions(data);
          setLoading(false);
        })
        .catch(err => {
          console.error(err);
          setLoading(false);
        });
    }
  }, [id]);

  if (loading) return <div className="main-content">Loading...</div>;
  if (versions.length === 0) return (
    <div className="main-content">
      <div className="card">
        <button className="btn" onClick={() => navigate(`/prompt/${id}`)}><FiArrowLeft /> Back</button>
        <p style={{ marginTop: '1rem' }}>No version history available.</p>
      </div>
    </div>
  );

  const selected = selectedIdx !== null ? versions[selectedIdx] : null;
  // The previous version (older) for diff comparison
  const previousVersion = selectedIdx !== null && selectedIdx < versions.length - 1
    ? versions[selectedIdx + 1]
    : null;

  return (
    <div className="main-content">
      <div className="card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
          <button className="btn" onClick={() => navigate(`/prompt/${id}`)}><FiArrowLeft /> Back</button>
          <h1 style={{ margin: 0 }}>Version History</h1>
        </div>

        <div className="history-layout">
          <div className="history-list">
            {versions.map((v, idx) => (
              <div
                key={v.id}
                className={`history-item ${selectedIdx === idx ? 'active' : ''}`}
                onClick={() => setSelectedIdx(idx)}
              >
                <div className="history-item-header">
                  <span className="history-version">v{v.versionNumber}</span>
                  <span className="history-date">
                    {new Date(v.createdAt).toLocaleDateString(undefined, {
                      year: 'numeric', month: 'short', day: 'numeric',
                      hour: '2-digit', minute: '2-digit'
                    })}
                  </span>
                </div>
                <div className="history-item-title">{v.title}</div>
              </div>
            ))}
          </div>

          <div className="history-detail">
            {selected ? (
              <>
                <h2 style={{ marginTop: 0 }}>
                  v{selected.versionNumber} — {selected.title}
                </h2>
                {previousVersion ? (
                  <DiffView
                    oldTitle={previousVersion.title}
                    newTitle={selected.title}
                    oldContent={previousVersion.content}
                    newContent={selected.content}
                    oldVersion={previousVersion.versionNumber}
                    newVersion={selected.versionNumber}
                  />
                ) : (
                  <div>
                    <h3 style={{ color: 'var(--text-muted)', marginBottom: '0.5rem' }}>Initial version</h3>
                    <div className="output-preview">{selected.content}</div>
                  </div>
                )}
              </>
            ) : (
              <p style={{ color: 'var(--text-muted)' }}>Select a version to view details and diff.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

interface DiffViewProps {
  oldTitle: string;
  newTitle: string;
  oldContent: string;
  newContent: string;
  oldVersion: number;
  newVersion: number;
}

function DiffView({ oldTitle, newTitle, oldContent, newContent, oldVersion, newVersion }: DiffViewProps) {
  const titleChanged = oldTitle !== newTitle;
  const contentDiff = computeLineDiff(oldContent, newContent);

  return (
    <div>
      <h3 style={{ color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
        Changes from v{oldVersion} → v{newVersion}
      </h3>
      {titleChanged && (
        <div className="diff-title-change">
          <span className="diff-label">Title:</span>
          <span className="diff-removed-inline">{oldTitle}</span>
          <span style={{ color: 'var(--text-muted)' }}> → </span>
          <span className="diff-added-inline">{newTitle}</span>
        </div>
      )}
      <div className="diff-container">
        {contentDiff.map((line, i) => (
          <div key={i} className={`diff-line ${line.type}`}>
            <span className="diff-line-prefix">
              {line.type === 'added' ? '+' : line.type === 'removed' ? '-' : ' '}
            </span>
            <span className="diff-line-content">{line.text}</span>
          </div>
        ))}
        {contentDiff.length === 0 && (
          <div className="diff-line unchanged">
            <span className="diff-line-content" style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>Content unchanged</span>
          </div>
        )}
      </div>
    </div>
  );
}

interface DiffLine {
  type: 'added' | 'removed' | 'unchanged';
  text: string;
}

function computeLineDiff(oldText: string, newText: string): DiffLine[] {
  const oldLines = oldText.split('\n');
  const newLines = newText.split('\n');

  // Myers-like LCS diff
  const lcs = longestCommonSubsequence(oldLines, newLines);
  const result: DiffLine[] = [];

  let oi = 0, ni = 0, li = 0;
  while (oi < oldLines.length || ni < newLines.length) {
    if (li < lcs.length && oi < oldLines.length && ni < newLines.length &&
        oldLines[oi] === lcs[li] && newLines[ni] === lcs[li]) {
      result.push({ type: 'unchanged', text: oldLines[oi] });
      oi++; ni++; li++;
    } else if (oi < oldLines.length && (li >= lcs.length || oldLines[oi] !== lcs[li])) {
      result.push({ type: 'removed', text: oldLines[oi] });
      oi++;
    } else if (ni < newLines.length && (li >= lcs.length || newLines[ni] !== lcs[li])) {
      result.push({ type: 'added', text: newLines[ni] });
      ni++;
    }
  }

  return result;
}

function longestCommonSubsequence(a: string[], b: string[]): string[] {
  const m = a.length, n = b.length;
  const dp: number[][] = Array.from({ length: m + 1 }, () => Array(n + 1).fill(0));

  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      if (a[i - 1] === b[j - 1]) {
        dp[i][j] = dp[i - 1][j - 1] + 1;
      } else {
        dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
      }
    }
  }

  const result: string[] = [];
  let i = m, j = n;
  while (i > 0 && j > 0) {
    if (a[i - 1] === b[j - 1]) {
      result.unshift(a[i - 1]);
      i--; j--;
    } else if (dp[i - 1][j] > dp[i][j - 1]) {
      i--;
    } else {
      j--;
    }
  }
  return result;
}
