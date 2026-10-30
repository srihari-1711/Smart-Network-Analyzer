import React, { useState, useEffect } from 'react';
import { 
  Sparkles, 
  RotateCw, 
  CheckCircle2, 
  AlertCircle, 
  HelpCircle, 
  Bot, 
  Cpu, 
  ArrowRight,
  TrendingUp,
  History
} from 'lucide-react';
import api from '../api/client';
import HealthRing from '../components/HealthRing';

export default function AnalysisPage() {
  const [analyses, setAnalyses] = useState([]);
  const [activeAnalysis, setActiveAnalysis] = useState(null);
  const [analyzing, setAnalyzing] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchAnalyses = async () => {
    try {
      const res = await api.get('/analysis?page=1&page_size=10');
      const items = res.data?.items || [];
      setAnalyses(items);
      if (items.length > 0 && !activeAnalysis) {
        // Fetch detailed analysis
        const detailed = await api.get(`/analysis/${items[0].id}`);
        setActiveAnalysis(detailed.data);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnalyses();
  }, []);

  const handleRunAnalysis = async () => {
    setAnalyzing(true);
    try {
      const res = await api.post('/analysis', {
        use_llm: true,
        include_recent_metrics: true,
      });
      setActiveAnalysis(res.data);
      fetchAnalyses();
    } catch (err) {
      console.error('Failed to run AI analysis:', err);
    } finally {
      setAnalyzing(false);
    }
  };

  const handleSelectAnalysis = async (id) => {
    try {
      const res = await api.get(`/analysis/${id}`);
      setActiveAnalysis(res.data);
    } catch (e) {
      console.error(e);
    }
  };

  const issues = activeAnalysis?.issues || [];
  const recommendations = activeAnalysis?.recommendations || [];
  const isGroq = activeAnalysis?.source === 'groq_ai';

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2.5">
            <Sparkles className="w-6 h-6 text-purple-400" />
            <span>AI Diagnostic Engine</span>
          </h2>
          <p className="text-sm text-slate-400 mt-1">
            Real-time multi-dimensional network health analysis powered by Groq Llama/DeepSeek inference
          </p>
        </div>

        <button
          onClick={handleRunAnalysis}
          disabled={analyzing}
          className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-gradient-to-r from-purple-500 via-sky-500 to-cyan-400 hover:from-purple-400 hover:to-cyan-300 text-slate-950 text-xs font-bold shadow-lg shadow-purple-500/20 transition-all disabled:opacity-50"
        >
          <RotateCw className={`w-4 h-4 ${analyzing ? 'animate-spin' : ''}`} />
          <span>{analyzing ? 'Consulting Groq LLM...' : 'Run Live AI Diagnostic'}</span>
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Health Score & Overview */}
        <div className="space-y-6">
          <div className="rounded-3xl bg-gradient-to-b from-[#131926] to-[#0A0E17] border border-white/10 p-6 shadow-2xl flex flex-col items-center text-center">
            <span className="text-xs uppercase font-semibold text-slate-400 tracking-wider mb-4">
              Diagnostic Health Score
            </span>
            <HealthRing score={activeAnalysis?.health_score ?? null} size={180} />
            <div className="mt-4 flex items-center gap-2 px-3 py-1 rounded-full bg-purple-500/10 border border-purple-500/20 text-[11px] font-mono text-purple-300">
              <Bot className="w-3.5 h-3.5" />
              <span>Engine: {isGroq ? 'Groq LLM AI' : 'Deterministic Rules Engine'}</span>
            </div>
          </div>

          {/* Past Analyses */}
          <div className="rounded-3xl bg-[#131926]/90 border border-white/5 p-6 shadow-xl space-y-3">
            <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Diagnostic History ({analyses.length})
            </h3>
            <div className="space-y-2 max-h-72 overflow-y-auto">
              {analyses.map((a) => (
                <div
                  key={a.id}
                  onClick={() => handleSelectAnalysis(a.id)}
                  className={`p-3.5 rounded-2xl border transition-all cursor-pointer ${
                    activeAnalysis?.id === a.id
                      ? 'bg-purple-500/15 border-purple-500/30'
                      : 'bg-white/[0.02] border-white/5 hover:bg-white/[0.05]'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-white">Score {a.health_score}/100</span>
                    <span className="text-[10px] text-slate-500 font-mono">
                      {new Date(a.created_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-400 truncate mt-1">{a.summary}</p>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Column: AI Findings & Prioritized Remediation */}
        <div className="lg:col-span-2 space-y-6">
          {/* Executive Summary Card */}
          <div className="rounded-3xl bg-[#131926]/90 border border-white/10 p-6 shadow-2xl">
            <h3 className="text-sm font-bold text-white mb-2 flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-purple-400" />
              <span>Executive Synthesis</span>
            </h3>
            <p className="text-sm text-slate-300 leading-relaxed font-sans">
              {activeAnalysis?.summary ||
                'No diagnostic analysis loaded. Click "Run Live AI Diagnostic" to generate automated insights.'}
            </p>
          </div>

          {/* Issues Identified */}
          <div className="rounded-3xl bg-[#131926]/90 border border-white/5 p-6 shadow-xl space-y-3">
            <h3 className="text-sm font-bold text-white">Issues & Observations ({issues.length})</h3>
            {issues.length === 0 ? (
              <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-xs flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
                <span>No critical bottlenecks or configuration flaws detected.</span>
              </div>
            ) : (
              <div className="space-y-3">
                {issues.map((issue, idx) => (
                  <div key={idx} className="p-4 rounded-2xl bg-black/25 border border-white/5 flex items-start gap-3">
                    <AlertCircle className="w-4 h-4 text-amber-400 mt-0.5 flex-shrink-0" />
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-white">{issue.title || issue.code}</span>
                        {issue.severity && (
                          <span className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-rose-500/20 text-rose-300 uppercase">
                            {issue.severity}
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-slate-400 mt-1 leading-relaxed">{issue.description || issue.message}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Recommendations */}
          <div className="rounded-3xl bg-[#131926]/90 border border-white/5 p-6 shadow-xl space-y-3">
            <h3 className="text-sm font-bold text-white">Actionable Remediation Steps</h3>
            {recommendations.length === 0 ? (
              <div className="p-4 rounded-2xl bg-black/20 border border-white/5 text-slate-400 text-xs">
                Maintain current router channels and Wi-Fi configurations.
              </div>
            ) : (
              <div className="space-y-3">
                {recommendations.map((rec, idx) => (
                  <div key={idx} className="p-4 rounded-2xl bg-black/25 border border-white/5 flex items-start gap-3">
                    <div className="w-6 h-6 rounded-lg bg-sky-500/10 text-sky-400 flex items-center justify-center font-bold text-xs flex-shrink-0">
                      {idx + 1}
                    </div>
                    <div>
                      <h4 className="text-xs font-bold text-white">{rec.action || rec.title}</h4>
                      <p className="text-xs text-slate-400 mt-1 leading-relaxed">{rec.details || rec.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

