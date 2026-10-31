import React from 'react';
import { PieChart } from 'lucide-react';

export default function BudgetsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
          <PieChart className="w-6 h-6 text-blue-400" />
          Budget Tracking
        </h1>
        <p className="text-slate-400 text-sm mt-1">Category-wise monthly caps, burn rate analysis, and overrun warnings.</p>
      </div>
      <div className="p-6 rounded-xl bg-surface-card border border-surface-border text-slate-300">
        Budgets module initialized.
      </div>
    </div>
  );
}

