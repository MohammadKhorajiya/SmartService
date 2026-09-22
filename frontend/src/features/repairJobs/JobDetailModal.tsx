import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/client';
import { RepairJob, Diagnosis, Estimate, RepairJobStatusHistory } from '../../types';
import { Modal } from '../../components/ui/Modal';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../context/AuthContext';
import { RepairTrackerWidget } from '../../components/ui/RepairTrackerWidget';
import { CheckCircle2, Clock, ShieldCheck, AlertCircle, Receipt } from 'lucide-react';

interface JobDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  job: RepairJob | null;
}

export const JobDetailModal: React.FC<JobDetailModalProps> = ({ isOpen, onClose, onSuccess, job }) => {
  const { user } = useAuth();
  const [rejectReason, setRejectReason] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);

  const { data: diagnosis } = useQuery<Diagnosis>({
    queryKey: ['job-diagnosis', job?.id],
    queryFn: async () => {
      const res = await api.get(`/diagnoses/job/${job?.id}`);
      return res.data.data;
    },
    enabled: isOpen && !!job?.id,
  });

  const { data: estimate, refetch: refetchEstimate } = useQuery<Estimate>({
    queryKey: ['job-estimate', job?.id],
    queryFn: async () => {
      const res = await api.get(`/estimates/job/${job?.id}`);
      return res.data.data;
    },
    enabled: isOpen && !!job?.id,
  });

  const { data: history } = useQuery<RepairJobStatusHistory[]>({
    queryKey: ['job-history', job?.id],
    queryFn: async () => {
      const res = await api.get(`/repair-jobs/${job?.id}/history`);
      return res.data.data;
    },
    enabled: isOpen && !!job?.id,
  });

  const handleEstimateAction = async (approved: boolean) => {
    if (!estimate) return;
    setIsProcessing(true);

    try {
      await api.post(`/estimates/${estimate.id}/approval`, {
        approved,
        rejectionReason: approved ? undefined : rejectReason,
      });
      onSuccess();
      refetchEstimate();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Action failed');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleGenerateInvoice = async () => {
    if (!job) return;
    try {
      await api.post(`/invoices/job/${job.id}`);
      alert('Invoice generated successfully! View in Invoices & Billing tab.');
      onSuccess();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Invoice generation failed');
    }
  };

  if (!job) return null;

  const pipelineStages = [
    'REQUESTED',
    'ASSIGNED',
    'DIAGNOSING',
    'ESTIMATE_CREATED',
    'APPROVED',
    'IN_REPAIR',
    'READY_FOR_QC',
    'READY_FOR_PICKUP',
    'COMPLETED',
  ];

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Repair Job Details — ${job.jobNumber}`} maxWidth="2xl">
      <div className="space-y-6 font-sans">
        {/* Status Header */}
        <div className="bg-slate-50 p-4 rounded-xl border border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <div className="text-xs text-slate-500 font-medium">Job Status &amp; Priority</div>
            <div className="flex items-center space-x-2 mt-1">
              <StatusBadge status={job.status} />
              <StatusBadge status={job.priority} />
            </div>
          </div>

          {user?.role !== 'CUSTOMER' && (
            <Button variant="outline" size="sm" onClick={handleGenerateInvoice} icon={<Receipt className="w-3.5 h-3.5" />}>
              Generate Invoice
            </Button>
          )}
        </div>

        {/* Visual Progress Timeline */}
        <RepairTrackerWidget
          status={job.status}
          jobNumber={job.jobNumber}
          deviceInfo={`${job.deviceBrand} ${job.deviceModel}`}
        />

        {/* Pipeline Stepper */}
        <div className="py-2">
          <div className="text-xs font-semibold text-slate-700 mb-2">Repair Pipeline State</div>
          <div className="flex items-center justify-between border-t border-b border-slate-100 py-3 overflow-x-auto">
            {pipelineStages.map((stage, idx) => {
              const isCurrent = job.status === stage;
              return (
                <div key={stage} className="flex flex-col items-center min-w-[70px]">
                  <div
                    className={`w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold ${
                      isCurrent ? 'bg-blue-600 text-white shadow-md ring-2 ring-blue-200' : 'bg-slate-100 text-slate-400'
                    }`}
                  >
                    {idx + 1}
                  </div>
                  <span className={`text-[9px] font-medium mt-1 text-center truncate max-w-[65px] ${isCurrent ? 'text-blue-600 font-semibold' : 'text-slate-400'}`}>
                    {stage.replace(/_/g, ' ')}
                  </span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Customer & Device Grid */}
        <div className="grid grid-cols-2 gap-4 text-xs">
          <div className="bg-white p-3 rounded-lg border border-slate-200 space-y-1">
            <span className="font-semibold text-slate-400 uppercase tracking-wider block">Customer Info</span>
            <div className="font-bold text-slate-900">{job.customerName}</div>
            <div className="text-slate-500">{job.customerEmail}</div>
            <div className="text-slate-500">{job.customerPhone}</div>
          </div>

          <div className="bg-white p-3 rounded-lg border border-slate-200 space-y-1">
            <span className="font-semibold text-slate-400 uppercase tracking-wider block">Device Details</span>
            <div className="font-bold text-slate-900">{job.deviceBrand} {job.deviceModel}</div>
            <div className="text-slate-500">SN: {job.deviceSerialNumber || 'N/A'}</div>
            <div className="text-slate-500">Tech: {job.technicianName || 'Unassigned'}</div>
          </div>
        </div>

        {/* Diagnosis & Estimate Details */}
        {diagnosis && (
          <div className="bg-slate-50 p-4 rounded-xl border border-slate-200 text-xs space-y-2">
            <h4 className="font-bold text-slate-900 text-xs uppercase tracking-wider">Technician Diagnosis</h4>
            <p className="text-slate-700"><strong>Findings:</strong> {diagnosis.findings}</p>
            {diagnosis.recommendedRepair && <p className="text-slate-700"><strong>Recommended Repair:</strong> {diagnosis.recommendedRepair}</p>}
          </div>
        )}

        {estimate && (
          <div className="bg-white p-4 rounded-xl border border-blue-200 shadow-xs text-xs space-y-3">
            <div className="flex items-center justify-between border-b border-slate-100 pb-2">
              <div>
                <h4 className="font-bold text-slate-900">Repair Cost Estimate #{estimate.estimateNumber}</h4>
                <div className="text-[10px] text-slate-500">Tax included (18% GST)</div>
              </div>
              <StatusBadge status={estimate.status} />
            </div>

            <div className="space-y-1 divide-y divide-slate-100">
              {estimate.items.map((it) => (
                <div key={it.id} className="flex justify-between py-1 text-slate-700">
                  <span>{it.itemDescription} (x{it.quantity})</span>
                  <span className="font-semibold">₹{it.totalPrice}</span>
                </div>
              ))}
              <div className="flex justify-between py-1 text-slate-700">
                <span>Labor &amp; Service Fee</span>
                <span className="font-semibold">₹{estimate.laborCost}</span>
              </div>
              <div className="flex justify-between py-1 text-slate-700">
                <span>Estimated Tax</span>
                <span className="font-semibold">₹{estimate.taxAmount}</span>
              </div>
              <div className="flex justify-between pt-2 text-sm font-bold text-slate-900">
                <span>Total Estimated Cost</span>
                <span className="text-blue-600">₹{estimate.totalAmount}</span>
              </div>
            </div>

            {/* Customer Approval Action Bar */}
            {estimate.status === 'SENT' || estimate.status === 'DRAFT' ? (
              <div className="pt-3 border-t border-slate-100 space-y-2">
                <p className="text-xs font-semibold text-slate-800">Do you approve this estimate for repair execution?</p>
                <div className="flex items-center space-x-3">
                  <Button variant="primary" size="sm" isLoading={isProcessing} onClick={() => handleEstimateAction(true)}>
                    Approve &amp; Reserve Parts
                  </Button>
                  <Button
                    variant="danger"
                    size="sm"
                    isLoading={isProcessing}
                    onClick={() => {
                      const reason = prompt('Reason for rejection:');
                      if (reason) {
                        setRejectReason(reason);
                        handleEstimateAction(false);
                      }
                    }}
                  >
                    Reject Estimate
                  </Button>
                </div>
              </div>
            ) : null}
          </div>
        )}

        {/* History Audit Timeline */}
        {history && history.length > 0 && (
          <div className="space-y-2 text-xs">
            <h4 className="font-bold text-slate-900 uppercase tracking-wider">Job Audit Timeline</h4>
            <div className="space-y-2 max-h-40 overflow-y-auto pr-1">
              {history.map((h) => (
                <div key={h.id} className="p-2.5 bg-slate-50 rounded-lg border border-slate-100 flex items-start justify-between">
                  <div>
                    <span className="font-semibold text-slate-800">{h.changedByUserName}</span> changed status to{' '}
                    <span className="font-bold text-blue-600">{h.newStatus}</span>
                    {h.remarks && <p className="text-[11px] text-slate-500 mt-0.5">{h.remarks}</p>}
                  </div>
                  <span className="text-[10px] text-slate-400">{new Date(h.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </Modal>
  );
};
