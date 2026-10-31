const EMAIL_PATTERN = /^[A-Za-z0-9](?:[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]{0,62}[A-Za-z0-9])?@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$/;

export function isValidEmail(value) {
  const email = String(value || '').trim();
  return email.length <= 150 && !email.includes('..') && EMAIL_PATTERN.test(email);
}

export const emailPattern = EMAIL_PATTERN.source;

export function emailValidationMessage(value) {
  if (!String(value || '').trim()) return 'Email is required';
  return isValidEmail(value) ? '' : 'Enter a valid email with a domain such as you@example.com';
}
