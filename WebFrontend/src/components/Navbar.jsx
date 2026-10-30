import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Activity, Sparkles, Gauge, Database, CheckCircle2, AlertCircle } from 'lucide-react';
import api from '../api/client';

export default function Navbar() {
  const navigate = useNavigate();
  const [serverOnline, setServerOnline] = useState(true);

  useEffect(() => {
    const checkServer = () => {
      api.get('/health')
        .then(() => setServerOnline(true))
        .catch(() => setServerOnline(false));
    };
    checkServer();
    const interval = setInterval(checkServer, 15000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="h-16 border-b border-white/5 bg-[#070A11]/80 backdrop-blur-md px-8 flex items-center justify-between sticky top-0 z-30">
      {/* Left: Backend / Database Status */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-white/[0.03] border border-white/5 text-xs text-slate-300">
          <Database className="w-3.5 h-3.5 text-sky-400" />
          <span className="text-[11px] font-mono text-slate-400">Database:</span>
          <span className="font-semibold text-slate-200">Shared SQLite</span>
          <span className="inline-block w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse ml-0.5"></span>
        </div>

        <div className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-mono border ${
          serverOnline 
            ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' 
            : 'bg-rose-500/10 text-rose-400 border-rose-500/20'
        }`}>
          {serverOnline ? (
            <>
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>Backend 8000: Live</span>
            </>
          ) : (
            <>
              <AlertCircle className="w-3.5 h-3.5" />
              <span>Backend Offline</span>
            </>
          )}
        </div>
      </div>

      {/* Right: Quick Trigger Actions */}
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/tests')}
          className="flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-white/[0.05] hover:bg-white/[0.08] text-slate-200 text-xs font-medium border border-white/5 transition-all"
        >
          <Gauge className="w-3.5 h-3.5 text-sky-400" />
          <span>Quick Test</span>
        </button>

        <button
          onClick={() => navigate('/analysis')}
          className="flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-gradient-to-r from-sky-500 to-cyan-400 hover:from-sky-400 hover:to-cyan-300 text-slate-950 text-xs font-bold shadow-md shadow-sky-500/20 transition-all"
        >
          <Sparkles className="w-3.5 h-3.5" />
          <span>AI Inspect</span>
        </button>
      </div>
    </header>
  );
}

