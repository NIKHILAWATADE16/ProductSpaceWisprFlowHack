import { Outlet } from 'react-router-dom';
import { useState } from 'react';
import Sidebar from './Sidebar';
import TopNav from './TopNav';

export default function MainLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);
  return <div className="flex min-h-screen bg-background text-slate-100 font-sans">
    <Sidebar mobileOpen={mobileOpen} onClose={() => setMobileOpen(false)} />
    <div className="flex min-w-0 flex-1 flex-col lg:pl-64">
      <TopNav onMenu={() => setMobileOpen(true)} />
      <main className="mx-auto w-full max-w-[1480px] flex-1 p-4 sm:p-6 lg:p-8"><Outlet /></main>
    </div>
  </div>;
}
