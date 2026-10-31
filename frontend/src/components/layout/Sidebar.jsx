import { NavLink } from 'react-router-dom';
import { X, LayoutDashboard, ReceiptText, PieChart, Target, Repeat, BotMessageSquare, FileBarChart, UserCheck, Sparkles, LogOut } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

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

export default function Sidebar({ mobileOpen, onClose }) {
  const { user, signOut } = useAuth();
  return <>
    {mobileOpen && <button aria-label="Close navigation" className="fixed inset-0 z-40 bg-slate-950/70 lg:hidden" onClick={onClose} />}
    <aside className={`fixed inset-y-0 left-0 z-50 flex w-64 flex-col bg-surface shadow-2xl transition-transform lg:translate-x-0 lg:shadow-none ${mobileOpen ? 'translate-x-0' : '-translate-x-full'}`}>
      <div className="flex h-16 items-center justify-between border-b border-surface-border px-5">
        <div className="flex items-center gap-3"><div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-500 shadow-lg shadow-blue-500/20"><Sparkles className="h-5 w-5 text-white" /></div><div><span className="block text-base font-bold tracking-tight text-white">FinanceFlow</span><span className="block text-[10px] font-medium uppercase tracking-wider text-blue-400">Living Money Model</span></div></div>
        <button onClick={onClose} className="rounded-lg p-2 text-slate-500 hover:bg-slate-800 hover:text-white lg:hidden"><X className="h-4 w-4" /></button>
      </div>
      <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-5">{navItems.map((item) => { const Icon = item.icon; return <NavLink key={item.path} to={item.path} onClick={onClose} className={({ isActive }) => `flex items-center justify-between rounded-xl border px-3.5 py-2.5 text-sm font-medium transition-colors ${isActive ? 'border-blue-500/30 bg-blue-600/15 text-blue-300' : 'border-transparent text-slate-400 hover:bg-surface-card/70 hover:text-slate-200'}`}><span className="flex items-center gap-3"><Icon className="h-4 w-4" />{item.name}</span>{item.badge && <span className="rounded bg-blue-500/20 px-1.5 py-0.5 text-[10px] font-semibold uppercase text-blue-400">{item.badge}</span>}</NavLink>; })}</nav>
      <div className="border-t border-surface-border p-4"><div className="mb-4 flex items-center gap-3"><div className="flex h-9 w-9 items-center justify-center rounded-full bg-blue-500/15 text-xs font-bold text-blue-300">{user?.name?.slice(0, 2).toUpperCase() || 'FC'}</div><div className="min-w-0"><p className="truncate text-sm font-semibold text-slate-200">{user?.name || 'Finance Coach'}</p><p className="truncate text-xs text-slate-500">{user?.email || 'Personal workspace'}</p></div></div><button onClick={signOut} className="flex w-full items-center gap-2 rounded-lg px-2 py-2 text-xs font-semibold text-slate-500 hover:bg-slate-800 hover:text-rose-300"><LogOut className="h-3.5 w-3.5" />Sign out</button></div>
    </aside>
  </>;
}
