import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  ReceiptText,
  PieChart,
  Target,
  Repeat,
  BotMessageSquare,
  FileBarChart,
  UserCheck,
  Sparkles
} from 'lucide-react';

const navItems = [
  { name: 'Dashboard', path: '/', icon: LayoutDashboard },
  { name: 'Transactions', path: '/transactions', icon: ReceiptText },
  { name: 'Budgets', path: '/budgets', icon: PieChart },
  { name: 'Financial Goals', path: '/goals', icon: Target },
  { name: 'Subscriptions', path: '/subscriptions', icon: Repeat },
  { name: 'AI Financial Coach', path: '/coach', icon: BotMessageSquare, badge: 'AI' },
  { name: 'Reports', path: '/reports', icon: FileBarChart },
  { name: 'Financial Profile', path: '/profile', icon: UserCheck },
];

export default function Sidebar() {
  return (
    <aside className="w-64 bg-surface border-r border-surface-border flex flex-col shrink-0 min-h-screen">
      {/* Brand Header */}
      <div className="h-16 flex items-center px-6 border-b border-surface-border gap-3">
        <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-500 flex items-center justify-center shadow-lg shadow-blue-500/20">
          <Sparkles className="w-5 h-5 text-white" />
        </div>
        <div>
          <span className="text-base font-bold text-white tracking-tight">FinanceFlow</span>
          <span className="block text-[10px] text-blue-400 font-medium uppercase tracking-wider">Living Money Model</span>
        </div>
      </div>

      {/* Nav List */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-600/15 text-blue-400 border border-blue-500/30'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-surface-card/60'
                }`
              }
            >
              <div className="flex items-center gap-3">
                <Icon className="w-4 h-4" />
                <span>{item.name}</span>
              </div>
              {item.badge && (
                <span className="text-[10px] font-semibold uppercase px-1.5 py-0.5 rounded bg-blue-500/20 text-blue-400 border border-blue-500/30">
                  {item.badge}
                </span>
              )}
            </NavLink>
          );
        })}
      </nav>

      {/* Footer Info */}
      <div className="p-4 border-t border-surface-border text-xs text-slate-500">
        <p className="font-medium text-slate-400">Personal Finance Coach</p>
        <p className="text-[11px] text-slate-500 mt-0.5">MVP v1.0.0 • Connected</p>
      </div>
    </aside>
  );
}

