export default function FormField({ label, error, className = '', children, ...props }) {
  return <label className={`block ${className}`} {...props}><span className="mb-2 block text-xs font-semibold uppercase tracking-wider text-slate-500">{label}</span>{children}{error && <span className="mt-1.5 block text-xs text-rose-400">{error}</span>}</label>;
}

export const inputClass = 'w-full rounded-xl border border-surface-border bg-[#0b1320] px-3.5 py-3 text-sm text-slate-200 outline-none transition placeholder:text-slate-600 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/10';
