import React from 'react';
import { BotMessageSquare, Sparkles } from 'lucide-react';

export default function CoachPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
          <BotMessageSquare className="w-6 h-6 text-blue-400" />
          AI Financial Coach
          <span className="inline-flex items-center gap-1 text-xs px-2.5 py-0.5 rounded-full bg-blue-500/20 text-blue-300 border border-blue-500/30 font-medium">
            <Sparkles className="w-3 h-3" /> Living Money AI
          </span>
        </h1>
        <p className="text-slate-400 text-sm mt-1">Grounded financial insights, what-if simulations, and personalized financial coaching.</p>
      </div>
      <div className="p-6 rounded-xl bg-surface-card border border-surface-border text-slate-300">
        AI Coach module initialized with fallback and safety guardrails.
      </div>
    </div>
  );
}

