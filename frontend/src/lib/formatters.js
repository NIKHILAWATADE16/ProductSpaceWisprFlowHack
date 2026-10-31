export const INR = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
});

export function formatCurrency(value, fallback = '₹0') {
  if (value === null || value === undefined || value === '') return fallback;
  const amount = Number(value);
  return Number.isFinite(amount) ? INR.format(amount) : fallback;
}

export function formatPercent(value, fallback = '0%') {
  if (value === null || value === undefined || value === '') return fallback;
  const amount = Number(value);
  return Number.isFinite(amount) ? `${amount.toFixed(amount % 1 === 0 ? 0 : 1)}%` : fallback;
}

export function formatDate(value, options = {}) {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '—';
  return new Intl.DateTimeFormat('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    ...options,
  }).format(date);
}

export function monthLabel(month, year) {
  if (!month || !year) return 'This month';
  return new Intl.DateTimeFormat('en-IN', { month: 'long', year: 'numeric' })
    .format(new Date(Number(year), Number(month) - 1, 1));
}

export function getErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  return error?.response?.data?.message || error?.response?.data?.error || error?.message || fallback;
}

export function clamp(value, min = 0, max = 100) {
  return Math.min(max, Math.max(min, Number(value) || 0));
}
