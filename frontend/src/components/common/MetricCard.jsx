import { ArrowDownRight, ArrowUpRight, Minus } from 'lucide-react';
import { Card } from './UiStates';

export default function MetricCard({ icon: Icon, label, value, helper, trend, tone = 'blue' }) {
  const tones = { blue: 'bg-blue-500/10 text-blue-400', emerald: 'bg-emerald-500/10 text-emerald-400', amber: 'bg-amber-500/10 text-amber-400', rose: 'bg-rose-500/10 text-rose-400' };
  const trendPositive = Number(trend) >= 0;
  return <Card className="p-5"><div className="flex items-start justify-between gap-4"><div className={`flex h-10 w-10 items-center justify-center rounded-xl ${tones[tone] || tones.blue}`}>{Icon && <Icon className="h-5 w-5" />}</div>{trend !== undefined && <span className={`inline-flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-semibold ${trendPositive ? 'bg-emerald-500/10 text-emerald-400' : 'bg-rose-500/10 text-rose-400'}`}>{trendPositive ? <ArrowUpRight className="h-3.5 w-3.5" /> : <ArrowDownRight className="h-3.5 w-3.5" />}{Math.abs(Number(trend))}%</span>}</div><p className="mt-5 text-sm text-slate-500">{label}</p><p className="mt-1 font-mono-numbers text-2xl font-semibold tracking-tight text-white">{value}</p>{helper && <p className="mt-2 flex items-center gap-1 text-xs text-slate-500">{trend === undefined && <Minus className="h-3 w-3" />}{helper}</p>}</Card>;
}

export function ProgressBar({ value, tone = 'blue' }) {
  const colors = { blue: 'bg-blue-500', emerald: 'bg-emerald-400', amber: 'bg-amber-400', rose: 'bg-rose-400' };
  return <div className="h-2 overflow-hidden rounded-full bg-slate-800"><div className={`h-full rounded-full transition-all ${colors[tone] || colors.blue}`} style={{ width: `${Math.min(100, Math.max(0, Number(value) || 0))}%` }} /></div>;
}
