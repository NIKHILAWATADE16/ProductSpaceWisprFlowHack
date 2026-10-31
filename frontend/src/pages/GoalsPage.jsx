import React from 'react';
import { Target } from 'lucide-react';

export default function GoalsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
          <Target className="w-6 h-6 text-blue-400" />
          Financial Goals
        </h1>
        <p className="text-slate-400 text-sm mt-1">Target savings, timelines, required monthly savings, and priority planning.</p>
      </div>
      <div className="p-6 rounded-xl bg-surface-card border border-surface-border text-slate-300">
        Financial Goals module initialized.
      </div>
    </div>
  );
}

