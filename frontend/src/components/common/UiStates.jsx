import { AlertTriangle, Inbox, Loader2, RefreshCw } from 'lucide-react';

export function LoadingState({ label = 'Loading your data…' }) {
  return <div className="flex min-h-44 items-center justify-center rounded-2xl border border-surface-border bg-surface-card/60 text-sm text-slate-400"><Loader2 className="mr-2 h-4 w-4 animate-spin text-blue-400" />{label}</div>;
}

export function EmptyState({ icon: Icon = Inbox, title, description, action }) {
  return <div className="flex min-h-56 flex-col items-center justify-center rounded-2xl border border-dashed border-surface-border bg-surface-card/40 px-6 text-center"><span className="mb-4 flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-800 text-slate-500"><Icon className="h-5 w-5" /></span><h3 className="text-sm font-semibold text-slate-200">{title}</h3>{description && <p className="mt-2 max-w-md text-sm leading-6 text-slate-500">{description}</p>}{action && <div className="mt-5">{action}</div>}</div>;
}

export function ErrorState({ message, onRetry }) {
  return <div className="flex min-h-44 items-center justify-between gap-4 rounded-2xl border border-rose-500/20 bg-rose-500/5 px-5 py-4 text-sm text-rose-200"><div className="flex items-center gap-3"><AlertTriangle className="h-5 w-5 shrink-0 text-rose-400" /><span>{message}</span></div>{onRetry && <button onClick={onRetry} className="inline-flex shrink-0 items-center gap-2 rounded-lg border border-rose-400/20 px-3 py-2 text-xs font-semibold hover:bg-rose-400/10"><RefreshCw className="h-3.5 w-3.5" />Retry</button>}</div>;
}

export function Button({ children, variant = 'primary', className = '', ...props }) {
  const styles = { primary: 'bg-blue-600 text-white shadow-lg shadow-blue-600/15 hover:bg-blue-500', secondary: 'border border-surface-border bg-surface-card text-slate-200 hover:border-slate-600 hover:bg-slate-800', ghost: 'text-slate-400 hover:bg-slate-800 hover:text-white', danger: 'border border-rose-500/20 bg-rose-500/10 text-rose-300 hover:bg-rose-500/20' };
  return <button className={`inline-flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-50 ${styles[variant]} ${className}`} {...props}>{children}</button>;
}

export function Card({ children, className = '', ...props }) {
  return <section className={`rounded-2xl border border-surface-border bg-surface-card ${className}`} {...props}>{children}</section>;
}
