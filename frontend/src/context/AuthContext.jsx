import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import axiosClient from '../api/axiosClient';

const AuthContext = createContext(null);

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem('financeflow_user') || 'null');
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);
  const [loading, setLoading] = useState(Boolean(localStorage.getItem('token')));

  useEffect(() => {
    const handleExpiredSession = () => setUser(null);
    window.addEventListener('financeflow:session-expired', handleExpiredSession);
    const token = localStorage.getItem('token');
    if (!token) {
      setLoading(false);
      return () => window.removeEventListener('financeflow:session-expired', handleExpiredSession);
    }

    axiosClient.get('/auth/me')
      .then(({ data }) => {
        setUser(data);
        localStorage.setItem('financeflow_user', JSON.stringify(data));
      })
      .catch(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('financeflow_user');
        setUser(null);
      })
      .finally(() => setLoading(false));

    return () => window.removeEventListener('financeflow:session-expired', handleExpiredSession);
  }, []);

  const value = useMemo(() => ({
    user,
    loading,
    isAuthenticated: Boolean(user && localStorage.getItem('token')),
    signIn: async (credentials) => {
      const { data } = await axiosClient.post('/auth/login', credentials);
      localStorage.setItem('token', data.token);
      localStorage.setItem('financeflow_user', JSON.stringify(data.user));
      setUser(data.user);
      return data;
    },
    register: async (payload) => {
      const { data } = await axiosClient.post('/auth/register', payload);
      localStorage.setItem('token', data.token);
      localStorage.setItem('financeflow_user', JSON.stringify(data.user));
      setUser(data.user);
      return data;
    },
    signOut: () => {
      localStorage.removeItem('token');
      localStorage.removeItem('financeflow_user');
      setUser(null);
    },
  }), [loading, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
