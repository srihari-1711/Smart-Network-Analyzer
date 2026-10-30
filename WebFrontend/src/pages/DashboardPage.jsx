import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Download, 
  Upload, 
  Activity, 
  Wifi, 
  Sparkles, 
  Gauge, 
  RefreshCw, 
  Radio, 
  MapPin, 
  FileText,
  ShieldCheck,
  ArrowRight,
  TrendingUp
} from 'lucide-react';
import api from '../api/client';
import HealthRing from '../components/HealthRing';

export default function DashboardPage() {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchDashboard = async () => {
    try {
      const res = await api.get('/dashboard');
      setData(res.data);
    } catch (err) {
      console.error('Failed to load dashboard:', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const handleRefresh = () => {
    setRefreshing(true);
    fetchDashboard();
  };

  const healthScore = data?.latest_analysis?.health_score ?? null;
  const downloadMbps = data?.latest_speed_test?.download_mbps ? data.latest_speed_test.download_mbps.toFixed(1) : '--';
  const uploadMbps = data?.latest_speed_test?.upload_mbps ? data.latest_speed_test.upload_mbps.toFixed(1) : '--';
  const latencyMs = data?.latest_latency_test?.avg_latency_ms ? Math.round(data.latest_latency_test.avg_latency_ms) : '--';
  const signalDbm = data?.current_signal_dbm ?? data?.latest_wifi_scan_summary?.strongest_signal_dbm ?? '--';

  return (
    <div className="space-y-8 max-w-7xl mx-auto pb-12">
      {/* Top Banner / Hero */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-white">Network Overview</h2>
          <p className="text-sm text-slate-400 mt-1">Real-time health, performance telemetry, and diagnostics</p>
        </div>
        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-white/[0.04] hover:bg-white/[0.08] text-slate-300 text-xs font-medium border border-white/5 transition-all"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${refreshing ? 'animate-spin text-sky-400' : ''}`} />
          <span>Refresh Sync</span>
        </button>
      </div>

      {/* Hero Health Score Card */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-b from-[#131926] to-[#0D121D] border border-white/10 p-8 shadow-2xl">
        <div className="absolute top-0 right-0 w-96 h-96 bg-sky-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="relative z-10 flex flex-col md:flex-row items-center justify-between gap-8">
          <div className="flex-1 space-y-4 text-center md:text-left">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-sky-500/10 border border-sky-500/20 text-xs text-sky-400 font-semibold tracking-wide uppercase">
              <Activity className="w-3.5 h-3.5" />
              <span>Network Health Diagnostic</span>
            </div>
            <h3 className="text-2xl font-extrabold text-white tracking-tight">
              {data?.latest_analysis ? data.latest_analysis.summary : 'No Diagnostic Run Yet'}
            </h3>
            <p className="text-slate-400 text-sm max-w-xl leading-relaxed">
              {data?.latest_analysis
                ? `Diagnostic completed via ${data.latest_analysis.source === 'groq_ai' ? 'Groq LLM AI' : 'Deterministic Engine'}. ${data.latest_analysis.issues?.length || 0} optimization areas detected.`
                : 'Execute your first AI diagnostic to inspect latency, Wi-Fi channel crowding, speed stability, and security posture.'}
            </p>
            <div className="pt-2 flex flex-wrap gap-3 justify-center md:justify-start">
              <button
                onClick={() => navigate('/analysis')}
                className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-cyan-400 hover:from-sky-400 hover:to-cyan-300 text-slate-950 font-bold text-xs shadow-lg shadow-sky-500/20 transition-all"
              >
                <Sparkles className="w-4 h-4" />
                <span>Run AI Diagnostic</span>
              </button>
              <button
                onClick={() => navigate('/tests')}
                className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-white/[0.05] hover:bg-white/[0.08] text-white font-medium text-xs border border-white/10 transition-all"
              >
                <Gauge className="w-4 h-4 text-sky-400" />
                <span>Speed Test</span>
              </button>
            </div>
          </div>

          <div className="flex-shrink-0 bg-black/20 p-6 rounded-3xl border border-white/5">
            <HealthRing score={healthScore} size={170} />
          </div>
        </div>
      </div>

      {/* Telemetry Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Download Speed */}
        <div className="bg-[#131926]/80 border border-white/5 rounded-2xl p-5 hover:border-sky-500/30 transition-all group">
          <div className="flex items-center justify-between text-slate-400 mb-3">
            <span className="text-xs font-semibold uppercase tracking-wider">Download Speed</span>
            <div className="w-8 h-8 rounded-lg bg-sky-500/10 text-sky-400 flex items-center justify-center">
              <Download className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-white font-mono tracking-tight">{downloadMbps}</span>
            <span className="text-xs text-sky-400 font-semibold">Mbps</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2">Latest throughput measurement</p>
        </div>

        {/* Upload Speed */}
        <div className="bg-[#131926]/80 border border-white/5 rounded-2xl p-5 hover:border-cyan-500/30 transition-all group">
          <div className="flex items-center justify-between text-slate-400 mb-3">
            <span className="text-xs font-semibold uppercase tracking-wider">Upload Speed</span>
            <div className="w-8 h-8 rounded-lg bg-cyan-500/10 text-cyan-400 flex items-center justify-center">
              <Upload className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-white font-mono tracking-tight">{uploadMbps}</span>
            <span className="text-xs text-cyan-400 font-semibold">Mbps</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2">Upstream link capacity</p>
        </div>

        {/* Latency */}
        <div className="bg-[#131926]/80 border border-white/5 rounded-2xl p-5 hover:border-emerald-500/30 transition-all group">
          <div className="flex items-center justify-between text-slate-400 mb-3">
            <span className="text-xs font-semibold uppercase tracking-wider">Latency</span>
            <div className="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
              <Activity className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-white font-mono tracking-tight">{latencyMs}</span>
            <span className="text-xs text-emerald-400 font-semibold">ms</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2">
            {data?.latest_latency_test?.target_host ? `Target: ${data.latest_latency_test.target_host}` : 'Round-trip response time'}
          </p>
        </div>

        {/* Wi-Fi Signal */}
        <div className="bg-[#131926]/80 border border-white/5 rounded-2xl p-5 hover:border-purple-500/30 transition-all group">
          <div className="flex items-center justify-between text-slate-400 mb-3">
            <span className="text-xs font-semibold uppercase tracking-wider">Wi-Fi Signal</span>
            <div className="w-8 h-8 rounded-lg bg-purple-500/10 text-purple-400 flex items-center justify-center">
              <Wifi className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-white font-mono tracking-tight">{signalDbm}</span>
            <span className="text-xs text-purple-400 font-semibold">dBm</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-2">
            {data?.latest_wifi_scan_summary?.connected_ssid ? `SSID: ${data.latest_wifi_scan_summary.connected_ssid}` : 'RSSI signal strength'}
          </p>
        </div>
      </div>

      {/* Activity & Diagnostics Summary */}
      <div className="rounded-2xl bg-[#131926]/60 border border-white/5 p-6">
        <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4">
          Activity & Diagnostics Summary
        </h4>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
          <div className="p-4 rounded-xl bg-black/20 border border-white/5">
            <div className="text-xs text-slate-400">Speed Tests</div>
            <div className="text-2xl font-bold font-mono text-white mt-1">{data?.counts?.speed_tests || 0}</div>
          </div>
          <div className="p-4 rounded-xl bg-black/20 border border-white/5">
            <div className="text-xs text-slate-400">Ping Tests</div>
            <div className="text-2xl font-bold font-mono text-white mt-1">{data?.counts?.latency_tests || 0}</div>
          </div>
          <div className="p-4 rounded-xl bg-black/20 border border-white/5">
            <div className="text-xs text-slate-400">Wi-Fi Scans</div>
            <div className="text-2xl font-bold font-mono text-white mt-1">{data?.counts?.wifi_scans || 0}</div>
          </div>
          <div className="p-4 rounded-xl bg-black/20 border border-white/5">
            <div className="text-xs text-slate-400">Floor Maps</div>
            <div className="text-2xl font-bold font-mono text-white mt-1">{data?.counts?.signal_maps || 0}</div>
          </div>
          <div className="p-4 rounded-xl bg-black/20 border border-white/5">
            <div className="text-xs text-slate-400">Audit Reports</div>
            <div className="text-2xl font-bold font-mono text-white mt-1">{data?.counts?.reports || 0}</div>
          </div>
        </div>
      </div>

      {/* Quick Navigation Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div 
          onClick={() => navigate('/wifi')}
          className="p-5 rounded-2xl bg-[#131926]/80 border border-white/5 hover:border-sky-500/30 transition-all cursor-pointer group flex items-center justify-between"
        >
          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-xl bg-sky-500/10 text-sky-400 flex items-center justify-center">
              <Radio className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-semibold text-white group-hover:text-sky-400 transition-colors">Wi-Fi Scanner</h4>
              <p className="text-xs text-slate-400">Analyze AP frequencies & channels</p>
            </div>
          </div>
          <ArrowRight className="w-4 h-4 text-slate-500 group-hover:text-white group-hover:translate-x-1 transition-all" />
        </div>

        <div 
          onClick={() => navigate('/security')}
          className="p-5 rounded-2xl bg-[#131926]/80 border border-white/5 hover:border-emerald-500/30 transition-all cursor-pointer group flex items-center justify-between"
        >
          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-semibold text-white group-hover:text-emerald-400 transition-colors">Security Audit</h4>
              <p className="text-xs text-slate-400">Inspect encryption & rogue APs</p>
            </div>
          </div>
          <ArrowRight className="w-4 h-4 text-slate-500 group-hover:text-white group-hover:translate-x-1 transition-all" />
        </div>

        <div 
          onClick={() => navigate('/maps')}
          className="p-5 rounded-2xl bg-[#131926]/80 border border-white/5 hover:border-purple-500/30 transition-all cursor-pointer group flex items-center justify-between"
        >
          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center">
              <MapPin className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-semibold text-white group-hover:text-purple-400 transition-colors">Signal Heatmaps</h4>
              <p className="text-xs text-slate-400">Visual floor plan coverage</p>
            </div>
          </div>
          <ArrowRight className="w-4 h-4 text-slate-500 group-hover:text-white group-hover:translate-x-1 transition-all" />
        </div>
      </div>
    </div>
  );
}

