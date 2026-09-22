import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { StatusBadge } from '../ui/StatusBadge';
import { UserProfileModal } from '../../features/profile/UserProfileModal';
import { LogOut, User as UserIcon, Wrench, Settings } from 'lucide-react';
import { Button } from '../ui/Button';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();
  const [isProfileOpen, setIsProfileOpen] = React.useState(false);

  return (
    <header className="h-16 bg-white border-b border-slate-200 px-6 flex items-center justify-between sticky top-0 z-30 shadow-xs">
      <div className="flex items-center space-x-3">
        <div className="w-9 h-9 bg-blue-600 rounded-xl flex items-center justify-center text-white shadow-sm">
          <Wrench className="w-5 h-5" />
        </div>
        <div>
          <span className="font-bold text-slate-900 text-lg tracking-tight">Smart Service</span>
          <span className="ml-2 text-[10px] font-semibold bg-blue-50 text-blue-700 px-2 py-0.5 rounded-full uppercase border border-blue-200">SaaS Platform</span>
        </div>
      </div>

      {user && (
        <div className="flex items-center space-x-3">
          <button
            onClick={() => setIsProfileOpen(true)}
            className="flex items-center space-x-2 text-sm p-1.5 rounded-xl hover:bg-slate-100 transition-colors cursor-pointer text-left"
            title="Click to manage profile & password settings"
          >
            <div className="w-8 h-8 rounded-full bg-blue-50 border border-blue-200 flex items-center justify-center text-blue-600 font-semibold">
              <UserIcon className="w-4 h-4" />
            </div>
            <div className="hidden sm:block text-left">
              <div className="font-semibold text-slate-900 text-xs">{user.fullName}</div>
              <div className="text-[10px] text-slate-500">{user.email}</div>
            </div>
            <StatusBadge status={user.role} />
          </button>

          <Button variant="outline" size="sm" onClick={() => setIsProfileOpen(true)} icon={<Settings className="w-3.5 h-3.5" />}>
            Profile
          </Button>

          <Button variant="ghost" size="sm" onClick={logout} icon={<LogOut className="w-4 h-4" />}>
            Logout
          </Button>

          <UserProfileModal isOpen={isProfileOpen} onClose={() => setIsProfileOpen(false)} />
        </div>
      )}
    </header>
  );
};
