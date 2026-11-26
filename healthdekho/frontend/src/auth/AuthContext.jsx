import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { getCurrentCustomer, loginCustomer } from "../api/auth.js";
import {
  clearSession,
  getAccessToken,
  getStoredCustomer,
  saveAccessToken,
  saveCustomer,
  saveSession,
  sessionChangeEventName,
} from "./session.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSession] = useState({ status: "checking", customer: null });

  useEffect(() => {
    let active = true;
    const storedToken = getAccessToken();
    setSession({ status: storedToken ? "checking" : "anonymous", customer: null });

    const updateFromSessionStorage = () => {
      const token = getAccessToken();
      const customer = getStoredCustomer();
      setSession(token && customer
        ? { status: "authenticated", customer }
        : { status: token ? "checking" : "anonymous", customer: null });
    };

    window.addEventListener(sessionChangeEventName(), updateFromSessionStorage);
    if (storedToken) {
      getCurrentCustomer()
        .then((customer) => {
          if (!active) return;
          saveCustomer(customer);
          setSession({ status: "authenticated", customer });
        })
        .catch(() => {
          clearSession();
          if (active) setSession({ status: "anonymous", customer: null });
        });
    }

    return () => {
      active = false;
      window.removeEventListener(sessionChangeEventName(), updateFromSessionStorage);
    };
  }, []);

  const signIn = useCallback(async (credentials) => {
    const response = await loginCustomer(credentials);
    saveAccessToken(response.accessToken);
    try {
      const customer = await getCurrentCustomer();
      saveSession({ accessToken: response.accessToken, customer });
      setSession({ status: "authenticated", customer });
    } catch (error) {
      clearSession();
      setSession({ status: "anonymous", customer: null });
      throw error;
    }
  }, []);

  const signOut = useCallback(() => {
    clearSession();
    setSession({ status: "anonymous", customer: null });
  }, []);

  const value = useMemo(() => ({ ...session, signIn, signOut }), [session, signIn, signOut]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("useAuth must be used within AuthProvider");
  return value;
}
