const ACCESS_TOKEN_KEY = "healthdekho.accessToken";
const CUSTOMER_KEY = "healthdekho.customer";
const SESSION_EVENT = "healthdekho:session-change";

function notifySessionChange() {
  window.dispatchEvent(new Event(SESSION_EVENT));
}

export function getAccessToken() {
  return window.sessionStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getStoredCustomer() {
  const value = window.sessionStorage.getItem(CUSTOMER_KEY);
  if (!value) return null;
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

export function saveAccessToken(accessToken) {
  window.sessionStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  notifySessionChange();
}

export function saveSession({ accessToken, customer }) {
  window.sessionStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  if (customer) {
    window.sessionStorage.setItem(CUSTOMER_KEY, JSON.stringify(customer));
  } else {
    window.sessionStorage.removeItem(CUSTOMER_KEY);
  }
  notifySessionChange();
}

export function saveCustomer(customer) {
  window.sessionStorage.setItem(CUSTOMER_KEY, JSON.stringify(customer));
  notifySessionChange();
}

export function clearSession() {
  window.sessionStorage.removeItem(ACCESS_TOKEN_KEY);
  window.sessionStorage.removeItem(CUSTOMER_KEY);
  notifySessionChange();
}

export function sessionChangeEventName() {
  return SESSION_EVENT;
}
