import React from 'react';
import { FileBarChart } from 'lucide-react';

export default function ReportsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
          <FileBarChart className="w-6 h-6 text-blue-400" />
          Monthly Financial Reports
        </h1>
        <p className="text-slate-400 text-sm mt-1">Comprehensive monthly statements, spending shifts, and historical savings rate.</p>
      </div>
      <div className="p-6 rounded-xl bg-surface-card border border-surface-border text-slate-300">
        Reports module initialized.
      </div>
    </div>
  );
}

