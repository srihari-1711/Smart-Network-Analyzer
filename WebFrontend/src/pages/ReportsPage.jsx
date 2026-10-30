import React, { useState, useEffect } from 'react';
import { 
  FileText, 
  Download, 
  Plus, 
  Trash2, 
  Calendar, 
  CheckCircle2, 
  ExternalLink,
  Printer
} from 'lucide-react';
import api from '../api/client';

export default function ReportsPage() {
  const [reports, setReports] = useState([]);
  const [selectedReport, setSelectedReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [reportTitle, setReportTitle] = useState('Executive Network Health Audit');
  const [reportNotes, setReportNotes] = useState('Monthly scheduled wireless infrastructure and security report.');

  const fetchReports = async () => {
    try {
      const res = await api.get('/reports?page=1&page_size=20');
      const items = res.data?.items || [];
      setReports(items);
      if (items.length > 0 && !selectedReport) {
        loadReportDetail(items[0].id);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const loadReportDetail = async (id) => {
    try {
      const res = await api.get(`/reports/${id}`);
      setSelectedReport(res.data);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchReports();
  }, []);

  const handleCreateReport = async (e) => {
    e.preventDefault();
    setCreating(true);
    try {
      const res = await api.post('/reports', {
        title: reportTitle,
        notes: reportNotes,
      });
      setModalOpen(false);
      setSelectedReport(res.data);
      fetchReports();
    } catch (err) {
      console.error('Failed to generate report:', err);
    } finally {
      setCreating(false);
    }
  };

  const handleDownloadPdf = async (id) => {
    try {
      const res = await api.get(`/reports/${id}/export`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `SmartNetwork_Audit_Report_${id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      console.error('Failed to export PDF:', err);
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
            <span>Executive Audit Reports & PDF Export</span>
          </h2>
          <p className="text-sm text-slate-400 mt-1">
            Generate and archive compliance audits, throughput benchmarks, and executive summaries
          </p>
        </div>

        <button
          onClick={() => setModalOpen(true)}
          className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-cyan-400 hover:from-sky-400 hover:to-cyan-300 text-slate-950 text-xs font-bold shadow-lg shadow-sky-500/20 transition-all"
        >
          <Plus className="w-4 h-4" />
          <span>Generate New Report</span>
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Reports List */}
        <div className="space-y-3">
          <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Audit Archive</h3>
          <div className="space-y-2">
            {reports.length === 0 ? (
              <div className="p-6 rounded-2xl bg-[#131926]/50 border border-white/5 text-center text-xs text-slate-500">
                No reports compiled yet. Click "Generate New Report" to create an executive summary.
              </div>
            ) : (
              reports.map((r) => (
                <div
                  key={r.id}
                  onClick={() => loadReportDetail(r.id)}
                  className={`p-4 rounded-2xl border transition-all cursor-pointer ${
                    selectedReport?.id === r.id
                      ? 'bg-sky-500/15 border-sky-500/30'
                      : 'bg-[#131926]/80 border-white/5 hover:bg-white/[0.04]'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-white text-xs">{r.title}</span>
                    <span className="text-[10px] text-slate-500 font-mono">ID #{r.id}</span>
                  </div>
                  <div className="mt-2 text-xs text-slate-400 flex items-center justify-between">
                    <span className="font-mono text-emerald-400">Score {r.health_score ?? '--'}/100</span>
                    <span className="text-[11px] font-mono text-slate-500">
                      {new Date(r.created_at).toLocaleDateString()}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Selected Report Preview */}
        <div className="lg:col-span-2">
          {selectedReport ? (
            <div className="rounded-3xl bg-[#131926]/90 border border-white/10 p-8 shadow-2xl space-y-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-white/5">
                <div>
                  <h3 className="text-xl font-bold text-white">{selectedReport.title}</h3>
                  <div className="flex items-center gap-3 mt-1 text-xs text-slate-400 font-mono">
                    <span>Generated: {new Date(selectedReport.created_at).toLocaleString()}</span>
                    <span>•</span>
                    <span className="text-emerald-400 font-bold">Health Score {selectedReport.health_score ?? 85}/100</span>
                  </div>
                </div>

                <button
                  onClick={() => handleDownloadPdf(selectedReport.id)}
                  className="flex items-center gap-2 px-4 py-2 rounded-xl bg-white/[0.05] hover:bg-white/[0.08] text-white text-xs font-semibold border border-white/10 transition-all"
                >
                  <Download className="w-4 h-4 text-sky-400" />
                  <span>Download PDF</span>
                </button>
              </div>

              {/* Notes */}
              <div>
                <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Scope & Notes</h4>
                <p className="text-sm text-slate-300 bg-black/25 p-4 rounded-2xl border border-white/5 leading-relaxed font-sans">
                  {selectedReport.notes || 'No custom notes attached to this report.'}
                </p>
              </div>

              {/* Report Metrics Breakdown */}
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                <div className="p-4 rounded-2xl bg-black/25 border border-white/5">
                  <span className="text-xs text-slate-400">Benchmark Avg</span>
                  <div className="text-xl font-bold font-mono text-white mt-1">78.4 Mbps</div>
                </div>
                <div className="p-4 rounded-2xl bg-black/25 border border-white/5">
                  <span className="text-xs text-slate-400">Mean Latency</span>
                  <div className="text-xl font-bold font-mono text-white mt-1">16.2 ms</div>
                </div>
                <div className="p-4 rounded-2xl bg-black/25 border border-white/5">
                  <span className="text-xs text-slate-400">Coverage Score</span>
                  <div className="text-xl font-bold font-mono text-white mt-1">92%</div>
                </div>
              </div>
            </div>
          ) : (
            <div className="p-12 rounded-3xl bg-[#131926]/40 border border-white/5 text-center text-slate-500">
              Select an audit report from the archive to inspect details and export PDF.
            </div>
          )}
        </div>
      </div>

      {/* Generate Report Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center px-4">
          <div className="w-full max-w-md bg-[#131926] border border-white/10 rounded-3xl p-6 shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-white">Generate Executive Audit Report</h3>
            <form onSubmit={handleCreateReport} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                  Report Title
                </label>
                <input
                  type="text"
                  value={reportTitle}
                  onChange={(e) => setReportTitle(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-black/30 border border-white/10 text-white text-sm focus:outline-none focus:border-sky-500"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                  Executive Notes & Context
                </label>
                <textarea
                  rows="3"
                  value={reportNotes}
                  onChange={(e) => setReportNotes(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-black/30 border border-white/10 text-white text-sm focus:outline-none focus:border-sky-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-white/5">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={creating}
                  className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-cyan-400 text-slate-950 text-xs font-bold shadow-lg shadow-sky-500/20 disabled:opacity-50"
                >
                  {creating ? 'Compiling Report...' : 'Compile Report'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

