import React from 'react';
import { Repeat } from 'lucide-react';

export default function SubscriptionsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
          <Repeat className="w-6 h-6 text-blue-400" />
          Recurring Payments & Subscriptions
        </h1>
        <p className="text-slate-400 text-sm mt-1">Automatic recurring transaction detection, renewal schedules, and active subs.</p>
      </div>
      <div className="p-6 rounded-xl bg-surface-card border border-surface-border text-slate-300">
        Subscriptions module initialized.
      </div>
    </div>
  );
}

