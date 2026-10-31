import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { Loader2 } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function ProtectedRoute() {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <div className="flex min-h-screen items-center justify-center bg-background text-slate-400"><Loader2 className="h-5 w-5 animate-spin" /></div>;
  }

  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace state={{ from: location }} />;
}
