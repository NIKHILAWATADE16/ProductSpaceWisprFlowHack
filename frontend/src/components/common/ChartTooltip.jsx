export default function ChartTooltip({ active, payload, label, formatter }) {
  if (!active || !payload?.length) return null;
  return <div className="rounded-xl border border-surface-border bg-[#111827] px-3 py-2.5 shadow-xl"><p className="mb-1 text-xs text-slate-500">{label}</p>{payload.map((entry) => <p key={entry.dataKey || entry.name} className="text-sm font-semibold text-slate-100">{entry.name}: {formatter ? formatter(entry.value) : entry.value}</p>)}</div>;
}
