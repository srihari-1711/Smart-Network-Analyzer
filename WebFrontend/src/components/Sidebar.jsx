import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Wifi, 
  Gauge, 
  MapPin, 
  BarChart3, 
  ShieldCheck, 
  Sparkles, 
  FileText, 
  UserCircle, 
  LogOut,
  Radio
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Sidebar() {
  const { user, logout } = useAuth();

  const navGroups = [
    {
      title: 'TELEMETRY & SCANS',
      items: [
        { name: 'Dashboard', path: '/', icon: LayoutDashboard },
        { name: 'Wi-Fi Scanner', path: '/wifi', icon: Wifi },
        { name: 'Speed & Latency', path: '/tests', icon: Gauge },
        { name: 'Signal Maps', path: '/maps', icon: MapPin },
      ]
    },
    {
      title: 'DIAGNOSTICS & TOOLS',
      items: [
        { name: 'Data Usage', path: '/data-usage', icon: BarChart3 },
        { name: 'Security Audit', path: '/security', icon: ShieldCheck },
        { name: 'AI Diagnostics', path: '/analysis', icon: Sparkles, badge: 'Groq' },
        { name: 'Reports & Export', path: '/reports', icon: FileText },
      ]
    },
    {
      title: 'ACCOUNT',
      items: [
        { name: 'Profile & Settings', path: '/profile', icon: UserCircle },
      ]
    }
  ];

  return (
    <aside className="w-64 flex-shrink-0 bg-[#0B0F17]/90 border-r border-white/5 flex flex-col h-screen select-none">
      {/* Brand Header */}
      <div className="h-16 flex items-center px-6 border-b border-white/5 gap-3">
        <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-sky-500 to-cyan-400 flex items-center justify-center shadow-lg shadow-sky-500/20">
          <Radio className="w-5 h-5 text-slate-950 font-bold" />
        </div>
        <div>
          <h1 className="text-sm font-bold tracking-tight text-white flex items-center gap-1.5">
            Smart Network
          </h1>
          <span className="text-[10px] text-sky-400 font-mono tracking-widest uppercase">Analyzer Web</span>
        </div>
      </div>

      {/* Navigation Links */}
      <div className="flex-1 overflow-y-auto px-3 py-4 space-y-6">
        {navGroups.map((group, idx) => (
          <div key={idx} className="space-y-1">
            <div className="px-3 text-[10px] font-semibold text-slate-500 tracking-wider uppercase mb-2">
              {group.title}
            </div>
            {group.items.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  end={item.path === '/'}
                  className={({ isActive }) =>
                    `flex items-center justify-between px-3.5 py-2.5 rounded-xl text-xs font-medium transition-all duration-200 group ${
                      isActive
                        ? 'bg-sky-500/15 text-sky-400 shadow-sm border border-sky-500/20'
                        : 'text-slate-400 hover:text-slate-200 hover:bg-white/[0.04]'
                    }`
                  }
                >
                  <div className="flex items-center gap-3">
                    <Icon className="w-4 h-4 transition-transform group-hover:scale-110" />
                    <span>{item.name}</span>
                  </div>
                  {item.badge && (
                    <span className="text-[10px] font-mono px-1.5 py-0.5 rounded-md bg-purple-500/20 text-purple-300 border border-purple-500/30">
                      {item.badge}
                    </span>
                  )}
                </NavLink>
              );
            })}
          </div>
        ))}
      </div>

      {/* User Footer Profile */}
      <div className="p-3 border-t border-white/5 bg-black/20">
        <div className="flex items-center justify-between p-2 rounded-xl bg-white/[0.03] border border-white/5">
          <div className="flex items-center gap-2.5 min-w-0">
            <div className="w-8 h-8 rounded-lg bg-sky-500/20 border border-sky-500/30 text-sky-400 flex items-center justify-center font-bold text-xs uppercase flex-shrink-0">
              {user?.name ? user.name.slice(0, 2) : 'NA'}
            </div>
            <div className="min-w-0">
              <p className="text-xs font-semibold text-white truncate">{user?.name || 'User'}</p>
              <p className="text-[10px] text-slate-400 truncate">{user?.email || ''}</p>
            </div>
          </div>
          <button
            onClick={logout}
            title="Sign Out"
            className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </aside>
  );
}
