import React from 'react';
import { ReceiptText } from 'lucide-react';

export default function TransactionsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
          <ReceiptText className="w-6 h-6 text-blue-400" />
          Transactions & Imports
        </h1>
        <p className="text-slate-400 text-sm mt-1">Manage expenses, income, CSV & PDF statement parsing, and smart categorization.</p>
      </div>
      <div className="p-6 rounded-xl bg-surface-card border border-surface-border text-slate-300">
        Transactions module initialized.
      </div>
    </div>
  );
}

