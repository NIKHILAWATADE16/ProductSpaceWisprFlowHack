import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ArrowRight, Eye, EyeOff, LockKeyhole } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Button } from '../components/common/UiStates';
import FormField, { inputClass } from '../components/common/FormField';
import { getErrorMessage } from '../lib/formatters';
import { emailPattern, emailValidationMessage } from '../lib/validation';
import { AuthShell } from './authShell';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { signIn } = useAuth();
  const [form, setForm] = useState({ email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [emailError, setEmailError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    const validationError = emailValidationMessage(form.email);
    if (validationError) {
      setEmailError(validationError);
      return;
    }
    setEmailError('');
    setError('');
    setSubmitting(true);
    try {
      await signIn({ ...form, email: form.email.trim().toLowerCase() });
      navigate(location.state?.from?.pathname || '/', { replace: true });
    } catch (err) {
      setError(getErrorMessage(err, 'We could not sign you in. Check your details and try again.'));
    } finally {
      setSubmitting(false);
    }
  };

  return <AuthShell>
    <div className="mb-8">
      <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-500/15 text-blue-300"><LockKeyhole className="h-5 w-5" /></div>
      <h1 className="text-2xl font-bold text-white">Welcome back</h1>
      <p className="mt-2 text-sm leading-6 text-slate-400">Sign in to see the money model that makes your next decision clearer.</p>
    </div>
    <form onSubmit={submit} className="space-y-5" noValidate>
      <FormField label="Email" error={emailError}><input required type="email" inputMode="email" autoComplete="email" maxLength={150} pattern={emailPattern} value={form.email} onChange={(event) => { setForm({ ...form, email: event.target.value }); setEmailError(''); }} className={inputClass} placeholder="you@example.com" /></FormField>
      <FormField label="Password"><div className="relative"><input required autoComplete="current-password" type={showPassword ? 'text' : 'password'} value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} className={`${inputClass} pr-11`} placeholder="Your password" /><button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-3 top-3 text-slate-500 hover:text-slate-200">{showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}</button></div></FormField>
      {error && <p className="rounded-lg border border-rose-500/20 bg-rose-500/10 px-3 py-2.5 text-sm text-rose-300">{error}</p>}
      <Button className="w-full" disabled={submitting}>{submitting ? 'Signing in…' : <>Continue <ArrowRight className="h-4 w-4" /></>}</Button>
    </form>
    <p className="mt-7 text-center text-sm text-slate-500">New here? <Link to="/register" className="font-semibold text-blue-400 hover:text-blue-300">Create your workspace</Link></p>
  </AuthShell>;
}
