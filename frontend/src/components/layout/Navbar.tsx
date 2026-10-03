import React, { useState, useEffect } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { StatusBadge } from '../ui/StatusBadge';
import { UserProfileModal } from '../../features/profile/UserProfileModal';
import {
  LogOut,
  User as UserIcon,
  Wrench,
  Settings,
  Menu,
  X,
  LayoutDashboard,
  Users,
  Smartphone,
  FileText,
  Package,
  Receipt,
} from 'lucide-react';
import { Button } from '../ui/Button';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const location = useLocation();
  const role = user?.role;

  // Auto-close mobile drawer on route change
  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [location.pathname]);

  const navItems = [
    { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard, roles: ['ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN', 'CUSTOMER'] },
    { label: 'Customers', path: '/customers', icon: Users, roles: ['ADMIN', 'MANAGER', 'STAFF'] },
    { label: 'Devices', path: '/devices', icon: Smartphone, roles: ['ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER'] },
    { label: 'Service Requests', path: '/service-requests', icon: FileText, roles: ['ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER'] },
    { label: 'Repair Jobs', path: '/repair-jobs', icon: Wrench, roles: ['ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN', 'CUSTOMER'] },
    { label: 'Inventory / Parts', path: '/inventory', icon: Package, roles: ['ADMIN', 'MANAGER', 'STAFF'] },
    { label: 'Invoices & Billing', path: '/invoices', icon: Receipt, roles: ['ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER'] },
  ];

  const filteredNav = navItems.filter((item) => role && item.roles.includes(role));

  return (
    <header className="h-16 bg-white border-b border-slate-200 px-4 sm:px-6 flex items-center justify-between sticky top-0 z-40 shadow-xs">
      <div className="flex items-center space-x-3">
        {/* Mobile Hamburger Button */}
        {user && (
          <button
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className="md:hidden min-h-[44px] min-w-[44px] flex items-center justify-center text-slate-700 hover:text-slate-900 hover:bg-slate-100 rounded-lg transition-colors cursor-pointer"
            aria-label="Toggle navigation menu"
          >
            {isMobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        )}

        <div className="flex items-center space-x-2.5">
          <div className="w-9 h-9 bg-blue-600 rounded-xl flex items-center justify-center text-white shadow-sm shrink-0">
            <Wrench className="w-5 h-5" />
          </div>
          <div>
            <span className="font-bold text-slate-900 text-base sm:text-lg tracking-tight">Smart Service</span>
            <span className="ml-1.5 text-[9px] sm:text-[10px] font-semibold bg-blue-50 text-blue-700 px-2 py-0.5 rounded-full uppercase border border-blue-200">
              SaaS
            </span>
          </div>
        </div>
      </div>

      {user && (
        <div className="flex items-center space-x-2 sm:space-x-3">
          {/* User badge button for desktop & mobile */}
          <button
            onClick={() => setIsProfileOpen(true)}
            className="flex items-center space-x-2 text-sm p-1.5 rounded-xl hover:bg-slate-100 transition-colors cursor-pointer text-left min-h-[44px]"
            title="Click to manage profile & password settings"
          >
            <div className="w-8 h-8 rounded-full bg-blue-50 border border-blue-200 flex items-center justify-center text-blue-600 font-semibold shrink-0">
              <UserIcon className="w-4 h-4" />
            </div>
            <div className="hidden sm:block text-left">
              <div className="font-semibold text-slate-900 text-xs">{user.fullName}</div>
              <div className="text-[10px] text-slate-500">{user.email}</div>
            </div>
            <StatusBadge status={user.role} />
          </button>

          {/* Desktop profile & logout actions */}
          <div className="hidden md:flex items-center space-x-2">
            <Button variant="outline" size="sm" onClick={() => setIsProfileOpen(true)} icon={<Settings className="w-3.5 h-3.5" />}>
              Profile
            </Button>
            <Button variant="ghost" size="sm" onClick={logout} icon={<LogOut className="w-4 h-4" />}>
              Logout
            </Button>
          </div>

          <UserProfileModal isOpen={isProfileOpen} onClose={() => setIsProfileOpen(false)} />
        </div>
      )}

      {/* Mobile Navigation Slide-Out Overlay & Drawer */}
      {user && isMobileMenuOpen && (
        <div className="md:hidden fixed inset-0 z-50 flex">
          {/* Backdrop */}
          <div
            className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs transition-opacity"
            onClick={() => setIsMobileMenuOpen(false)}
          />

          {/* Drawer content */}
          <div className="relative w-4/5 max-w-xs bg-white h-full shadow-2xl flex flex-col z-10 animate-in slide-in-from-left duration-200">
            {/* Drawer Header */}
            <div className="p-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
              <div className="flex items-center space-x-2.5">
                <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white">
                  <Wrench className="w-4 h-4" />
                </div>
                <span className="font-bold text-slate-900 text-sm">Smart Service Menu</span>
              </div>
              <button
                onClick={() => setIsMobileMenuOpen(false)}
                className="min-h-[44px] min-w-[44px] flex items-center justify-center text-slate-500 hover:text-slate-700 rounded-lg"
                aria-label="Close menu"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* User Profile Card in Mobile Menu */}
            <div className="p-4 border-b border-slate-100 bg-blue-50/50">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 rounded-full bg-blue-600 text-white font-bold flex items-center justify-center text-sm shadow-xs">
                  {user.fullName.charAt(0)}
                </div>
                <div className="overflow-hidden">
                  <p className="font-semibold text-slate-900 text-sm truncate">{user.fullName}</p>
                  <p className="text-xs text-slate-500 truncate">{user.email}</p>
                  <div className="mt-1">
                    <StatusBadge status={user.role} />
                  </div>
                </div>
              </div>
            </div>

            {/* Nav Items List */}
            <nav className="flex-1 overflow-y-auto p-3 space-y-1">
              <div className="px-3 py-1.5 text-[10px] font-bold uppercase tracking-wider text-slate-400">
                Navigation
              </div>
              {filteredNav.map((item) => {
                const Icon = item.icon;
                return (
                  <NavLink
                    key={item.path}
                    to={item.path}
                    onClick={() => setIsMobileMenuOpen(false)}
                    className={({ isActive }) =>
                      `flex items-center space-x-3 px-3 py-3 rounded-xl text-sm font-medium transition-colors min-h-[44px] ${
                        isActive
                          ? 'bg-blue-600 text-white font-semibold shadow-xs'
                          : 'text-slate-700 hover:bg-slate-100 hover:text-slate-900'
                      }`
                    }
                  >
                    <Icon className="w-5 h-5 shrink-0" />
                    <span>{item.label}</span>
                  </NavLink>
                );
              })}
            </nav>

            {/* Mobile Footer Actions */}
            <div className="p-4 border-t border-slate-200 space-y-2 bg-slate-50">
              <button
                onClick={() => {
                  setIsMobileMenuOpen(false);
                  setIsProfileOpen(true);
                }}
                className="w-full flex items-center justify-center space-x-2 min-h-[44px] px-4 py-2.5 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-xl hover:bg-slate-100 transition-colors"
              >
                <Settings className="w-4 h-4 text-slate-500" />
                <span>Account Settings</span>
              </button>
              <button
                onClick={() => {
                  setIsMobileMenuOpen(false);
                  logout();
                }}
                className="w-full flex items-center justify-center space-x-2 min-h-[44px] px-4 py-2.5 text-xs font-semibold text-red-600 bg-red-50 border border-red-200 rounded-xl hover:bg-red-100 transition-colors"
              >
                <LogOut className="w-4 h-4 text-red-600" />
                <span>Logout</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </header>
  );
};

