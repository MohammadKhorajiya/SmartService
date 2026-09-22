import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Users,
  Smartphone,
  FileText,
  Wrench,
  Package,
  Receipt,
  CheckSquare,
  ShieldAlert,
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const { user } = useAuth();
  const role = user?.role;

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
    <aside className="w-64 bg-white border-r border-slate-200 min-h-[calc(100vh-4rem)] p-4 hidden md:block">
      <div className="space-y-1">
        <div className="px-3 py-2 text-[10px] font-bold uppercase tracking-wider text-slate-400">
          Navigation Menu
        </div>
        {filteredNav.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center space-x-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-50 text-blue-600 font-semibold'
                    : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
                }`
              }
            >
              <Icon className="w-4 h-4" />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </div>
    </aside>
  );
};
