import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/client';
import { PageResponse, RepairJob, RepairJobStatus } from '../../types';
import { Card } from '../../components/ui/Card';
import { Table } from '../../components/ui/Table';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { Spinner } from '../../components/ui/Spinner';
import { ErrorState } from '../../components/ui/ErrorState';
import { useAuth } from '../../context/AuthContext';
import { TechnicianAssignModal } from './TechnicianAssignModal';
import { DiagnosisModal } from './DiagnosisModal';
import { EstimateModal } from './EstimateModal';
import { JobDetailModal } from './JobDetailModal';
import { Wrench, Eye, UserCheck, Stethoscope, Calculator, Play } from 'lucide-react';

export const RepairJobList: React.FC = () => {
  const { user } = useAuth();
  const [selectedStatus, setSelectedStatus] = useState<string>('');
  const [page, setPage] = useState(0);
  const [selectedJob, setSelectedJob] = useState<RepairJob | null>(null);
  const [activeModal, setActiveModal] = useState<'detail' | 'assign' | 'diagnosis' | 'estimate' | null>(null);

  const { data, isLoading, error, refetch } = useQuery<PageResponse<RepairJob>>({
    queryKey: ['repair-jobs', selectedStatus, page],
    queryFn: async () => {
      const res = await api.get('/repair-jobs', {
        params: { status: selectedStatus || undefined, page, size: 10 },
      });
      return res.data.data;
    },
  });

  const handleUpdateStatus = async (job: RepairJob, newStatus: RepairJobStatus) => {
    try {
      await api.patch(`/repair-jobs/${job.id}/status`, { newStatus });
      refetch();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Status update failed');
    }
  };

  const columns = [
    {
      header: 'Job ID & Device',
      accessor: (j: RepairJob) => (
        <div>
          <div className="font-bold text-blue-600 font-mono text-xs">{j.jobNumber}</div>
          <div className="text-sm font-medium text-slate-900">{j.deviceBrand} {j.deviceModel}</div>
        </div>
      ),
    },
    {
      header: 'Customer',
      accessor: (j: RepairJob) => (
        <div>
          <div className="font-semibold text-slate-900">{j.customerName}</div>
          <div className="text-xs text-slate-500">{j.customerPhone}</div>
        </div>
      ),
    },
    { header: 'Technician', accessor: (j: RepairJob) => <span className="text-slate-700 text-xs">{j.technicianName || 'Unassigned'}</span> },
    { header: 'Priority', accessor: (j: RepairJob) => <StatusBadge status={j.priority} /> },
    { header: 'Status Stage', accessor: (j: RepairJob) => <StatusBadge status={j.status} /> },
    {
      header: 'Date & Time',
      accessor: (j: RepairJob) => (
        <div className="text-slate-600 text-xs font-medium">
          <div>{new Date(j.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })}</div>
          <div className="text-[11px] text-slate-400 font-mono">{new Date(j.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true })}</div>
        </div>
      ),
    },
    {
      header: 'Actions',
      accessor: (j: RepairJob) => (
        <div className="flex items-center space-x-1.5">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setSelectedJob(j);
              setActiveModal('detail');
            }}
            icon={<Eye className="w-3.5 h-3.5" />}
          >
            View
          </Button>

          {user?.role === 'MANAGER' || user?.role === 'ADMIN' ? (
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                setSelectedJob(j);
                setActiveModal('assign');
              }}
              icon={<UserCheck className="w-3.5 h-3.5" />}
            >
              Assign
            </Button>
          ) : null}

          {(user?.role === 'TECHNICIAN' || user?.role === 'ADMIN' || user?.role === 'MANAGER') && (
            <>
              {j.status === 'ASSIGNED' || j.status === 'DIAGNOSING' ? (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setSelectedJob(j);
                    setActiveModal('diagnosis');
                  }}
                  icon={<Stethoscope className="w-3.5 h-3.5" />}
                >
                  Diagnose
                </Button>
              ) : null}

              {j.status === 'DIAGNOSING' || j.status === 'ESTIMATE_CREATED' ? (
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => {
                    setSelectedJob(j);
                    setActiveModal('estimate');
                  }}
                  icon={<Calculator className="w-3.5 h-3.5" />}
                >
                  Estimate
                </Button>
              ) : null}

              {j.status === 'APPROVED' || j.status === 'WAITING_FOR_PARTS' ? (
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => handleUpdateStatus(j, 'IN_REPAIR')}
                  icon={<Play className="w-3.5 h-3.5" />}
                >
                  Start Repair
                </Button>
              ) : null}

              {j.status === 'IN_REPAIR' ? (
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => handleUpdateStatus(j, 'READY_FOR_QC')}
                >
                  Submit for QC
                </Button>
              ) : null}

              {j.status === 'READY_FOR_QC' ? (
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => handleUpdateStatus(j, 'READY_FOR_PICKUP')}
                >
                  Pass QC
                </Button>
              ) : null}
            </>
          )}
        </div>
      ),
    },
  ];

  const statusFilters = [
    { label: 'All Jobs', value: '' },
    { label: 'Assigned', value: 'ASSIGNED' },
    { label: 'Diagnosing', value: 'DIAGNOSING' },
    { label: 'Pending Approval', value: 'WAITING_FOR_APPROVAL' },
    { label: 'Approved', value: 'APPROVED' },
    { label: 'In Repair', value: 'IN_REPAIR' },
    { label: 'Ready for Pickup', value: 'READY_FOR_PICKUP' },
    { label: 'Completed', value: 'COMPLETED' },
  ];

  return (
    <div className="space-y-6 font-sans">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Repair Jobs Pipeline</h1>
          <p className="text-xs text-slate-500 mt-1">End-to-end device repair tracking, diagnosis &amp; estimates</p>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center space-x-2 overflow-x-auto pb-2">
        {statusFilters.map((tab) => (
          <button
            key={tab.value}
            onClick={() => {
              setSelectedStatus(tab.value);
              setPage(0);
            }}
            className={`px-3.5 py-1.5 text-xs font-semibold rounded-lg whitespace-nowrap transition-colors ${
              selectedStatus === tab.value
                ? 'bg-blue-600 text-white shadow-xs'
                : 'bg-white text-slate-600 hover:bg-slate-100 border border-slate-200'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <Card>
        {isLoading ? (
          <Spinner />
        ) : error ? (
          <ErrorState message="Failed to load repair jobs" onRetry={refetch} />
        ) : (
          <>
            <Table columns={columns} data={data?.content || []} keyExtractor={(j) => j.id} emptyMessage="No repair jobs found in this stage." />
            {data && data.totalPages > 1 && (
              <div className="flex items-center justify-between pt-4 text-xs text-slate-500">
                <span>
                  Page {data.page + 1} of {data.totalPages} ({data.totalElements} total jobs)
                </span>
                <div className="flex space-x-2">
                  <Button variant="outline" size="sm" disabled={data.page === 0} onClick={() => setPage((p) => p - 1)}>
                    Previous
                  </Button>
                  <Button variant="outline" size="sm" disabled={data.last} onClick={() => setPage((p) => p + 1)}>
                    Next
                  </Button>
                </div>
              </div>
            )}
          </>
        )}
      </Card>

      <JobDetailModal isOpen={activeModal === 'detail'} onClose={() => setActiveModal(null)} onSuccess={refetch} job={selectedJob} />
      <TechnicianAssignModal isOpen={activeModal === 'assign'} onClose={() => setActiveModal(null)} onSuccess={refetch} job={selectedJob} />
      <DiagnosisModal isOpen={activeModal === 'diagnosis'} onClose={() => setActiveModal(null)} onSuccess={refetch} job={selectedJob} />
      <EstimateModal isOpen={activeModal === 'estimate'} onClose={() => setActiveModal(null)} onSuccess={refetch} job={selectedJob} />
    </div>
  );
};
