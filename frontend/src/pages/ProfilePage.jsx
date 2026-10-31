import React from 'react';
import { UserCheck } from 'lucide-react';

export default function ProfilePage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
          <UserCheck className="w-6 h-6 text-blue-400" />
          Financial Profile & Living Money Model
        </h1>
        <p className="text-slate-400 text-sm mt-1">Configure baseline income, fixed expenses, emergency fund, and savings targets.</p>
      </div>
      <div className="p-6 rounded-xl bg-surface-card border border-surface-border text-slate-300">
        Financial Profile module initialized.
      </div>
    </div>
  );
}

