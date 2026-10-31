export const INR = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
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
  const date = typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value)
    ? new Date(`${value}T00:00:00`)
    : new Date(value);
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

export function localizeCurrencyText(value) {
  return typeof value === 'string' ? value.replace(/\$/g, '₹') : value;
}

export function getErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  const data = error?.response?.data;
  if (data?.validationErrors && typeof data.validationErrors === 'object') {
    const details = Object.entries(data.validationErrors)
      .map(([field, message]) => `${field}: ${message}`)
      .join(' • ');
    if (details) return details;
  }
  return data?.message || data?.error || error?.message || fallback;
}

export function clamp(value, min = 0, max = 100) {
  return Math.min(max, Math.max(min, Number(value) || 0));
}
