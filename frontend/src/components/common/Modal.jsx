import { X } from 'lucide-react';

export default function Modal({ open, onClose, title, description, children, wide = false }) {
  if (!open) return null;
  return <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/75 p-4 backdrop-blur-sm" onMouseDown={(event) => event.target === event.currentTarget && onClose()}><div className={`max-h-[90vh] w-full overflow-y-auto rounded-2xl border border-surface-border bg-[#111827] shadow-2xl ${wide ? 'max-w-3xl' : 'max-w-lg'}`}><div className="flex items-start justify-between border-b border-surface-border px-6 py-5"><div><h2 className="text-lg font-semibold text-white">{title}</h2>{description && <p className="mt-1 text-sm text-slate-500">{description}</p>}</div><button onClick={onClose} aria-label="Close dialog" className="rounded-lg p-2 text-slate-500 hover:bg-slate-800 hover:text-white"><X className="h-4 w-4" /></button></div><div className="p-6">{children}</div></div></div>;
}
