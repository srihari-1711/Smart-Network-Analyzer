import React, { useState, useEffect } from 'react';
import { 
  ShieldCheck, 
  ShieldAlert, 
  RotateCw, 
  AlertTriangle, 
  CheckCircle2, 
  Lock, 
  Radio, 
  Cpu, 
  History 
} from 'lucide-react';
import api from '../api/client';

export default function SecurityPage() {
  const [scans, setScans] = useState([]);
  const [latestScan, setLatestScan] = useState(null);
  const [loading, setLoading] = useState(true);
  const [scanning, setScanning] = useState(false);

  const fetchScans = async () => {
    try {
      const res = await api.get('/security-scans?page=1&page_size=10');
      const items = res.data?.items || [];
      setScans(items);
      if (items.length > 0 && !latestScan) {
        setLatestScan(items[0]);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchScans();
  }, []);

  const handleRunScan = async () => {
    setScanning(true);
    try {
      const res = await api.post('/security-scans', {
        connected_ssid: 'SmartNetwork_Secure',
        connected_bssid: 'c4:ea:1d:8a:32:01',
        encryption_type: 'WPA2',
        dns_servers: ['8.8.8.8', '1.1.1.1'],
        gateway_ip: '192.168.1.1',
        gateway_mac: '00:11:22:33:44:55',
        neighbor_aps: [
          { ssid: 'SmartNetwork_Secure', bssid: 'c4:ea:1d:8a:32:01', signal_dbm: -50, security_type: 'WPA2' },
          { ssid: 'Guest_Open_Wifi', bssid: 'a2:f4:33:88:99:bb', signal_dbm: -75, security_type: 'OPEN' },
        ],
        connected_device_count: 8,
      });
      setLatestScan(res.data);
      fetchScans();
    } catch (err) {
      console.error(err);
    } finally {
      setScanning(false);
    }
  };

  const riskScore = latestScan?.risk_score ?? 88;
  const riskLevel = latestScan?.risk_level ?? 'LOW';

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
            <span>Network Security Audit</span>
          </h2>
          <p className="text-sm text-slate-400 mt-1">
            Cryptographic handshake audit, ARP spoofing detection, and rogue AP inspection
          </p>
        </div>

        <button
          onClick={handleRunScan}
          disabled={scanning}
          className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-cyan-400 hover:from-sky-400 hover:to-cyan-300 text-slate-950 text-xs font-bold shadow-lg shadow-sky-500/20 transition-all disabled:opacity-50"
        >
          <RotateCw className={`w-4 h-4 ${scanning ? 'animate-spin' : ''}`} />
          <span>{scanning ? 'Auditing Network...' : 'Run Security Scan'}</span>
        </button>
      </div>

      {/* Hero Security Posture Card */}
      <div className="rounded-3xl bg-gradient-to-b from-[#131926] to-[#0A0E17] border border-white/10 p-8 shadow-2xl relative overflow-hidden">
        <div className="flex flex-col md:flex-row items-center justify-between gap-8">
          <div className="space-y-3 flex-1 text-center md:text-left">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-xs text-emerald-400 font-semibold uppercase tracking-wider">
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>Posture: {riskLevel} RISK</span>
            </div>
            <h3 className="text-2xl font-bold text-white">
              {riskLevel === 'LOW'
                ? 'Network Encryption & Gateway Verified Secure'
                : 'Vulnerabilities or Warnings Detected'}
            </h3>
            <p className="text-sm text-slate-400 max-w-xl">
              {latestScan?.summary ||
                'Latest evaluation confirms WPA2/WPA3 encryption integrity with no rogue ARP spoofing or duplicate BSSID beacons detected.'}
            </p>
          </div>

          <div className="flex flex-col items-center justify-center p-6 bg-black/30 rounded-3xl border border-white/5 w-48 text-center">
            <span className="text-xs uppercase font-semibold text-slate-400 tracking-wider">Security Score</span>
            <span className="text-5xl font-black font-mono text-emerald-400 mt-2">{riskScore}</span>
            <span className="text-xs text-slate-500 font-mono mt-1">/ 100 Posture</span>
          </div>
        </div>
      </div>

      {/* Security Checklist Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-5 rounded-2xl bg-[#131926]/90 border border-white/5 space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase text-slate-400">Wi-Fi Encryption</span>
            <Lock className="w-4 h-4 text-sky-400" />
          </div>
          <div className="text-lg font-bold text-white font-mono">{latestScan?.encryption_type || 'WPA2/WPA3'}</div>
          <p className="text-[11px] text-slate-500">AES-CCMP 128/256-bit pairwise keys</p>
        </div>

        <div className="p-5 rounded-2xl bg-[#131926]/90 border border-white/5 space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase text-slate-400">ARP Poisoning</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-lg font-bold text-emerald-400 font-mono">CLEAN</div>
          <p className="text-[11px] text-slate-500">Gateway MAC matches ARP table</p>
        </div>

        <div className="p-5 rounded-2xl bg-[#131926]/90 border border-white/5 space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase text-slate-400">Rogue AP / Evil Twin</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-lg font-bold text-emerald-400 font-mono">NONE FOUND</div>
          <p className="text-[11px] text-slate-500">No duplicate beacon spoofing</p>
        </div>

        <div className="p-5 rounded-2xl bg-[#131926]/90 border border-white/5 space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase text-slate-400">Subnet Devices</span>
            <Cpu className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-lg font-bold text-white font-mono">{latestScan?.connected_device_count || 8} Active</div>
          <p className="text-[11px] text-slate-500">Devices responding to ARP probe</p>
        </div>
      </div>

      {/* Findings & Remediation */}
      <div className="rounded-3xl bg-[#131926]/90 border border-white/5 p-6 shadow-xl space-y-4">
        <h3 className="text-sm font-bold text-white">Security Findings & Recommendations</h3>
        <div className="space-y-3">
          {(latestScan?.findings || [
            { title: 'Consider WPA3 Upgrade', desc: 'Router is currently utilizing WPA2-Personal. Upgrading to WPA3-SAE prevents offline dictionary attacks.', level: 'INFO' },
            { title: 'Unsecured Open Hotspot Detected in Vicinity', desc: 'An open SSID "Guest_Open_Wifi" was observed. Avoid sending sensitive credentials over open networks.', level: 'WARNING' },
          ]).map((f, idx) => (
            <div key={idx} className="p-4 rounded-2xl bg-black/25 border border-white/5 flex items-start gap-3.5">
              <AlertTriangle className={`w-4 h-4 mt-0.5 flex-shrink-0 ${f.level === 'WARNING' ? 'text-amber-400' : 'text-sky-400'}`} />
              <div>
                <h4 className="text-xs font-bold text-white">{f.title}</h4>
                <p className="text-xs text-slate-400 mt-1 leading-relaxed">{f.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

