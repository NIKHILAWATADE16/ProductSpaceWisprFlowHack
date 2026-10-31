import { CheckCircle2, X } from 'lucide-react';

export default function Toast({ message, onClose }) {
  if (!message) return null;
  return <div className="fixed bottom-5 right-5 z-[60] flex max-w-sm items-center gap-3 rounded-xl border border-emerald-500/20 bg-[#10251f] px-4 py-3 text-sm text-emerald-200 shadow-xl"><CheckCircle2 className="h-4 w-4 text-emerald-400" />{message}<button onClick={onClose} className="ml-2 text-emerald-400/70 hover:text-emerald-200"><X className="h-4 w-4" /></button></div>;
}
