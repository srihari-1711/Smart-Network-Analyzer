import React, { useState, useEffect } from 'react';
import { 
  Wifi, 
  RotateCw, 
  History, 
  ShieldCheck, 
  Radio, 
  SlidersHorizontal,
  CheckCircle2,
  Lock,
  Unlock,
  Layers
} from 'lucide-react';
import api from '../api/client';

export default function WifiPage() {
  const [scans, setScans] = useState([]);
  const [selectedScan, setSelectedScan] = useState(null);
  const [filter, setFilter] = useState('ALL'); // 'ALL', '2.4', '5'
  const [loading, setLoading] = useState(true);
  const [scanning, setScanning] = useState(false);
  const [historyOpen, setHistoryOpen] = useState(false);

  const fetchScans = async () => {
    try {
      const res = await api.get('/wifi/scans?page=1&page_size=20');
      const items = res.data?.items || [];
      setScans(items);
      if (items.length > 0 && !selectedScan) {
        // Fetch detailed scan with its networks
        const detailed = await api.get(`/wifi/scans/${items[0].id}`);
        setSelectedScan(detailed.data);
      }
    } catch (err) {
      console.error('Failed to load Wi-Fi scans:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchScans();
  }, []);

  const handleSelectScan = async (scanId) => {
    try {
      const res = await api.get(`/wifi/scans/${scanId}`);
      setSelectedScan(res.data);
      setHistoryOpen(false);
    } catch (err) {
      console.error(err);
    }
  };

  const handleRunScan = async () => {
    setScanning(true);
    try {
      // Simulate/measure AP environments for Web
      const mockNetworks = [
        { ssid: 'Home_HighSpeed_5G', bssid: 'c4:ea:1d:8a:32:01', frequency_mhz: 5180, channel: 36, signal_dbm: -48, security_type: 'WPA3', is_connected: true },
        { ssid: 'Home_HighSpeed_2.4G', bssid: 'c4:ea:1d:8a:32:02', frequency_mhz: 2412, channel: 1, signal_dbm: -52, security_type: 'WPA2', is_connected: false },
        { ssid: 'Neighbor_Network_Ext', bssid: '00:11:22:33:44:55', frequency_mhz: 2437, channel: 6, signal_dbm: -74, security_type: 'WPA2', is_connected: false },
        { ssid: 'Smart_Office_IoT', bssid: '70:88:6b:12:90:ac', frequency_mhz: 2462, channel: 11, signal_dbm: -68, security_type: 'WPA2', is_connected: false },
        { ssid: 'Guest_Public_Open', bssid: 'a2:f4:33:88:99:bb', frequency_mhz: 5240, channel: 48, signal_dbm: -82, security_type: 'OPEN', is_connected: false },
      ];

      const res = await api.post('/wifi/scans', {
        location_label: 'Web Diagnostic Station',
        networks: mockNetworks,
      });

      setSelectedScan(res.data);
      fetchScans();
    } catch (err) {
      console.error('Failed to save scan:', err);
    } finally {
      setScanning(false);
    }
  };

  const rawNetworks = selectedScan?.networks || [];
  const filteredNetworks = rawNetworks.filter((net) => {
    if (filter === '2.4') return net.frequency_mhz < 3000;
    if (filter === '5') return net.frequency_mhz >= 3000;
    return true;
  });

  const getSignalColor = (dbm) => {
    if (dbm >= -60) return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
    if (dbm >= -75) return 'text-amber-400 bg-amber-500/10 border-amber-500/20';
    return 'text-rose-400 bg-rose-500/10 border-rose-500/20';
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
            <span>Wi-Fi Spectrum & Scanner</span>
          </h2>
          <p className="text-sm text-slate-400 mt-1">
            Detect neighboring BSSIDs, analyze RF channel overlapping, and inspect signal strength
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setHistoryOpen(true)}
            className="flex items-center gap-2 px-3.5 py-2 rounded-xl bg-white/[0.04] hover:bg-white/[0.08] text-slate-300 text-xs font-medium border border-white/5 transition-all"
          >
            <History className="w-3.5 h-3.5" />
            <span>Scan History ({scans.length})</span>
          </button>

          <button
            onClick={handleRunScan}
            disabled={scanning}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-gradient-to-r from-sky-500 to-cyan-400 hover:from-sky-400 hover:to-cyan-300 text-slate-950 text-xs font-bold shadow-lg shadow-sky-500/20 transition-all disabled:opacity-50"
          >
            <RotateCw className={`w-3.5 h-3.5 ${scanning ? 'animate-spin' : ''}`} />
            <span>{scanning ? 'Scanning Spectrum...' : 'Scan Now'}</span>
          </button>
        </div>
      </div>

      {/* Channel Distribution Card */}
      <div className="rounded-3xl bg-[#131926]/90 border border-white/5 p-6 backdrop-blur-xl">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-2">
            <Layers className="w-4 h-4 text-sky-400" />
            <h3 className="text-sm font-bold text-white">Channel Crowding Distribution</h3>
          </div>
          <span className="text-xs text-slate-400 font-mono">
            {selectedScan ? `${filteredNetworks.length} APs Recorded` : 'No Data'}
          </span>
        </div>

        {/* Channel Bars */}
        <div className="h-44 flex items-end gap-2 px-4 py-2 bg-black/25 rounded-2xl border border-white/5 overflow-x-auto">
          {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 149, 153, 157, 161].map((ch) => {
            const count = filteredNetworks.filter((n) => n.channel === ch).length;
            const heightPct = count > 0 ? Math.min(100, count * 35) : 4;
            return (
              <div key={ch} className="flex-1 min-w-[28px] flex flex-col items-center gap-1 group">
                <span className="text-[10px] text-slate-400 font-mono opacity-0 group-hover:opacity-100 transition-opacity">
                  {count}
                </span>
                <div
                  style={{ height: `${heightPct}%` }}
                  className={`w-full rounded-t-md transition-all duration-500 ${
                    count > 2
                      ? 'bg-rose-500 shadow-md shadow-rose-500/30'
                      : count > 0
                      ? 'bg-sky-400 shadow-md shadow-sky-400/30'
                      : 'bg-white/5'
                  }`}
                />
                <span className="text-[10px] text-slate-400 font-mono mt-1">
                  {ch}
                </span>
              </div>
            );
          })}
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 p-1 rounded-xl bg-white/[0.03] border border-white/5">
          <button
            onClick={() => setFilter('ALL')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              filter === 'ALL' ? 'bg-sky-500 text-slate-950 shadow-sm' : 'text-slate-400 hover:text-white'
            }`}
          >
            All Bands
          </button>
          <button
            onClick={() => setFilter('2.4')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              filter === '2.4' ? 'bg-sky-500 text-slate-950 shadow-sm' : 'text-slate-400 hover:text-white'
            }`}
          >
            2.4 GHz
          </button>
          <button
            onClick={() => setFilter('5')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              filter === '5' ? 'bg-sky-500 text-slate-950 shadow-sm' : 'text-slate-400 hover:text-white'
            }`}
          >
            5 GHz
          </button>
        </div>

        <span className="text-xs text-slate-400 font-mono">
          Showing {filteredNetworks.length} discovered networks
        </span>
      </div>

      {/* Discovered Networks Table */}
      <div className="rounded-3xl bg-[#131926]/90 border border-white/5 overflow-hidden shadow-2xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-white/[0.02] border-b border-white/5 text-slate-400 uppercase tracking-wider font-semibold">
              <tr>
                <th className="py-3.5 px-6">Network SSID</th>
                <th className="py-3.5 px-6">BSSID MAC</th>
                <th className="py-3.5 px-6">Channel & Band</th>
                <th className="py-3.5 px-6">Signal Strength</th>
                <th className="py-3.5 px-6">Security</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5 text-slate-200">
              {filteredNetworks.length === 0 ? (
                <tr>
                  <td colSpan="5" className="py-12 text-center text-slate-500">
                    No networks found. Click "Scan Now" to record neighboring APs.
                  </td>
                </tr>
              ) : (
                filteredNetworks.map((net, idx) => {
                  const isConn = net.is_connected;
                  const signalStyle = getSignalColor(net.signal_dbm);
                  return (
                    <tr key={idx} className="hover:bg-white/[0.02] transition-colors">
                      <td className="py-4 px-6 font-semibold flex items-center gap-2.5">
                        <Wifi className="w-4 h-4 text-sky-400" />
                        <span>{net.ssid}</span>
                        {isConn && (
                          <span className="px-2 py-0.5 rounded-md bg-emerald-500/20 text-emerald-400 text-[10px] font-bold border border-emerald-500/30">
                            CONNECTED
                          </span>
                        )}
                      </td>
                      <td className="py-4 px-6 font-mono text-slate-400">{net.bssid}</td>
                      <td className="py-4 px-6">
                        <span className="font-semibold text-white">Ch {net.channel}</span>{' '}
                        <span className="text-slate-400 text-[11px]">({net.frequency_mhz} MHz)</span>
                      </td>
                      <td className="py-4 px-6">
                        <span className={`px-2.5 py-1 rounded-full font-mono font-bold border text-[11px] ${signalStyle}`}>
                          {net.signal_dbm} dBm
                        </span>
                      </td>
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-1.5">
                          {net.security_type === 'OPEN' ? (
                            <Unlock className="w-3.5 h-3.5 text-rose-400" />
                          ) : (
                            <Lock className="w-3.5 h-3.5 text-sky-400" />
                          )}
                          <span className="font-semibold">{net.security_type}</span>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* History Drawer Modal */}
      {historyOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex justify-end">
          <div className="w-full max-w-md bg-[#0D121D] border-l border-white/10 h-full p-6 flex flex-col space-y-4">
            <div className="flex items-center justify-between pb-4 border-b border-white/5">
              <h3 className="text-lg font-bold text-white flex items-center gap-2">
                <History className="w-5 h-5 text-sky-400" />
                <span>Wi-Fi Scan History</span>
              </h3>
              <button
                onClick={() => setHistoryOpen(false)}
                className="text-slate-400 hover:text-white text-sm"
              >
                Close
              </button>
            </div>

            <div className="flex-1 overflow-y-auto space-y-2">
              {scans.map((item) => (
                <div
                  key={item.id}
                  onClick={() => handleSelectScan(item.id)}
                  className={`p-4 rounded-2xl border transition-all cursor-pointer ${
                    selectedScan?.id === item.id
                      ? 'bg-sky-500/15 border-sky-500/30'
                      : 'bg-white/[0.03] border-white/5 hover:bg-white/[0.06]'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-semibold text-white text-xs">{item.location_label || 'Default Location'}</span>
                    <span className="text-[10px] text-slate-500 font-mono">ID #{item.id}</span>
                  </div>
                  <div className="mt-2 flex items-center justify-between text-xs text-slate-400">
                    <span>{item.network_count ?? item.networks?.length ?? 0} networks detected</span>
                    <span className="text-[11px] font-mono text-slate-500">
                      {new Date(item.scanned_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
