import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AppLayout } from '../components/layout/AppLayout';
import { Login } from '../features/auth/Login';
import { Register } from '../features/auth/Register';
import { Dashboard } from '../features/dashboard/Dashboard';
import { CustomerList } from '../features/customers/CustomerList';
import { DeviceList } from '../features/devices/DeviceList';
import { ServiceRequestList } from '../features/serviceRequests/ServiceRequestList';
import { RepairJobList } from '../features/repairJobs/RepairJobList';
import { InventoryList } from '../features/inventory/InventoryList';
import { InvoiceList } from '../features/invoices/InvoiceList';
import { RoleGuard } from './RoleGuard';

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route
        path="/"
        element={
          <RoleGuard>
            <AppLayout />
          </RoleGuard>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route
          path="customers"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'MANAGER', 'STAFF']}>
              <CustomerList />
            </RoleGuard>
          }
        />
        <Route path="devices" element={<DeviceList />} />
        <Route path="service-requests" element={<ServiceRequestList />} />
        <Route path="repair-jobs" element={<RepairJobList />} />
        <Route
          path="inventory"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'MANAGER', 'STAFF']}>
              <InventoryList />
            </RoleGuard>
          }
        />
        <Route path="invoices" element={<InvoiceList />} />
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
};
