import { useCallback, useEffect, useMemo, useState } from 'react';
import { BellRing, CalendarClock, CheckCircle2, IndianRupee, PauseCircle, Plus, RefreshCw, Repeat, Search, Sparkles, Trash2 } from 'lucide-react';
import axiosClient from '../api/axiosClient';
import PageHeader from '../components/common/PageHeader';
import MetricCard from '../components/common/MetricCard';
import { Button, Card, EmptyState, ErrorState, LoadingState } from '../components/common/UiStates';
import Modal from '../components/common/Modal';
import FormField, { inputClass } from '../components/common/FormField';
import Toast from '../components/common/Toast';
import { formatCurrency, formatDate, getErrorMessage, localizeCurrencyText } from '../lib/formatters';

const categories = ['Subscriptions', 'Utilities', 'Entertainment', 'Education', 'Other'];
const emptyForm = { name: '', amount: '', billingFrequency: 'MONTHLY', nextPaymentDate: new Date().toISOString().slice(0, 10), category: 'Subscriptions' };

function monthlyAmount(item) {
  const amount = Number(item.amount || 0);
  const frequency = item.billingFrequency || 'MONTHLY';
  return frequency === 'YEARLY' ? amount / 12 : frequency === 'QUARTERLY' ? amount / 3 : frequency === 'WEEKLY' ? amount * 4.33 : amount;
}

export default function SubscriptionsPage() {
  const [subscriptions, setSubscriptions] = useState([]);
  const [detected, setDetected] = useState([]);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');
  const [query, setQuery] = useState('');
  const [open, setOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [actionId, setActionId] = useState(null);
  const [detecting, setDetecting] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [alerts, setAlerts] = useState([]);
  const [toast, setToast] = useState('');
  const [form, setForm] = useState(emptyForm);

  const load = useCallback(async () => {
    setStatus('loading');
    setError('');
    try {
      const { data } = await axiosClient.get('/subscriptions');
      setSubscriptions(Array.isArray(data) ? data : data?.content || []);
      setStatus('ready');
    } catch (err) {
      setError(getErrorMessage(err, 'Subscriptions are not available yet.'));
      setStatus('error');
      return;
    }

    try {
      const [{ data: detectedData }, { data: alertData }] = await Promise.all([
        axiosClient.get('/subscriptions/detected'),
        axiosClient.get('/subscriptions/alerts'),
      ]);
      setDetected(Array.isArray(detectedData) ? detectedData : []);
      setAlerts(Array.isArray(alertData) ? alertData : []);
    } catch {
      // The recurring register remains usable if optional insights are unavailable.
      setDetected([]);
      setAlerts([]);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);
  const detect = async () => {
    setDetecting(true);
    setError('');
    try {
      const { data } = await axiosClient.post('/subscriptions/detect', null, { params: { autoSync: false } });
      setDetected(Array.isArray(data) ? data : []);
      setToast(data?.length ? `${data.length} recurring pattern${data.length === 1 ? '' : 's'} found` : 'No recurring patterns found yet');
    } catch (err) {
      setError(getErrorMessage(err, 'Could not scan transactions for recurring payments.'));
    } finally {
      setDetecting(false);
    }
  };

  const syncDetected = async () => {
    if (!window.confirm('Track all new recurring patterns in your subscription register?')) return;
    setSyncing(true);
    setError('');
    try {
      const { data } = await axiosClient.post('/subscriptions/detect', null, { params: { autoSync: true } });
      setSubscriptions(Array.isArray(data) ? data : data?.content || []);
      await load();
      setToast('New recurring payments added to your register');
    } catch (err) {
      setError(getErrorMessage(err, 'Could not add detected recurring payments.'));
    } finally {
      setSyncing(false);
    }
  };

  const save = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await axiosClient.post('/subscriptions', { ...form, amount: Number(form.amount) });
      setOpen(false);
      setForm(emptyForm);
      setToast('Subscription added');
      await load();
    } catch (err) {
      setError(getErrorMessage(err, 'Could not save this subscription.'));
    } finally {
      setSaving(false);
    }
  };

  const setActive = async (item, active) => {
    setActionId(item.id);
    setError('');
    try {
      await axiosClient.post(`/subscriptions/${item.id}/${active ? 'confirm' : 'dismiss'}`);
      setToast(active ? 'Subscription confirmed' : 'Subscription dismissed');
      await load();
    } catch (err) {
      setError(getErrorMessage(err, 'Could not update this subscription.'));
    } finally {
      setActionId(null);
    }
  };

  const remove = async (item) => {
    if (!window.confirm(`Delete “${item.name || 'this subscription'}” from your register?`)) return;
    setActionId(item.id);
    setError('');
    try {
      await axiosClient.delete(`/subscriptions/${item.id}`);
      setToast('Subscription deleted');
      await load();
    } catch (err) {
      setError(getErrorMessage(err, 'Could not delete this subscription.'));
    } finally {
      setActionId(null);
    }
  };

  const filtered = useMemo(() => subscriptions.filter((item) => `${item.name || ''} ${item.category || ''}`.toLowerCase().includes(query.toLowerCase())), [subscriptions, query]);
  const active = subscriptions.filter((item) => item.active !== false);
  const monthly = active.reduce((sum, item) => sum + monthlyAmount(item), 0);
  const next = active.slice().sort((a, b) => new Date(a.nextPaymentDate) - new Date(b.nextPaymentDate))[0];
  const newDetected = detected.filter((item) => !item.alreadyTracked);

  return <div className="space-y-7">
    <PageHeader
      icon={Repeat}
      eyebrow="Recurring payments"
      title="Subscriptions"
      description="See the payments that repeat, what they cost monthly, and what is coming next."
      action={<Button onClick={() => { setForm(emptyForm); setOpen(true); }}><Plus className="h-4 w-4" />Add subscription</Button>}
    />

    <div className="grid gap-4 sm:grid-cols-3">
      <MetricCard icon={Repeat} label="Active subscriptions" value={active.length} helper="Detected or confirmed" tone="blue" />
      <MetricCard icon={IndianRupee} label="Estimated monthly cost" value={formatCurrency(monthly)} helper="Normalized frequency" tone="amber" />
      <MetricCard icon={CalendarClock} label="Next payment" value={next ? formatDate(next.nextPaymentDate, { year: undefined }) : '—'} helper="Upcoming renewal" tone="emerald" />
    </div>

    {error && <ErrorState message={error} onRetry={load} />}

    {alerts.length > 0 && <Card className="p-5 sm:p-6"><div className="flex items-center gap-2"><BellRing className="h-5 w-5 text-amber-400" /><div><h2 className="font-semibold text-white">Subscription alerts</h2><p className="mt-1 text-sm text-slate-500">Price changes and unused subscriptions worth reviewing.</p></div></div><div className="mt-5 grid gap-3 lg:grid-cols-2">{alerts.map((item) => <div key={item.id} className="rounded-xl border border-amber-500/20 bg-amber-500/5 p-4"><div className="flex items-start justify-between gap-3"><div><p className="font-medium text-slate-200">{item.name}</p><p className="mt-1 text-xs text-slate-500">{item.category || 'Other'}</p></div><span className="rounded-lg bg-amber-500/10 px-2 py-1 text-xs font-semibold text-amber-300">{item.priceChangeAlert ? 'Price change' : 'Unused'}</span></div>{item.priceChangeAlert && <p className="mt-3 text-sm leading-6 text-slate-300">{localizeCurrencyText(item.priceChangeMessage || `New amount: ${formatCurrency(item.newAmount)}`)}</p>}{item.unusedAlert && <p className="mt-3 text-sm leading-6 text-slate-300">{localizeCurrencyText(item.unusedMessage || `No recent activity for ${item.daysInactive} days.`)}</p>}</div>)}</div></Card>}

    <Card className="p-5 sm:p-6">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
        <div className="flex items-start gap-3">
          <Sparkles className="mt-0.5 h-5 w-5 shrink-0 text-blue-400" />
          <div>
            <h2 className="font-semibold text-white">Recurring payment suggestions</h2>
            <p className="mt-1 max-w-2xl text-sm leading-6 text-slate-500">Scan recent transactions for merchant, amount, and timing patterns. Suggestions are reviewable and never change your bank account.</p>
          </div>
        </div>
        <div className="flex shrink-0 flex-wrap gap-2">
          <Button variant="secondary" disabled={detecting} onClick={detect}><RefreshCw className={`h-4 w-4 ${detecting ? 'animate-spin' : ''}`} />{detecting ? 'Scanning…' : 'Scan transactions'}</Button>
          {newDetected.length > 0 && <Button disabled={syncing} onClick={syncDetected}>{syncing ? 'Adding…' : `Track ${newDetected.length} new`}</Button>}
        </div>
      </div>
      {detected.length ? <div className="mt-5 grid gap-3 lg:grid-cols-2">{detected.map((item) => <div key={`${item.name}-${item.amount}`} className="rounded-xl border border-surface-border bg-slate-900/50 p-4"><div className="flex items-start justify-between gap-3"><div><p className="font-medium text-slate-200">{item.name}</p><p className="mt-1 text-xs text-slate-500">{item.category || 'Other'} · {item.occurrenceCount} occurrences · {item.confidence || 'Review'}</p></div><span className={`rounded-lg px-2 py-1 text-xs font-semibold ${item.alreadyTracked ? 'bg-emerald-500/10 text-emerald-400' : 'bg-amber-500/10 text-amber-300'}`}>{item.alreadyTracked ? 'Tracked' : 'New suggestion'}</span></div><div className="mt-4 flex flex-wrap items-end justify-between gap-3"><div><p className="font-mono-numbers text-lg font-semibold text-white">{formatCurrency(item.amount)}</p><p className="mt-1 text-xs text-slate-500">{item.billingFrequency || 'MONTHLY'} · next {formatDate(item.nextPaymentDate)}</p></div>{!item.alreadyTracked && <span className="text-xs text-slate-500">Use “Track new” to review and add</span>}</div></div>)}</div> : <p className="mt-5 rounded-xl bg-slate-900/50 p-4 text-sm text-slate-500">No recurring patterns found yet. Add a few dated transactions from the same merchant, then scan again.</p>}
    </Card>

    {status === 'loading' ? <LoadingState label="Looking for recurring payments…" /> : status === 'ready' && !subscriptions.length ? <EmptyState icon={Repeat} title="No recurring payments yet" description="Add a subscription manually or scan transactions for recurring payment suggestions." action={<Button onClick={() => setOpen(true)}><Plus className="h-4 w-4" />Add subscription</Button>} /> : <Card className="overflow-hidden">
      <div className="flex flex-col justify-between gap-3 border-b border-surface-border p-5 sm:flex-row sm:items-center"><div><h2 className="font-semibold text-white">Recurring payment register</h2><p className="mt-1 text-sm text-slate-500">Confirm what should stay in your monthly picture.</p></div><label className="relative"><Search className="absolute left-3 top-3 h-4 w-4 text-slate-600" /><input value={query} onChange={(event) => setQuery(event.target.value)} className="w-full rounded-xl border border-surface-border bg-slate-900/60 py-2.5 pl-10 pr-3 text-sm text-slate-200 outline-none focus:border-blue-500 sm:w-64" placeholder="Search subscriptions" /></label></div>
      <div className="divide-y divide-surface-border">{filtered.map((item) => <div key={item.id || item.name} className="flex flex-col gap-4 p-5 transition hover:bg-slate-800/25 sm:flex-row sm:items-center sm:justify-between"><div className="flex items-center gap-3"><span className={`flex h-10 w-10 items-center justify-center rounded-xl ${item.active === false ? 'bg-slate-800 text-slate-500' : 'bg-blue-500/10 text-blue-400'}`}>{item.active === false ? <PauseCircle className="h-5 w-5" /> : <Repeat className="h-5 w-5" />}</span><div><p className="font-medium text-slate-200">{item.name}</p><p className="mt-1 text-xs text-slate-500">{item.category || 'Other'} · {item.billingFrequency || 'MONTHLY'}</p></div></div><div className="flex flex-wrap items-center gap-4 sm:justify-end"><div className="sm:text-right"><p className="font-mono-numbers font-semibold text-slate-200">{formatCurrency(item.amount)}</p><p className="mt-1 text-xs text-slate-500">Next {formatDate(item.nextPaymentDate)}</p></div><span className={`hidden items-center gap-1 rounded-lg px-2 py-1 text-xs font-semibold sm:inline-flex ${item.active === false ? 'bg-slate-800 text-slate-500' : 'bg-emerald-500/10 text-emerald-400'}`}>{item.active === false ? <PauseCircle className="h-3.5 w-3.5" /> : <CheckCircle2 className="h-3.5 w-3.5" />}{item.active === false ? 'Inactive' : 'Active'}</span><Button variant="secondary" disabled={actionId === item.id} onClick={() => setActive(item, item.active === false)}>{item.active === false ? 'Confirm' : 'Dismiss'}</Button><button aria-label={`Delete ${item.name || 'subscription'}`} disabled={actionId === item.id} onClick={() => remove(item)} className="rounded-lg p-2 text-slate-500 hover:bg-rose-500/10 hover:text-rose-300 disabled:opacity-50"><Trash2 className="h-4 w-4" /></button></div></div>)}{!filtered.length && <div className="p-10 text-center text-sm text-slate-500">No subscriptions match your search.</div>}</div>
    </Card>}

    <Modal open={open} onClose={() => setOpen(false)} title="Add a subscription" description="Track a recurring payment in your monthly money model."><form onSubmit={save} className="grid gap-4 sm:grid-cols-2"><FormField label="Name" className="sm:col-span-2"><input required maxLength="150" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className={inputClass} placeholder="Streaming service" /></FormField><FormField label="Amount"><input required min="0.01" step="0.01" type="number" value={form.amount} onChange={(event) => setForm({ ...form, amount: event.target.value })} className={inputClass} placeholder="0" /></FormField><FormField label="Frequency"><select value={form.billingFrequency} onChange={(event) => setForm({ ...form, billingFrequency: event.target.value })} className={inputClass}><option>WEEKLY</option><option>MONTHLY</option><option>QUARTERLY</option><option>YEARLY</option></select></FormField><FormField label="Next payment date"><input required type="date" value={form.nextPaymentDate} onChange={(event) => setForm({ ...form, nextPaymentDate: event.target.value })} className={inputClass} /></FormField><FormField label="Category"><select value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })} className={inputClass}>{categories.map((category) => <option key={category}>{category}</option>)}</select></FormField><div className="flex justify-end gap-2 sm:col-span-2"><Button type="button" variant="secondary" onClick={() => setOpen(false)}>Cancel</Button><Button disabled={saving}>{saving ? 'Saving…' : 'Add subscription'}</Button></div></form></Modal>
    <Toast message={toast} onClose={() => setToast('')} />
  </div>;
}
