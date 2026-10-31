import { Bell, Menu, ShieldCheck } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function TopNav({ onMenu }) {
  const { user } = useAuth();
  return <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-surface-border bg-surface/85 px-4 backdrop-blur-md sm:px-6 lg:px-8"><div><button onClick={onMenu} className="rounded-lg p-2 text-slate-400 hover:bg-slate-800 hover:text-white lg:hidden"><Menu className="h-5 w-5" /></button></div><div className="flex items-center gap-3"><div className="hidden items-center gap-2 rounded-lg border border-surface-border bg-surface-card px-3 py-1.5 text-xs text-slate-300 sm:flex"><ShieldCheck className="h-4 w-4 text-blue-400" />{user?.name || 'Local Demo User'}</div><button type="button" aria-label="System notifications" className="rounded-lg border border-surface-border bg-surface-card p-2 text-slate-400 hover:text-white"><Bell className="h-4 w-4" /></button></div></header>;
}
