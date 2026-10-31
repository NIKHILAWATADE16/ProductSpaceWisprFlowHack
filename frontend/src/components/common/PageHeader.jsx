import { ChevronDown } from 'lucide-react';

export default function PageHeader({ icon: Icon, eyebrow, title, description, action, children }) {
  return (
    <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
      <div>
        {eyebrow && <p className="mb-2 text-xs font-semibold uppercase tracking-[0.18em] text-blue-400">{eyebrow}</p>}
        <h1 className="flex items-center gap-3 text-2xl font-bold tracking-tight text-white sm:text-3xl">
          {Icon && <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-500/10 text-blue-400"><Icon className="h-5 w-5" /></span>}
          {title}
        </h1>
        {description && <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-400">{description}</p>}
      </div>
      {action || children}
    </div>
  );
}

export function PeriodSelect({ month, year, onChange }) {
  const value = `${year}-${String(month).padStart(2, '0')}`;
  return <label className="relative block">
    <span className="sr-only">Select period</span>
    <select value={value} onChange={(event) => onChange(event.target.value)} className="appearance-none rounded-xl border border-surface-border bg-surface-card py-2.5 pl-3 pr-9 text-sm text-slate-300 outline-none transition focus:border-blue-500">
      <option value={value}>{new Intl.DateTimeFormat('en-IN', { month: 'long', year: 'numeric' }).format(new Date(year, month - 1, 1))}</option>
      <option value={`${year}-${String(month - 1 || 12).padStart(2, '0')}`}>Previous month</option>
    </select>
    <ChevronDown className="pointer-events-none absolute right-3 top-3 h-4 w-4 text-slate-500" />
  </label>;
}
