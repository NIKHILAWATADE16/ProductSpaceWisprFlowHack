import React from 'react';
import { Bell, ShieldCheck, Activity } from 'lucide-react';

export default function TopNav({ backendConnected = true }) {
  return (
    <header className="h-16 bg-surface/80 backdrop-blur-md border-b border-surface-border px-6 flex items-center justify-between sticky top-0 z-30">
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2 px-2.5 py-1 rounded-full bg-slate-900 border border-surface-border text-xs">
          <span className={`w-2 h-2 rounded-full ${backendConnected ? 'bg-emerald-400 animate-pulse' : 'bg-rose-500'}`} />
          <span className="text-slate-300 font-mono text-[11px]">
            {backendConnected ? 'API & Database Live' : 'Backend Offline'}
          </span>
        </div>
      </div>

      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-surface-card border border-surface-border text-xs text-slate-300">
          <ShieldCheck className="w-4 h-4 text-blue-400" />
          <span>Local Demo User</span>
        </div>
        <button
          type="button"
          aria-label="System notifications"
          className="p-2 rounded-lg bg-surface-card border border-surface-border text-slate-400 hover:text-white transition-colors"
        >
          <Bell className="w-4 h-4" />
        </button>
      </div>
    </header>
  );
}

