import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/client';
import { PageResponse, ServiceRequest } from '../../types';
import { Card } from '../../components/ui/Card';
import { Table } from '../../components/ui/Table';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { Spinner } from '../../components/ui/Spinner';
import { ErrorState } from '../../components/ui/ErrorState';
import { CreateRequestModal } from './CreateRequestModal';
import { useAuth } from '../../context/AuthContext';
import { FileText, Plus, ArrowRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const ServiceRequestList: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [page, setPage] = useState(0);

  const { data, isLoading, error, refetch } = useQuery<PageResponse<ServiceRequest>>({
    queryKey: ['service-requests', page],
    queryFn: async () => {
      const res = await api.get('/service-requests', { params: { page, size: 10 } });
      return res.data.data;
    },
  });

  const handleConvertToJob = async (sr: ServiceRequest) => {
    try {
      await api.post('/repair-jobs', {
        serviceRequestId: sr.id,
        customerId: sr.customerId,
        deviceId: sr.deviceId,
        priority: sr.priority,
      });
      refetch();
      navigate('/repair-jobs');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to convert request to job');
    }
  };

  const columns = [
    {
      header: 'Request Info',
      accessor: (sr: ServiceRequest) => (
        <div>
          <div className="font-semibold text-slate-900">{sr.problemTitle}</div>
          <div className="text-xs text-slate-500">{sr.deviceBrand} {sr.deviceModel}</div>
        </div>
      ),
    },
    { header: 'Customer', accessor: (sr: ServiceRequest) => <span className="text-slate-700 font-medium">{sr.customerName}</span> },
    { header: 'Priority', accessor: (sr: ServiceRequest) => <StatusBadge status={sr.priority} /> },
    { header: 'Status', accessor: (sr: ServiceRequest) => <StatusBadge status={sr.status} /> },
    {
      header: 'Requested Date & Time',
      accessor: (sr: ServiceRequest) => (
        <div className="text-slate-600 text-xs font-medium">
          <div>{new Date(sr.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })}</div>
          <div className="text-[11px] text-slate-400 font-mono">{new Date(sr.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true })}</div>
        </div>
      ),
    },
    {
      header: 'Actions',
      accessor: (sr: ServiceRequest) =>
        user?.role !== 'CUSTOMER' && sr.status === 'REQUESTED' ? (
          <Button
            variant="primary"
            size="sm"
            onClick={() => handleConvertToJob(sr)}
            icon={<ArrowRight className="w-3.5 h-3.5" />}
          >
            Convert to Repair Job
          </Button>
        ) : null,
    },
  ];

  return (
    <div className="space-y-6 font-sans">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Service Requests</h1>
          <p className="text-xs text-slate-500 mt-1">Customer reported device problems and repair requests</p>
        </div>
        {user?.role === 'CUSTOMER' && (
          <Button variant="primary" icon={<Plus className="w-4 h-4" />} onClick={() => setIsModalOpen(true)}>
            New Request
          </Button>
        )}
      </div>

      <Card>
        {isLoading ? (
          <Spinner />
        ) : error ? (
          <ErrorState message="Failed to load service requests" onRetry={refetch} />
        ) : (
          <Table columns={columns} data={data?.content || []} keyExtractor={(sr) => sr.id} emptyMessage="No service requests found." />
        )}
      </Card>

      <CreateRequestModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={refetch} />
    </div>
  );
};
