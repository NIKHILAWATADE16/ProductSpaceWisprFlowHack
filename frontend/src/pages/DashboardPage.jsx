import React from 'react';
import { LayoutDashboard } from 'lucide-react';

export default function DashboardPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
            <LayoutDashboard className="w-6 h-6 text-blue-400" />
            Financial Dashboard
          </h1>
          <p className="text-slate-400 text-sm mt-1">Living overview of your net worth, cash flow, and financial health.</p>
        </div>
      </div>
      <div className="p-6 rounded-xl bg-surface-card border border-surface-border text-slate-300">
        Phase 1 Setup verified. Dashboard metrics and visual cards will connect in upcoming phases.
      </div>
    </div>
  );
}

