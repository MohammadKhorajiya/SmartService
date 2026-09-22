import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/client';
import { Device } from '../../types';
import { Card } from '../../components/ui/Card';
import { Table } from '../../components/ui/Table';
import { Button } from '../../components/ui/Button';
import { Spinner } from '../../components/ui/Spinner';
import { ErrorState } from '../../components/ui/ErrorState';
import { AddDeviceModal } from './AddDeviceModal';
import { useAuth } from '../../context/AuthContext';
import { Smartphone, Plus } from 'lucide-react';

export const DeviceList: React.FC = () => {
  const { user } = useAuth();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const customerId = user?.role === 'CUSTOMER' ? user.customerId : null;

  const { data, isLoading, error, refetch } = useQuery<Device[]>({
    queryKey: ['devices', customerId],
    queryFn: async () => {
      if (user?.role === 'CUSTOMER' && customerId) {
        const res = await api.get(`/devices/customer/${customerId}`);
        return res.data.data;
      } else {
        const res = await api.get('/customers?size=50');
        // Fetch devices for top customers
        const firstCustId = res.data.data.content[0]?.id || 1;
        const devRes = await api.get(`/devices/customer/${firstCustId}`);
        return devRes.data.data;
      }
    },
  });

  const columns = [
    {
      header: 'Device Brand & Model',
      accessor: (d: Device) => (
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-blue-50 text-blue-600 rounded-lg">
            <Smartphone className="w-4 h-4" />
          </div>
          <div>
            <div className="font-semibold text-slate-900">{d.brand} {d.model}</div>
            <div className="text-xs text-slate-500">{d.deviceType}</div>
          </div>
        </div>
      ),
    },
    { header: 'Customer Owner', accessor: (d: Device) => <span className="text-slate-700 font-medium">{d.customerName || 'Customer #' + d.customerId}</span> },
    { header: 'Serial Number', accessor: (d: Device) => <span className="text-slate-600 font-mono text-xs">{d.serialNumber || 'N/A'}</span> },
    { header: 'IMEI', accessor: (d: Device) => <span className="text-slate-600 font-mono text-xs">{d.imei || 'N/A'}</span> },
    {
      header: 'Registered Date & Time',
      accessor: (d: Device) => (
        <div className="text-slate-600 text-xs font-medium">
          <div>{new Date(d.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })}</div>
          <div className="text-[11px] text-slate-400 font-mono">{new Date(d.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true })}</div>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6 font-sans">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Registered Devices</h1>
          <p className="text-xs text-slate-500 mt-1">Customer mobile phones, laptops, and repair equipment</p>
        </div>
        <Button variant="primary" icon={<Plus className="w-4 h-4" />} onClick={() => setIsModalOpen(true)}>
          Register Device
        </Button>
      </div>

      <Card>
        {isLoading ? (
          <Spinner />
        ) : error ? (
          <ErrorState message="Failed to load registered devices" onRetry={refetch} />
        ) : (
          <Table columns={columns} data={data || []} keyExtractor={(d) => d.id} emptyMessage="No devices registered yet." />
        )}
      </Card>

      <AddDeviceModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={refetch} />
    </div>
  );
};
