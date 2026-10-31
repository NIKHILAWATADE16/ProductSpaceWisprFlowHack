import { useCallback, useEffect, useMemo, useState } from 'react';
import { Activity, ArrowRight, Banknote, CalendarDays, CreditCard, ShieldCheck, Sparkles, Target, TrendingDown, TrendingUp, Wallet } from 'lucide-react';
import { Area, AreaChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import axiosClient from '../api/axiosClient';
import PageHeader, { PeriodSelect } from '../components/common/PageHeader';
import MetricCard, { ProgressBar } from '../components/common/MetricCard';
import { Button, Card, EmptyState, ErrorState, LoadingState } from '../components/common/UiStates';
import ChartTooltip from '../components/common/ChartTooltip';
import { useAuth } from '../context/AuthContext';
import { formatCurrency, formatPercent, getErrorMessage, monthLabel, clamp } from '../lib/formatters';

const palette = ['#60a5fa', '#34d399', '#fbbf24', '#a78bfa', '#fb7185', '#22d3ee'];

export default function DashboardPage() {
  const { user } = useAuth();
  const now = new Date();
  const [period, setPeriod] = useState({ month: now.getMonth() + 1, year: now.getFullYear() });
  const [summary, setSummary] = useState(null);
  const [spending, setSpending] = useState([]);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setStatus('loading'); setError('');
    try {
      const params = { month: period.month, year: period.year };
      const [summaryResponse, spendingResponse] = await Promise.all([axiosClient.get('/dashboard/summary', { params }), axiosClient.get('/dashboard/spending', { params })]);
      setSummary(summaryResponse.data); setSpending(Array.isArray(spendingResponse.data) ? spendingResponse.data : []); setStatus('ready');
    } catch (err) { setError(getErrorMessage(err, 'We could not load your dashboard right now.')); setStatus('error'); }
  }, [period]);

  useEffect(() => { load(); }, [load]);
  const hasData = summary && (Number(summary.totalIncome) > 0 || Number(summary.totalExpenses) > 0 || spending.length > 0);
  const spendingTotal = useMemo(() => spending.reduce((sum, item) => sum + Number(item.amount || 0), 0), [spending]);
  const profileComplete = summary && Number(summary.monthlyIncome || 0) > 0;
  const savingsRate = summary?.savingsRate ?? 0;

  const periodChange = (value) => { const [year, month] = value.split('-').map(Number); setPeriod({ year, month }); };
  return <div className="space-y-7">
    <PageHeader eyebrow={`Good morning, ${user?.name?.split(' ')[0] || 'there'}`} title="Your money, understood." description={`${monthLabel(period.month, period.year)} at a glance — a living view of cash flow, progress, and next steps.`} action={<PeriodSelect month={period.month} year={period.year} onChange={periodChange} />} />
    {status === 'error' && <ErrorState message={error} onRetry={load} />}
    {status === 'loading' && !summary ? <LoadingState label="Building your financial picture…" /> : <>
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <MetricCard icon={TrendingUp} label="Income" value={formatCurrency(summary?.totalIncome)} helper="This month" tone="emerald" />
        <MetricCard icon={TrendingDown} label="Expenses" value={formatCurrency(summary?.totalExpenses)} helper="This month" tone="rose" />
        <MetricCard icon={Wallet} label="Net savings" value={formatCurrency(summary?.netSavings)} helper={Number(summary?.netSavings) >= 0 ? 'Positive cash flow' : 'Above income'} tone="blue" />
        <MetricCard icon={Activity} label="Savings rate" value={formatPercent(savingsRate)} helper="Income retained" tone="amber" />
      </div>
      <div className="grid gap-6 xl:grid-cols-[1.35fr_0.65fr]">
        <Card className="p-5 sm:p-6"><div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-start"><div><h2 className="font-semibold text-white">Spending overview</h2><p className="mt-1 text-sm text-slate-500">Category mix for {monthLabel(period.month, period.year)}.</p></div><div className="flex items-center gap-2 text-xs text-slate-500"><span className="h-2 w-2 rounded-full bg-blue-400" /> Expenses</div></div>{spending.length ? <div className="mt-7 h-64"><ResponsiveContainer width="100%" height="100%"><AreaChart data={spending}><defs><linearGradient id="spendGradient" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#60a5fa" stopOpacity={0.34} /><stop offset="100%" stopColor="#60a5fa" stopOpacity={0.02} /></linearGradient></defs><CartesianGrid stroke="#1f293d" strokeDasharray="3 3" vertical={false} /><XAxis dataKey="category" axisLine={false} tickLine={false} tick={{ fill: '#64748b', fontSize: 11 }} interval="preserveStartEnd" /><YAxis axisLine={false} tickLine={false} tick={{ fill: '#64748b', fontSize: 11 }} tickFormatter={(value) => `₹${Math.round(value / 1000)}k`} /><Tooltip content={<ChartTooltip formatter={formatCurrency} />} /><Area type="monotone" dataKey="amount" name="Spent" stroke="#60a5fa" strokeWidth={2} fill="url(#spendGradient)" /></AreaChart></ResponsiveContainer></div> : <EmptyState icon={CreditCard} title="No spending data yet" description="Import a statement or add transactions to see where your money is going." action={<Button variant="secondary" onClick={() => window.location.assign('/transactions')}>Add transactions <ArrowRight className="h-4 w-4" /></Button>} />}</Card>
        <Card className="p-5 sm:p-6"><div className="flex items-center justify-between"><div><h2 className="font-semibold text-white">Category mix</h2><p className="mt-1 text-sm text-slate-500">Where expenses landed.</p></div><span className="font-mono-numbers text-sm text-slate-300">{formatCurrency(spendingTotal)}</span></div>{spending.length ? <><div className="mt-5 h-44"><ResponsiveContainer width="100%" height="100%"><PieChart><Pie data={spending.slice(0, 6)} dataKey="amount" nameKey="category" innerRadius={52} outerRadius={78} paddingAngle={3}><>{spending.slice(0, 6).map((entry, index) => <Cell key={entry.category} fill={palette[index % palette.length]} />)}</></Pie><Tooltip formatter={(value) => formatCurrency(value)} /></PieChart></ResponsiveContainer></div><div className="space-y-3">{spending.slice(0, 4).map((item, index) => <div key={item.category} className="flex items-center justify-between text-sm"><span className="flex items-center gap-2 text-slate-400"><span className="h-2 w-2 rounded-full" style={{ backgroundColor: palette[index % palette.length] }} />{item.category}</span><span className="font-mono-numbers text-slate-200">{formatCurrency(item.amount)}</span></div>)}</div></> : <div className="flex min-h-56 items-center justify-center text-center text-sm text-slate-500">Your category breakdown will appear here.</div>}</Card>
      </div>
      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="p-5"><div className="flex items-center gap-3"><span className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-500/10 text-blue-400"><ShieldCheck className="h-5 w-5" /></span><div><h3 className="font-semibold text-white">Financial baseline</h3><p className="text-xs text-slate-500">Profile health</p></div></div><div className="mt-6 flex items-end justify-between"><span className="font-mono-numbers text-2xl font-semibold text-white">{profileComplete ? 'Ready' : 'Start here'}</span><span className="text-xs text-slate-500">{profileComplete ? 'Income connected' : '0% complete'}</span></div><div className="mt-3"><ProgressBar value={profileComplete ? 100 : 15} /></div><a href="/profile" className="mt-5 inline-flex text-sm font-semibold text-blue-400 hover:text-blue-300">Update profile <ArrowRight className="ml-1 h-4 w-4" /></a></Card>
        <Card className="p-5"><div className="flex items-center gap-3"><span className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-500/10 text-amber-400"><Target className="h-5 w-5" /></span><div><h3 className="font-semibold text-white">Goals in motion</h3><p className="text-xs text-slate-500">Your active plan</p></div></div><p className="mt-6 font-mono-numbers text-2xl font-semibold text-white">{summary?.goalCount || 0}</p><p className="mt-1 text-sm text-slate-500">active financial goals</p><a href="/goals" className="mt-5 inline-flex text-sm font-semibold text-blue-400 hover:text-blue-300">Review goals <ArrowRight className="ml-1 h-4 w-4" /></a></Card>
        <Card className="relative overflow-hidden p-5"><div className="absolute -right-8 -top-8 h-28 w-28 rounded-full bg-blue-500/10 blur-2xl" /><div className="flex items-center gap-3"><span className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-500/10 text-indigo-300"><Sparkles className="h-5 w-5" /></span><div><h3 className="font-semibold text-white">Coach note</h3><p className="text-xs text-slate-500">Grounded in your numbers</p></div></div><p className="mt-6 text-sm leading-6 text-slate-400">{hasData ? `You retained ${formatPercent(savingsRate)} of recorded income this month. Ask the coach what to focus on next.` : 'Connect your profile and transactions to get your first personalized insight.'}</p><a href="/coach" className="mt-4 inline-flex text-sm font-semibold text-blue-400 hover:text-blue-300">Open coach <ArrowRight className="ml-1 h-4 w-4" /></a></Card>
      </div>
    </>}
  </div>;
}
