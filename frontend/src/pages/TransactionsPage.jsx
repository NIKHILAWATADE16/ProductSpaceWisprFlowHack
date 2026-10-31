import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  ArrowDownLeft,
  ArrowUpRight,
  Edit3,
  Filter,
  ListChecks,
  Plus,
  ReceiptText,
  Search,
  Settings2,
  Trash2,
  Upload,
  MessageSquare,
  X,
} from 'lucide-react';
import axiosClient from '../api/axiosClient';
import PageHeader from '../components/common/PageHeader';
import { Button, Card, EmptyState, ErrorState, LoadingState } from '../components/common/UiStates';
import Modal from '../components/common/Modal';
import FormField, { inputClass } from '../components/common/FormField';
import Toast from '../components/common/Toast';
import { formatCurrency, formatDate, getErrorMessage } from '../lib/formatters';

const fallbackCategories = [
  'Other',
  'Food & Dining',
  'Housing',
  'Transport',
  'Shopping',
  'Entertainment',
  'Utilities',
  'Healthcare',
  'Salary',
  'Income',
  'Investments',
  'Subscriptions',
  'Personal Care',
  'Education',
  'Travel',
];

const emptyForm = {
  id: null,
  description: '',
  amount: '',
  transactionDate: new Date().toISOString().slice(0, 10),
  type: 'EXPENSE',
  category: 'Other',
  merchant: '',
  paymentMethod: '',
  recurring: false,
};

function toForm(transaction) {
  return {
    id: transaction.id,
    description: transaction.description || '',
    amount: transaction.amount ?? '',
    transactionDate: transaction.transactionDate || new Date().toISOString().slice(0, 10),
    type: transaction.type || 'EXPENSE',
    category: transaction.category || 'Other',
    merchant: transaction.merchant || '',
    paymentMethod: transaction.paymentMethod || '',
    recurring: Boolean(transaction.recurring),
  };
}

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState([]);
  const [categories, setCategories] = useState(fallbackCategories);
  const [rules, setRules] = useState([]);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('ALL');
  const [type, setType] = useState('ALL');
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [importing, setImporting] = useState(false);
  const [smsOpen, setSmsOpen] = useState(false);
  const [smsText, setSmsText] = useState('');
  const [smsImporting, setSmsImporting] = useState(false);
  const [toast, setToast] = useState('');
  const [importResult, setImportResult] = useState(null);
  const fileRef = useRef(null);

  const load = useCallback(async () => {
    setStatus('loading');
    setError('');
    try {
      const { data } = await axiosClient.get('/transactions');
      setTransactions(Array.isArray(data) ? data : data?.content || []);
      setStatus('ready');
    } catch (err) {
      setError(getErrorMessage(err, 'Transactions are not available yet.'));
      setStatus('error');
      return;
    }

    try {
      const [{ data: categoryData }, { data: ruleData }] = await Promise.all([
        axiosClient.get('/transactions/categories'),
        axiosClient.get('/transactions/rules'),
      ]);
      if (Array.isArray(categoryData) && categoryData.length) setCategories(categoryData);
      setRules(Array.isArray(ruleData) ? ruleData : []);
    } catch {
      // Transaction data remains usable if optional categorization metadata is unavailable.
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const filtered = useMemo(() => transactions.filter((item) => {
    const text = `${item.description || ''} ${item.merchant || ''}`.toLowerCase();
    return (!query || text.includes(query.toLowerCase()))
      && (category === 'ALL' || item.category === category)
      && (type === 'ALL' || item.type === type);
  }), [transactions, query, category, type]);

  const setValue = (key, value) => setForm((current) => ({ ...current, [key]: value }));

  const openCreate = () => {
    setForm(emptyForm);
    setModal('edit');
  };

  const openEdit = (transaction) => {
    setForm(toForm(transaction));
    setModal('edit');
  };

  const save = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    const payload = { ...form, amount: Number(form.amount) };
    delete payload.id;
    try {
      if (form.id) {
        await axiosClient.put(`/transactions/${form.id}`, payload);
        setToast('Transaction updated and categorization rule learned');
      } else {
        await axiosClient.post('/transactions', payload);
        setToast('Transaction added');
      }
      setModal(null);
      setForm(emptyForm);
      await load();
    } catch (err) {
      setError(getErrorMessage(err, 'Could not save this transaction.'));
    } finally {
      setSaving(false);
    }
  };

  const remove = async (transaction) => {
    if (!window.confirm(`Delete “${transaction.description || 'this transaction'}”?`)) return;
    setDeletingId(transaction.id);
    setError('');
    try {
      await axiosClient.delete(`/transactions/${transaction.id}`);
      setToast('Transaction deleted');
      await load();
    } catch (err) {
      setError(getErrorMessage(err, 'Could not delete this transaction.'));
    } finally {
      setDeletingId(null);
    }
  };

  const importFile = async (event) => {
    const input = event.target;
    const file = input.files?.[0];
    if (!file) return;

    const isPdf = file.name.toLowerCase().endsWith('.pdf') || file.type === 'application/pdf';
    const supportedTypes = ['.csv', '.txt', '.pdf'];
    const hasSupportedExtension = supportedTypes.some((extension) => file.name.toLowerCase().endsWith(extension));
    if (!hasSupportedExtension && !['text/csv', 'text/plain', 'application/pdf'].includes(file.type)) {
      setError('Unsupported file type. Upload a CSV, TXT, or PDF statement.');
      input.value = '';
      return;
    }
    if (file.size === 0) {
      setError('The selected file is empty. Choose a statement with transaction rows.');
      input.value = '';
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      setError('The statement is too large. Upload a file smaller than 10 MB.');
      input.value = '';
      return;
    }

    const body = new FormData();
    body.append('file', file, file.name);
    setError('');
    setImporting(true);
    try {
      const { data } = await axiosClient.post('/transactions/import', body, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      const imported = data?.imported ?? data?.importedCount ?? 0;
      const skipped = data?.skipped ?? data?.skippedDuplicates ?? 0;
      setImportResult({
        imported,
        skipped,
        totalParsed: data?.totalParsed ?? imported + skipped,
      });
      setModal('import');
      setToast(`${imported} transactions imported`);
      await load();
    } catch (err) {
      const fallback = isPdf
        ? 'This PDF could not be imported. Upload a text-based PDF statement; scanned or password-protected PDFs are not supported.'
        : 'This statement could not be imported. Upload a supported CSV or TXT statement.';
      setError(getErrorMessage(err, fallback));
    } finally {
      setImporting(false);
      input.value = '';
    }
  };

  const importSms = async (event) => {
    event.preventDefault();
    if (!smsText.trim()) {
      setError('Paste an SMS statement before importing.');
      return;
    }
    setSmsImporting(true);
    setError('');
    try {
      const { data } = await axiosClient.post('/transactions/import-sms', { smsText: smsText.trim() });
      const imported = data?.imported ?? data?.importedCount ?? 0;
      const skipped = data?.skipped ?? data?.skippedDuplicates ?? 0;
      setImportResult({ imported, skipped, totalParsed: data?.totalParsed ?? imported + skipped });
      setSmsOpen(false);
      setSmsText('');
      setModal('import');
      setToast(`${imported} transactions imported from SMS`);
      await load();
    } catch (err) {
      setError(getErrorMessage(err, 'This SMS could not be imported. Check that it contains a bank transaction alert.'));
    } finally {
      setSmsImporting(false);
    }
  };

  const deleteRule = async (rule) => {
    if (!window.confirm(`Remove the learned rule for “${rule.keyword}”?`)) return;
    try {
      await axiosClient.delete(`/transactions/rules/${rule.id}`);
      setRules((current) => current.filter((item) => item.id !== rule.id));
      setToast('Learned rule removed');
    } catch (err) {
      setError(getErrorMessage(err, 'Could not remove this learned rule.'));
    }
  };

  return <div className="space-y-7">
    <PageHeader
      icon={ReceiptText}
      eyebrow="Cash flow"
      title="Transactions"
      description="Review, search, import, and correct the activity that powers your money model."
      action={<div className="flex flex-wrap gap-2">
        <input ref={fileRef} type="file" accept=".csv,.txt,.pdf" onChange={importFile} className="hidden" />
        <Button variant="secondary" onClick={() => setModal('rules')}><Settings2 className="h-4 w-4" />Rules</Button>
        <Button variant="secondary" onClick={() => setSmsOpen(true)}><MessageSquare className="h-4 w-4" />Paste SMS</Button>
        <Button variant="secondary" disabled={importing} onClick={() => fileRef.current?.click()}><Upload className="h-4 w-4" />{importing ? 'Importing…' : 'Import statement'}</Button>
        <Button onClick={openCreate}><Plus className="h-4 w-4" />Add transaction</Button>
      </div>}
    />

    {error && <ErrorState message={error} onRetry={load} />}

    <Card className="p-4">
      <div className="grid gap-3 lg:grid-cols-[1fr_180px_150px_auto]">
        <label className="relative">
          <Search className="absolute left-3 top-3 h-4 w-4 text-slate-600" />
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search descriptions or merchants" className={`${inputClass} pl-10`} />
        </label>
        <select value={category} onChange={(event) => setCategory(event.target.value)} className={inputClass}>
          <option value="ALL">All categories</option>
          {categories.map((item) => <option key={item}>{item}</option>)}
        </select>
        <select value={type} onChange={(event) => setType(event.target.value)} className={inputClass}>
          <option value="ALL">All activity</option>
          <option value="EXPENSE">Expenses</option>
          <option value="INCOME">Income</option>
        </select>
        <Button variant="secondary" onClick={() => { setQuery(''); setCategory('ALL'); setType('ALL'); }}><Filter className="h-4 w-4" />Reset</Button>
      </div>
    </Card>

    {status === 'loading' ? <LoadingState label="Loading transactions…" /> : status === 'ready' && filtered.length === 0 ? <EmptyState
      icon={ReceiptText}
      title={transactions.length ? 'No matching transactions' : 'Your transaction feed is empty'}
      description={transactions.length ? 'Try changing your filters.' : 'Add a transaction or import a CSV, TXT, or text-based PDF statement to begin.'}
      action={<Button onClick={openCreate}><Plus className="h-4 w-4" />Add your first transaction</Button>}
    /> : <Card className="overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full min-w-[900px] text-left text-sm">
          <thead className="border-b border-surface-border bg-slate-900/40 text-xs uppercase tracking-wider text-slate-500">
            <tr><th className="px-5 py-4 font-semibold">Transaction</th><th className="px-5 py-4 font-semibold">Category</th><th className="px-5 py-4 font-semibold">Date</th><th className="px-5 py-4 text-right font-semibold">Amount</th><th className="px-5 py-4 text-right font-semibold">Actions</th></tr>
          </thead>
          <tbody className="divide-y divide-surface-border">
            {filtered.map((item) => <tr key={item.id || `${item.description}-${item.transactionDate}`} className="transition hover:bg-slate-800/30">
              <td className="px-5 py-4"><div className="flex items-center gap-3"><span className={`flex h-9 w-9 items-center justify-center rounded-xl ${item.type === 'INCOME' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-slate-800 text-slate-400'}`}>{item.type === 'INCOME' ? <ArrowDownLeft className="h-4 w-4" /> : <ArrowUpRight className="h-4 w-4" />}</span><div><p className="font-medium text-slate-200">{item.description || 'Untitled transaction'}</p><p className="mt-0.5 text-xs text-slate-500">{item.merchant || item.paymentMethod || 'Manual entry'}</p></div></div></td>
              <td className="px-5 py-4"><span className="rounded-lg bg-slate-800 px-2.5 py-1 text-xs text-slate-400">{item.category || 'Other'}</span></td>
              <td className="px-5 py-4 text-slate-500">{formatDate(item.transactionDate)}</td>
              <td className={`px-5 py-4 text-right font-mono-numbers font-semibold ${item.type === 'INCOME' ? 'text-emerald-400' : 'text-slate-200'}`}>{item.type === 'INCOME' ? '+' : '-'}{formatCurrency(item.amount)}</td>
              <td className="px-5 py-4"><div className="flex justify-end gap-1"><button aria-label={`Edit ${item.description || 'transaction'}`} onClick={() => openEdit(item)} className="rounded-lg p-2 text-slate-500 hover:bg-slate-800 hover:text-blue-300"><Edit3 className="h-4 w-4" /></button><button aria-label={`Delete ${item.description || 'transaction'}`} disabled={deletingId === item.id} onClick={() => remove(item)} className="rounded-lg p-2 text-slate-500 hover:bg-rose-500/10 hover:text-rose-300 disabled:opacity-50"><Trash2 className="h-4 w-4" /></button></div></td>
            </tr>)}
          </tbody>
        </table>
      </div>
    </Card>}

    <Modal open={modal === 'edit'} onClose={() => setModal(null)} title={form.id ? 'Edit transaction' : 'Add transaction'} description="Changing a category teaches the categorization engine for the same merchant." wide>
      <form onSubmit={save} className="grid gap-4 sm:grid-cols-2">
        <FormField label="Description" className="sm:col-span-2"><input required value={form.description} onChange={(event) => setValue('description', event.target.value)} className={inputClass} placeholder="e.g. Grocery run" /></FormField>
        <FormField label="Amount"><input required min="0.01" step="0.01" type="number" value={form.amount} onChange={(event) => setValue('amount', event.target.value)} className={inputClass} placeholder="0" /></FormField>
        <FormField label="Type"><select value={form.type} onChange={(event) => setValue('type', event.target.value)} className={inputClass}><option value="EXPENSE">Expense</option><option value="INCOME">Income</option></select></FormField>
        <FormField label="Date"><input required type="date" value={form.transactionDate} onChange={(event) => setValue('transactionDate', event.target.value)} className={inputClass} /></FormField>
        <FormField label="Category"><select value={form.category} onChange={(event) => setValue('category', event.target.value)} className={inputClass}>{categories.map((item) => <option key={item}>{item}</option>)}</select></FormField>
        <FormField label="Merchant"><input value={form.merchant} onChange={(event) => setValue('merchant', event.target.value)} className={inputClass} placeholder="Optional" /></FormField>
        <FormField label="Payment method"><input value={form.paymentMethod} onChange={(event) => setValue('paymentMethod', event.target.value)} className={inputClass} placeholder="Optional" /></FormField>
        <label className="flex items-center gap-2 text-sm text-slate-400 sm:col-span-2"><input type="checkbox" checked={form.recurring} onChange={(event) => setValue('recurring', event.target.checked)} className="h-4 w-4 rounded border-slate-700 bg-slate-900 text-blue-500" />Mark as recurring</label>
        <div className="flex justify-end gap-2 sm:col-span-2"><Button type="button" variant="secondary" onClick={() => setModal(null)}>Cancel</Button><Button disabled={saving}>{saving ? 'Saving…' : form.id ? 'Save changes' : 'Save transaction'}</Button></div>
      </form>
    </Modal>

    <Modal open={smsOpen} onClose={() => !smsImporting && setSmsOpen(false)} title="Import from SMS" description="Paste a bank alert to turn it into a transaction without sharing it with the coach.">
      <form onSubmit={importSms} className="space-y-4">
        <FormField label="Bank SMS"><textarea required rows={7} value={smsText} onChange={(event) => setSmsText(event.target.value)} className={`${inputClass} min-h-40 resize-y`} placeholder="e.g. Your A/C XXXX debited by Rs. 450 at Swiggy on 31-10-2026" /></FormField>
        <p className="text-xs leading-5 text-slate-500">The parser accepts common debit, credit, UPI, and card alert formats. Duplicate transactions are skipped.</p>
        <div className="flex justify-end gap-2"><Button type="button" variant="secondary" disabled={smsImporting} onClick={() => setSmsOpen(false)}>Cancel</Button><Button disabled={smsImporting}>{smsImporting ? 'Importing…' : 'Import SMS'}</Button></div>
      </form>
    </Modal>

    <Modal open={modal === 'import'} onClose={() => setModal(null)} title="Import complete" description="Duplicate rows were skipped without changing existing transactions.">
      {importResult && <div className="space-y-3"><div className="grid grid-cols-3 gap-3">{[['Imported', importResult.imported], ['Skipped', importResult.skipped], ['Parsed', importResult.totalParsed]].map(([label, value]) => <div key={label} className="rounded-xl border border-surface-border bg-slate-900/50 p-4 text-center"><p className="text-xs uppercase tracking-wider text-slate-500">{label}</p><p className="mt-2 font-mono-numbers text-2xl font-semibold text-white">{value}</p></div>)}</div><div className="flex items-center gap-2 rounded-xl bg-blue-500/5 p-4 text-sm leading-6 text-slate-400"><ListChecks className="h-4 w-4 shrink-0 text-blue-400" />Imported rows are categorized from generic and learned user rules.</div><div className="flex justify-end"><Button onClick={() => setModal(null)}>Done</Button></div></div>}
    </Modal>

    <Modal open={modal === 'rules'} onClose={() => setModal(null)} title="Learned categorization rules" description="Correcting a transaction category creates a merchant rule for future imports.">
      {rules.length ? <div className="space-y-3">{rules.filter((rule) => rule.userOverride).map((rule) => <div key={rule.id} className="flex items-center justify-between gap-4 rounded-xl border border-surface-border bg-slate-900/50 p-4"><div><p className="font-medium text-slate-200">{rule.keyword}</p><p className="mt-1 text-xs text-slate-500">Categorize as <span className="text-blue-300">{rule.category}</span></p></div><button aria-label={`Delete rule for ${rule.keyword}`} onClick={() => deleteRule(rule)} className="rounded-lg p-2 text-slate-500 hover:bg-rose-500/10 hover:text-rose-300"><Trash2 className="h-4 w-4" /></button></div>)}{!rules.some((rule) => rule.userOverride) && <p className="py-8 text-center text-sm text-slate-500">No learned rules yet. Edit a transaction category to teach the engine.</p>}</div> : <div className="py-8 text-center text-sm text-slate-500">No learned rules yet. Edit a transaction category to teach the engine.</div>}
      <div className="mt-5 flex justify-end"><Button variant="secondary" onClick={() => setModal(null)}>Close</Button></div>
    </Modal>

    <Toast message={toast} onClose={() => setToast('')} />
  </div>;
}
