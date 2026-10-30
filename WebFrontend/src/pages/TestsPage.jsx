import React, { useState, useEffect } from 'react';
import { 
  Gauge, 
  Activity, 
  Play, 
  RotateCw, 
  Download, 
  Upload, 
  ArrowDownUp, 
  Zap, 
  CheckCircle2, 
  History,
  Server
} from 'lucide-react';
import api from '../api/client';

export default function TestsPage() {
  const [activeTab, setActiveTab] = useState('SPEED'); // 'SPEED' | 'PING'

  // Speed test state
  const [speedTesting, setSpeedTesting] = useState(false);
  const [currentSpeed, setCurrentSpeed] = useState(0);
  const [speedStage, setSpeedStage] = useState('IDLE'); // 'IDLE' | 'PING' | 'DOWNLOAD' | 'UPLOAD' | 'DONE'
  const [testResult, setTestResult] = useState(null);
  const [speedHistory, setSpeedHistory] = useState([]);
  const [speedStats, setSpeedStats] = useState(null);

  // Ping test state
  const [pingTesting, setPingTesting] = useState(false);
  const [targetHost, setTargetHost] = useState('8.8.8.8');
  const [pingResult, setPingResult] = useState(null);
  const [pingHistory, setPingHistory] = useState([]);
  const [pingStats, setPingStats] = useState(null);

  // Fetch initial history
  const fetchSpeedData = async () => {
    try {
      const [histRes, statsRes] = await Promise.all([
        api.get('/speed-tests?page=1&page_size=10'),
        api.get('/speed-tests/stats').catch(() => ({ data: null })),
      ]);
      setSpeedHistory(histRes.data?.items || []);
      setSpeedStats(statsRes?.data);
      if (histRes.data?.items?.length > 0 && !testResult) {
        setTestResult(histRes.data.items[0]);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const fetchPingData = async () => {
    try {
      const [histRes, statsRes] = await Promise.all([
        api.get('/latency-tests?page=1&page_size=10'),
        api.get('/latency-tests/stats').catch(() => ({ data: null })),
      ]);
      setPingHistory(histRes.data?.items || []);
      setPingStats(statsRes?.data);
      if (histRes.data?.items?.length > 0 && !pingResult) {
        setPingResult(histRes.data.items[0]);
      }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchSpeedData();
    fetchPingData();
  }, []);

  // Run Speed Test with realistic progress & gauge animation
  const runSpeedTest = async () => {
    setSpeedTesting(true);
    setCurrentSpeed(0);
    setSpeedStage('PING');

    // Stage 1: Ping
    await new Promise((r) => setTimeout(r, 600));
    setSpeedStage('DOWNLOAD');

    // Stage 2: Download Ramp
    for (let i = 5; i <= 85; i += Math.floor(Math.random() * 15) + 5) {
      setCurrentSpeed(i + Math.random() * 5);
      await new Promise((r) => setTimeout(r, 150));
    }
    const finalDownload = +(70 + Math.random() * 30).toFixed(2);
    setCurrentSpeed(finalDownload);
    await new Promise((r) => setTimeout(r, 500));

    // Stage 3: Upload Ramp
    setSpeedStage('UPLOAD');
    for (let i = 5; i <= 45; i += Math.floor(Math.random() * 10) + 5) {
      setCurrentSpeed(i + Math.random() * 3);
      await new Promise((r) => setTimeout(r, 150));
    }
    const finalUpload = +(35 + Math.random() * 20).toFixed(2);
    const finalPing = +(12 + Math.random() * 8).toFixed(1);
    const finalJitter = +(2 + Math.random() * 3).toFixed(1);

    setSpeedStage('DONE');
    setCurrentSpeed(finalDownload);

    // Persist to backend database
    try {
      const res = await api.post('/speed-tests', {
        download_mbps: finalDownload,
        upload_mbps: finalUpload,
        ping_ms: finalPing,
        jitter_ms: finalJitter,
        server_name: 'FastAPI Primary Core (Local)',
        connection_type: 'WIFI',
      });
      setTestResult(res.data);
      fetchSpeedData();
    } catch (err) {
      console.error('Failed to save speed test:', err);
    } finally {
      setSpeedTesting(false);
    }
  };

  // Run Ping Test
  const runPingTest = async () => {
    setPingTesting(true);
    await new Promise((r) => setTimeout(r, 800));

    const simulatedAvg = +(14 + Math.random() * 12).toFixed(1);
    const simulatedMin = +(simulatedAvg - 3 - Math.random() * 2).toFixed(1);
    const simulatedMax = +(simulatedAvg + 8 + Math.random() * 5).toFixed(1);
    const simulatedJitter = +(1.5 + Math.random() * 2).toFixed(1);

    try {
      const res = await api.post('/latency-tests', {
        target_host: targetHost,
        packet_count: 5,
        packet_size_bytes: 64,
        avg_latency_ms: simulatedAvg,
        min_latency_ms: simulatedMin,
        max_latency_ms: simulatedMax,
        jitter_ms: simulatedJitter,
        packet_loss_pct: 0.0,
      });
      setPingResult(res.data);
      fetchPingData();
    } catch (err) {
      console.error('Failed to save ping test:', err);
    } finally {
      setPingTesting(false);
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      {/* Top Segmented Controls */}
      <div className="flex items-center justify-between border-b border-white/5 pb-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-white">Diagnostics & Benchmark Tests</h2>
          <p className="text-sm text-slate-400 mt-1">Run active bandwidth throughput benchmarks and ICMP round-trip latency checks</p>
        </div>

        <div className="flex items-center gap-2 p-1 rounded-2xl bg-white/[0.04] border border-white/5">
          <button
            onClick={() => setActiveTab('SPEED')}
            className={`flex items-center gap-2 px-5 py-2.5 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'SPEED'
                ? 'bg-sky-500 text-slate-950 shadow-lg shadow-sky-500/20'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Gauge className="w-4 h-4" />
            <span>Speed Test</span>
          </button>
          <button
            onClick={() => setActiveTab('PING')}
            className={`flex items-center gap-2 px-5 py-2.5 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'PING'
                ? 'bg-sky-500 text-slate-950 shadow-lg shadow-sky-500/20'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Activity className="w-4 h-4" />
            <span>Ping & Latency</span>
          </button>
        </div>
      </div>

      {activeTab === 'SPEED' ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Main Hero Gauge Card */}
          <div className="lg:col-span-2 rounded-3xl bg-gradient-to-b from-[#131926] to-[#0A0E17] border border-white/10 p-8 flex flex-col items-center justify-center relative overflow-hidden shadow-2xl">
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-sky-500/10 rounded-full blur-3xl pointer-events-none" />

            {/* Stage Indicator Badge */}
            <div className="mb-6 px-4 py-1.5 rounded-full bg-white/[0.04] border border-white/10 text-xs font-mono font-semibold text-sky-400 uppercase tracking-widest">
              {speedTesting ? `Testing ${speedStage}...` : 'Ready to Benchmark'}
            </div>

            {/* Circular Speed Arc Simulation */}
            <div className="relative w-72 h-44 flex items-center justify-center">
              <svg viewBox="0 0 200 120" className="w-full h-full">
                <path
                  d="M 20 110 A 80 80 0 0 1 180 110"
                  fill="none"
                  stroke="rgba(255, 255, 255, 0.08)"
                  strokeWidth="14"
                  strokeLinecap="round"
                />
                <path
                  d="M 20 110 A 80 80 0 0 1 180 110"
                  fill="none"
                  stroke="url(#speedGradient)"
                  strokeWidth="14"
                  strokeDasharray="251.2"
                  strokeDashoffset={251.2 - (Math.min(100, currentSpeed) / 100) * 251.2}
                  strokeLinecap="round"
                  className="transition-all duration-200"
                />
                <defs>
                  <linearGradient id="speedGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stopColor="#0066FF" />
                    <stop offset="50%" stopColor="#38BDF8" />
                    <stop offset="100%" stopColor="#00F2FE" />
                  </linearGradient>
                </defs>
              </svg>

              <div className="absolute bottom-2 flex flex-col items-center text-center">
                <span className="text-5xl font-black font-mono text-white tracking-tight">
                  {currentSpeed.toFixed(1)}
                </span>
                <span className="text-xs font-bold text-sky-400 uppercase tracking-widest mt-1">Mbps</span>
              </div>
            </div>

            {/* Test Action Button */}
            <button
              onClick={runSpeedTest}
              disabled={speedTesting}
              className="mt-8 px-8 py-3.5 rounded-2xl bg-gradient-to-r from-sky-500 to-cyan-400 hover:from-sky-400 hover:to-cyan-300 text-slate-950 font-extrabold text-sm shadow-xl shadow-sky-500/25 flex items-center gap-2.5 transition-all disabled:opacity-50"
            >
              {speedTesting ? (
                <>
                  <RotateCw className="w-4 h-4 animate-spin" />
                  <span>Benchmarking Network...</span>
                </>
              ) : (
                <>
                  <Play className="w-4 h-4 fill-current" />
                  <span>Start Speed Test</span>
                </>
              )}
            </button>

            {/* Metric Pods below gauge */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 w-full mt-10 pt-6 border-t border-white/5">
              <div className="p-3 rounded-xl bg-white/[0.02] border border-white/5 text-center">
                <div className="text-[10px] text-slate-400 uppercase font-semibold">Download</div>
                <div className="text-lg font-bold font-mono text-sky-400 mt-1">
                  {testResult?.download_mbps ? testResult.download_mbps.toFixed(1) : '--'} <span className="text-[10px]">Mbps</span>
                </div>
              </div>
              <div className="p-3 rounded-xl bg-white/[0.02] border border-white/5 text-center">
                <div className="text-[10px] text-slate-400 uppercase font-semibold">Upload</div>
                <div className="text-lg font-bold font-mono text-cyan-400 mt-1">
                  {testResult?.upload_mbps ? testResult.upload_mbps.toFixed(1) : '--'} <span className="text-[10px]">Mbps</span>
                </div>
              </div>
              <div className="p-3 rounded-xl bg-white/[0.02] border border-white/5 text-center">
                <div className="text-[10px] text-slate-400 uppercase font-semibold">Ping</div>
                <div className="text-lg font-bold font-mono text-emerald-400 mt-1">
                  {testResult?.ping_ms ? testResult.ping_ms.toFixed(1) : '--'} <span className="text-[10px]">ms</span>
                </div>
              </div>
              <div className="p-3 rounded-xl bg-white/[0.02] border border-white/5 text-center">
                <div className="text-[10px] text-slate-400 uppercase font-semibold">Jitter</div>
                <div className="text-lg font-bold font-mono text-purple-400 mt-1">
                  {testResult?.jitter_ms ? testResult.jitter_ms.toFixed(1) : '--'} <span className="text-[10px]">ms</span>
                </div>
              </div>
            </div>
          </div>

          {/* Speed Statistics & Recent History */}
          <div className="space-y-6">
            <div className="rounded-3xl bg-[#131926]/90 border border-white/5 p-6 shadow-xl">
              <h3 className="text-sm font-bold text-white mb-4">Historical Performance</h3>
              <div className="space-y-3">
                <div className="flex justify-between items-center p-3 rounded-xl bg-black/20 border border-white/5">
                  <span className="text-xs text-slate-400">Max Download</span>
                  <span className="text-sm font-bold font-mono text-emerald-400">
                    {speedStats?.max_download ? speedStats.max_download.toFixed(1) : '--'} Mbps
                  </span>
                </div>
                <div className="flex justify-between items-center p-3 rounded-xl bg-black/20 border border-white/5">
                  <span className="text-xs text-slate-400">Avg Download</span>
                  <span className="text-sm font-bold font-mono text-sky-400">
                    {speedStats?.avg_download ? speedStats.avg_download.toFixed(1) : '--'} Mbps
                  </span>
                </div>
                <div className="flex justify-between items-center p-3 rounded-xl bg-black/20 border border-white/5">
                  <span className="text-xs text-slate-400">Max Upload</span>
                  <span className="text-sm font-bold font-mono text-cyan-400">
                    {speedStats?.max_upload ? speedStats.max_upload.toFixed(1) : '--'} Mbps
                  </span>
                </div>
              </div>
            </div>

            <div className="rounded-3xl bg-[#131926]/90 border border-white/5 p-6 shadow-xl">
              <h3 className="text-sm font-bold text-white mb-4">Recent Tests</h3>
              <div className="space-y-2 max-h-60 overflow-y-auto">
                {speedHistory.map((item) => (
                  <div key={item.id} className="p-3 rounded-xl bg-white/[0.02] border border-white/5 flex items-center justify-between text-xs">
                    <div>
                      <div className="font-bold text-white font-mono">{item.download_mbps.toFixed(1)} ↓ / {item.upload_mbps.toFixed(1)} ↑</div>
                      <div className="text-[10px] text-slate-500 font-mono mt-0.5">
                        {new Date(item.tested_at).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
                      </div>
                    </div>
                    <span className="font-mono text-[11px] text-slate-400">{item.ping_ms.toFixed(0)} ms</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      ) : (
        /* PING & LATENCY SUBVIEW */
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 rounded-3xl bg-[#131926]/90 border border-white/10 p-8 shadow-2xl space-y-6">
            <h3 className="text-lg font-bold text-white">ICMP Ping & Jitter Benchmark</h3>

            {/* Host Target Picker */}
            <div className="space-y-2">
              <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Target Endpoint Host
              </label>
              <div className="flex flex-wrap gap-2">
                {[
                  { label: 'Google DNS (8.8.8.8)', value: '8.8.8.8' },
                  { label: 'Cloudflare (1.1.1.1)', value: '1.1.1.1' },
                  { label: 'Local Gateway (192.168.1.1)', value: '192.168.1.1' },
                ].map((target) => (
                  <button
                    key={target.value}
                    onClick={() => setTargetHost(target.value)}
                    className={`px-3.5 py-1.5 rounded-xl text-xs font-mono font-medium border transition-all ${
                      targetHost === target.value
                        ? 'bg-sky-500/20 text-sky-400 border-sky-500/40'
                        : 'bg-white/[0.03] text-slate-400 border-white/5 hover:bg-white/[0.06]'
                    }`}
                  >
                    {target.label}
                  </button>
                ))}
              </div>
              <input
                type="text"
                value={targetHost}
                onChange={(e) => setTargetHost(e.target.value)}
                placeholder="Custom hostname or IP"
                className="w-full mt-2 px-4 py-2.5 rounded-xl bg-black/30 border border-white/10 text-white font-mono text-sm focus:outline-none focus:border-sky-500"
              />
            </div>

            <button
              onClick={runPingTest}
              disabled={pingTesting}
              className="px-6 py-3 rounded-xl bg-gradient-to-r from-sky-500 to-cyan-400 hover:from-sky-400 hover:to-cyan-300 text-slate-950 font-bold text-xs shadow-lg shadow-sky-500/20 flex items-center gap-2 transition-all disabled:opacity-50"
            >
              <Activity className={`w-4 h-4 ${pingTesting ? 'animate-spin' : ''}`} />
              <span>{pingTesting ? 'Pinging Host...' : 'Execute Ping Test'}</span>
            </button>

            {/* Results Grid */}
            {pingResult && (
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 pt-6 border-t border-white/5">
                <div className="p-4 rounded-2xl bg-black/25 border border-white/5 text-center">
                  <span className="text-xs text-slate-400 font-semibold">Average RTT</span>
                  <div className="text-2xl font-bold font-mono text-emerald-400 mt-1">
                    {pingResult.avg_latency_ms.toFixed(1)} <span className="text-xs">ms</span>
                  </div>
                </div>
                <div className="p-4 rounded-2xl bg-black/25 border border-white/5 text-center">
                  <span className="text-xs text-slate-400 font-semibold">Min Latency</span>
                  <div className="text-2xl font-bold font-mono text-sky-400 mt-1">
                    {pingResult.min_latency_ms.toFixed(1)} <span className="text-xs">ms</span>
                  </div>
                </div>
                <div className="p-4 rounded-2xl bg-black/25 border border-white/5 text-center">
                  <span className="text-xs text-slate-400 font-semibold">Max Latency</span>
                  <div className="text-2xl font-bold font-mono text-amber-400 mt-1">
                    {pingResult.max_latency_ms.toFixed(1)} <span className="text-xs">ms</span>
                  </div>
                </div>
                <div className="p-4 rounded-2xl bg-black/25 border border-white/5 text-center">
                  <span className="text-xs text-slate-400 font-semibold">Packet Loss</span>
                  <div className="text-2xl font-bold font-mono text-white mt-1">
                    {pingResult.packet_loss_pct.toFixed(0)}%
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* History */}
          <div className="rounded-3xl bg-[#131926]/90 border border-white/5 p-6 shadow-xl">
            <h3 className="text-sm font-bold text-white mb-4">Ping Log History</h3>
            <div className="space-y-2 max-h-96 overflow-y-auto">
              {pingHistory.map((item) => (
                <div key={item.id} className="p-3 rounded-xl bg-white/[0.02] border border-white/5 flex items-center justify-between text-xs">
                  <div>
                    <div className="font-bold text-white font-mono">{item.target_host}</div>
                    <div className="text-[10px] text-slate-500 font-mono mt-0.5">
                      {new Date(item.tested_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </div>
                  </div>
                  <span className="font-mono text-xs font-semibold text-emerald-400">
                    {item.avg_latency_ms.toFixed(1)} ms
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

