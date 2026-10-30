import React, { useState, useEffect, useRef } from 'react';
import { 
  MapPin, 
  Plus, 
  Trash2, 
  Activity, 
  Layers, 
  ArrowLeft, 
  CheckCircle2, 
  Radio,
  Sliders
} from 'lucide-react';
import api from '../api/client';

export default function SignalMapsPage() {
  const [maps, setMaps] = useState([]);
  const [activeMap, setActiveMap] = useState(null);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [newMapName, setNewMapName] = useState('');
  const [newMapFloor, setNewMapFloor] = useState(1);
  const [submitting, setSubmitting] = useState(false);

  // New point input
  const [selectedSignalDbm, setSelectedSignalDbm] = useState(-55);
  const canvasRef = useRef(null);

  const fetchMaps = async () => {
    try {
      const res = await api.get('/signal-maps?page=1&page_size=20');
      const items = res.data?.items || [];
      setMaps(items);
      if (items.length > 0 && !activeMap) {
        loadMapDetail(items[0].id);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const loadMapDetail = async (id) => {
    try {
      const res = await api.get(`/signal-maps/${id}`);
      setActiveMap(res.data);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchMaps();
  }, []);

  const handleCreateMap = async (e) => {
    e.preventDefault();
    if (!newMapName) return;
    setSubmitting(true);
    try {
      const res = await api.post('/signal-maps', {
        name: newMapName,
        floor_number: Number(newMapFloor),
        width_meters: 25.0,
        height_meters: 15.0,
      });
      setCreateOpen(false);
      setNewMapName('');
      setActiveMap(res.data);
      fetchMaps();
    } catch (err) {
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  // Add measurement point when clicking on the heatmap canvas
  const handleCanvasClick = async (e) => {
    if (!activeMap || !canvasRef.current) return;
    const rect = canvasRef.current.getBoundingClientRect();
    const xPct = Math.min(100, Math.max(0, ((e.clientX - rect.left) / rect.width) * 100));
    const yPct = Math.min(100, Math.max(0, ((e.clientY - rect.top) / rect.height) * 100));

    try {
      const res = await api.post(`/signal-maps/${activeMap.id}/points`, {
        x_coord: +xPct.toFixed(2),
        y_coord: +yPct.toFixed(2),
        signal_dbm: Number(selectedSignalDbm),
        ssid: 'Primary_AP',
        bssid: 'c4:ea:1d:8a:32:01',
      });
      loadMapDetail(activeMap.id);
    } catch (err) {
      console.error('Failed to add signal point:', err);
    }
  };

  const getPointColor = (dbm) => {
    if (dbm >= -55) return '#10B981'; // Strong Green
    if (dbm >= -68) return '#06B6D4'; // Good Cyan
    if (dbm >= -78) return '#F59E0B'; // Fair Amber
    return '#EF4444'; // Weak Coral
  };

  const points = activeMap?.points || [];
  const avgDbm = points.length > 0 ? Math.round(points.reduce((a, b) => a + b.signal_dbm, 0) / points.length) : null;

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
            <span>Signal Strength Maps & Heatmaps</span>
          </h2>
          <p className="text-sm text-slate-400 mt-1">
            2D RSSI Wi-Fi coverage mapping and dead-zone visualization
          </p>
        </div>

        <button
          onClick={() => setCreateOpen(true)}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-gradient-to-r from-sky-500 to-cyan-400 hover:from-sky-400 hover:to-cyan-300 text-slate-950 text-xs font-bold shadow-lg shadow-sky-500/20 transition-all"
        >
          <Plus className="w-4 h-4" />
          <span>New Floor Map</span>
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Left: Map Selector */}
        <div className="space-y-3">
          <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Created Floor Plans</h3>
          <div className="space-y-2">
            {maps.length === 0 ? (
              <div className="p-5 rounded-2xl bg-[#131926]/50 border border-white/5 text-center text-xs text-slate-500">
                No floor maps created yet.
              </div>
            ) : (
              maps.map((m) => (
                <div
                  key={m.id}
                  onClick={() => loadMapDetail(m.id)}
                  className={`p-4 rounded-2xl border transition-all cursor-pointer ${
                    activeMap?.id === m.id
                      ? 'bg-sky-500/15 border-sky-500/30'
                      : 'bg-[#131926]/80 border-white/5 hover:bg-white/[0.04]'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-white text-sm">{m.name}</span>
                    <span className="text-[10px] px-2 py-0.5 rounded-md bg-white/5 text-slate-400 font-mono">
                      Floor {m.floor_number}
                    </span>
                  </div>
                  <div className="mt-2 text-xs text-slate-400 flex items-center gap-2">
                    <MapPin className="w-3.5 h-3.5 text-sky-400" />
                    <span>{m.points?.length ?? m.point_count ?? 0} points sampled</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Right: Interactive Heatmap Canvas */}
        <div className="lg:col-span-3 space-y-4">
          {activeMap ? (
            <div className="rounded-3xl bg-[#131926]/90 border border-white/10 p-6 shadow-2xl space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-white/5">
                <div>
                  <h3 className="text-lg font-bold text-white flex items-center gap-2">
                    <Radio className="w-5 h-5 text-sky-400" />
                    <span>{activeMap.name}</span>
                  </h3>
                  <span className="text-xs text-slate-400 font-mono">
                    Click anywhere on the floor grid to drop a signal measurement
                  </span>
                </div>

                {/* Point Signal Level Slider */}
                <div className="flex items-center gap-3 bg-black/30 px-4 py-2 rounded-2xl border border-white/5">
                  <span className="text-xs text-slate-400">Sample dBm:</span>
                  <input
                    type="range"
                    min="-90"
                    max="-30"
                    value={selectedSignalDbm}
                    onChange={(e) => setSelectedSignalDbm(e.target.value)}
                    className="w-24 accent-sky-400"
                  />
                  <span className="font-mono text-xs font-bold text-sky-400">{selectedSignalDbm} dBm</span>
                </div>
              </div>

              {/* Floor Plan Heatmap Canvas */}
              <div
                ref={canvasRef}
                onClick={handleCanvasClick}
                className="relative w-full h-[450px] rounded-2xl bg-[#090D15] border border-white/10 cursor-crosshair overflow-hidden shadow-inner group"
                style={{
                  backgroundImage: `radial-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px)`,
                  backgroundSize: '24px 24px',
                }}
              >
                {/* Rendered Heat Points */}
                {points.map((pt, idx) => {
                  const color = getPointColor(pt.signal_dbm);
                  return (
                    <div
                      key={idx}
                      style={{
                        left: `${pt.x_coord}%`,
                        top: `${pt.y_coord}%`,
                        transform: 'translate(-50%, -50%)',
                      }}
                      className="absolute group/pt"
                    >
                      {/* Ambient Heat Glow Wave */}
                      <div
                        style={{ backgroundColor: color }}
                        className="w-16 h-16 rounded-full blur-xl opacity-40 animate-pulse pointer-events-none absolute -inset-4"
                      />
                      {/* Center Point Beacon */}
                      <div
                        style={{ backgroundColor: color }}
                        className="w-4 h-4 rounded-full border-2 border-white shadow-lg flex items-center justify-center relative z-10"
                      />
                      {/* Tooltip on hover */}
                      <div className="opacity-0 group-hover/pt:opacity-100 transition-opacity absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-2.5 py-1 rounded-lg bg-black/90 border border-white/10 text-[10px] font-mono whitespace-nowrap text-white z-20 pointer-events-none">
                        {pt.signal_dbm} dBm ({pt.x_coord}%, {pt.y_coord}%)
                      </div>
                    </div>
                  );
                })}

                {points.length === 0 && (
                  <div className="absolute inset-0 flex flex-col items-center justify-center text-slate-500 pointer-events-none">
                    <MapPin className="w-10 h-10 mb-2 opacity-30 text-sky-400" />
                    <p className="text-sm font-semibold">No signal points recorded</p>
                    <p className="text-xs">Click anywhere on the grid to add measurement samples</p>
                  </div>
                )}
              </div>

              {/* Heatmap Legend */}
              <div className="flex items-center justify-between pt-2 text-xs text-slate-400">
                <div className="flex items-center gap-4">
                  <div className="flex items-center gap-1.5">
                    <span className="w-3 h-3 rounded-full bg-emerald-400 inline-block"></span>
                    <span>Strong (&gt; -55 dBm)</span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <span className="w-3 h-3 rounded-full bg-cyan-400 inline-block"></span>
                    <span>Good (-56 to -68 dBm)</span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <span className="w-3 h-3 rounded-full bg-amber-400 inline-block"></span>
                    <span>Fair (-69 to -78 dBm)</span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <span className="w-3 h-3 rounded-full bg-rose-400 inline-block"></span>
                    <span>Weak (&lt; -78 dBm)</span>
                  </div>
                </div>

                {avgDbm !== null && (
                  <span className="font-mono text-white font-semibold">
                    Avg Coverage: <span className="text-sky-400">{avgDbm} dBm</span>
                  </span>
                )}
              </div>
            </div>
          ) : (
            <div className="p-12 rounded-3xl bg-[#131926]/40 border border-white/5 text-center text-slate-500">
              Select or create a floor map to view signal heat distribution.
            </div>
          )}
        </div>
      </div>

      {/* Create Map Modal */}
      {createOpen && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center px-4">
          <div className="w-full max-w-md bg-[#131926] border border-white/10 rounded-3xl p-6 shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-white">Create New Floor Map</h3>
            <form onSubmit={handleCreateMap} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                  Map Name
                </label>
                <input
                  type="text"
                  value={newMapName}
                  onChange={(e) => setNewMapName(e.target.value)}
                  placeholder="Office 2nd Floor / Home Living Room"
                  className="w-full px-4 py-2.5 rounded-xl bg-black/30 border border-white/10 text-white text-sm focus:outline-none focus:border-sky-500"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                  Floor Number
                </label>
                <input
                  type="number"
                  value={newMapFloor}
                  onChange={(e) => setNewMapFloor(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-black/30 border border-white/10 text-white text-sm focus:outline-none focus:border-sky-500"
                  required
                />
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-white/5">
                <button
                  type="button"
                  onClick={() => setCreateOpen(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-cyan-400 text-slate-950 text-xs font-bold shadow-lg shadow-sky-500/20 disabled:opacity-50"
                >
                  {submitting ? 'Creating...' : 'Create Map'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

