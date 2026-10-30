import React, { useState } from 'react';
import { 
  UserCircle, 
  Mail, 
  Lock, 
  Server, 
  Database, 
  CheckCircle2, 
  AlertCircle, 
  LogOut,
  Smartphone
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import api from '../api/client';

export default function ProfilePage() {
  const { user, logout, updateUser } = useAuth();
  const [name, setName] = useState(user?.name || '');
  const [nameSuccess, setNameSuccess] = useState('');
  const [nameError, setNameError] = useState('');
  const [savingName, setSavingName] = useState(false);

  // Password change state
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [pwdSuccess, setPwdSuccess] = useState('');
  const [pwdError, setPwdError] = useState('');
  const [savingPwd, setSavingPwd] = useState(false);

  const handleUpdateName = async (e) => {
    e.preventDefault();
    setNameError('');
    setNameSuccess('');
    setSavingName(true);
    try {
      const res = await api.put('/users/me', { name });
      updateUser(res.data);
      setNameSuccess('Profile name updated successfully.');
    } catch (err) {
      setNameError(err.response?.data?.message || err.message || 'Failed to update name.');
    } finally {
      setSavingName(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setPwdError('');
    setPwdSuccess('');
    if (!oldPassword || !newPassword) {
      setPwdError('Please enter both current and new password.');
      return;
    }
    setSavingPwd(true);
    try {
      const res = await api.put('/users/me/password', {
        old_password: oldPassword,
        new_password: newPassword,
      });
      setPwdSuccess(res.data?.message || 'Password changed successfully.');
      setOldPassword('');
      setNewPassword('');
    } catch (err) {
      setPwdError(err.response?.data?.message || err.message || 'Failed to change password.');
    } finally {
      setSavingPwd(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto pb-12">
      <div>
        <h2 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
          <span>Profile & System Settings</span>
        </h2>
        <p className="text-sm text-slate-400 mt-1">
          Manage your account credentials and inspect the unified server connection
        </p>
      </div>

      {/* Account Info Header */}
      <div className="rounded-3xl bg-[#131926]/90 border border-white/10 p-6 flex flex-col sm:flex-row items-center gap-6 shadow-2xl">
        <div className="w-20 h-20 rounded-2xl bg-gradient-to-tr from-sky-500 to-cyan-400 text-slate-950 flex items-center justify-center font-black text-3xl shadow-xl shadow-sky-500/25">
          {user?.name ? user.name.slice(0, 2).toUpperCase() : 'NA'}
        </div>
        <div className="text-center sm:text-left flex-1">
          <h3 className="text-xl font-bold text-white">{user?.name || 'User'}</h3>
          <p className="text-xs text-slate-400 font-mono mt-0.5">{user?.email || ''}</p>
          <div className="flex flex-wrap gap-2 mt-3 justify-center sm:justify-start">
            <span className="px-3 py-1 rounded-full bg-sky-500/10 border border-sky-500/20 text-sky-400 text-xs font-mono font-semibold">
              Account ID #{user?.id || 1}
            </span>
            <span className="px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-mono font-semibold flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>Synced with Android App</span>
            </span>
          </div>
        </div>
        <button
          onClick={logout}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/20 text-xs font-semibold transition-all"
        >
          <LogOut className="w-4 h-4" />
          <span>Sign Out</span>
        </button>
      </div>

      {/* Unified Architecture Info Card */}
      <div className="rounded-3xl bg-[#131926]/90 border border-white/5 p-6 shadow-xl space-y-4">
        <h4 className="text-sm font-bold text-white flex items-center gap-2">
          <Database className="w-4 h-4 text-sky-400" />
          <span>Shared Server & Database Infrastructure</span>
        </h4>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-4 rounded-2xl bg-black/25 border border-white/5 space-y-1">
            <span className="text-[10px] uppercase font-semibold text-slate-400 tracking-wider">FastAPI Core Server</span>
            <div className="font-mono text-xs text-sky-400 font-bold">http://localhost:8000/api/v1</div>
            <p className="text-[11px] text-slate-400">Common REST API serving Android app and Web application simultaneously.</p>
          </div>
          <div className="p-4 rounded-2xl bg-black/25 border border-white/5 space-y-1">
            <span className="text-[10px] uppercase font-semibold text-slate-400 tracking-wider">Unified SQLite Database</span>
            <div className="font-mono text-xs text-emerald-400 font-bold">Backend/smart_network.db</div>
            <p className="text-[11px] text-slate-400">All users, speed tests, Wi-Fi scans, and AI diagnostics are stored in one database.</p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Update Name Form */}
        <div className="rounded-3xl bg-[#131926]/90 border border-white/5 p-6 shadow-xl space-y-4">
          <h4 className="text-sm font-bold text-white flex items-center gap-2">
            <UserCircle className="w-4 h-4 text-sky-400" />
            <span>Profile Details</span>
          </h4>

          {nameSuccess && (
            <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-xs">
              {nameSuccess}
            </div>
          )}
          {nameError && (
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs">
              {nameError}
            </div>
          )}

          <form onSubmit={handleUpdateName} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">
                Full Name
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-4 py-2.5 rounded-xl bg-black/30 border border-white/10 text-white text-sm focus:outline-none focus:border-sky-500"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">
                Email Address (Read-only)
              </label>
              <input
                type="text"
                disabled
                value={user?.email || ''}
                className="w-full px-4 py-2.5 rounded-xl bg-white/[0.02] border border-white/5 text-slate-400 text-sm cursor-not-allowed"
              />
            </div>

            <button
              type="submit"
              disabled={savingName}
              className="px-5 py-2.5 rounded-xl bg-sky-500 hover:bg-sky-400 text-slate-950 font-bold text-xs shadow-md transition-all disabled:opacity-50"
            >
              {savingName ? 'Saving...' : 'Update Name'}
            </button>
          </form>
        </div>

        {/* Change Password Form */}
        <div className="rounded-3xl bg-[#131926]/90 border border-white/5 p-6 shadow-xl space-y-4">
          <h4 className="text-sm font-bold text-white flex items-center gap-2">
            <Lock className="w-4 h-4 text-sky-400" />
            <span>Security & Password</span>
          </h4>

          {pwdSuccess && (
            <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-xs">
              {pwdSuccess}
            </div>
          )}
          {pwdError && (
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs">
              {pwdError}
            </div>
          )}

          <form onSubmit={handleChangePassword} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">
                Current Password
              </label>
              <input
                type="password"
                value={oldPassword}
                onChange={(e) => setOldPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full px-4 py-2.5 rounded-xl bg-black/30 border border-white/10 text-white text-sm focus:outline-none focus:border-sky-500"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">
                New Password
              </label>
              <input
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="Min 6 characters"
                className="w-full px-4 py-2.5 rounded-xl bg-black/30 border border-white/10 text-white text-sm focus:outline-none focus:border-sky-500"
                required
              />
            </div>

            <button
              type="submit"
              disabled={savingPwd}
              className="px-5 py-2.5 rounded-xl bg-sky-500 hover:bg-sky-400 text-slate-950 font-bold text-xs shadow-md transition-all disabled:opacity-50"
            >
              {savingPwd ? 'Updating Password...' : 'Change Password'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

