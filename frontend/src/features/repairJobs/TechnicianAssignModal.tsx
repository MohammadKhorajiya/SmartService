import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/client';
import { RepairJob } from '../../types';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';

interface TechnicianAssignModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  job: RepairJob | null;
}

export const TechnicianAssignModal: React.FC<TechnicianAssignModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  job,
}) => {
  const [technicianId, setTechnicianId] = useState<number | ''>('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const { data: technicians } = useQuery({
    queryKey: ['technicians'],
    queryFn: async () => {
      // Mock / fetch list of technicians
      return [
        { id: 1, name: 'Alex Technician', email: 'tech.alex@smartservice.com', specialization: 'Mobile & Laptop Logic Boards' },
        { id: 2, name: 'Sarah Engineer', email: 'tech.sarah@smartservice.com', specialization: 'Display & Battery Replacement' },
      ];
    },
    enabled: isOpen,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!job || !technicianId) return;

    setIsLoading(true);
    setError('');

    try {
      await api.post(`/repair-jobs/${job.id}/assign-technician`, { technicianId });
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to assign technician');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Assign Technician — ${job?.jobNumber}`}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <div className="p-3 text-xs bg-red-50 text-red-600 rounded-lg border border-red-200">{error}</div>}

        <div className="space-y-1">
          <label className="block text-xs font-semibold text-slate-700">Select Assignee Technician</label>
          <select
            className="w-full px-3 py-2 text-sm bg-white border border-slate-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
            value={technicianId}
            onChange={(e) => setTechnicianId(Number(e.target.value))}
            required
          >
            <option value="">-- Select Technician --</option>
            {technicians?.map((t) => (
              <option key={t.id} value={t.id}>
                {t.name} ({t.specialization})
              </option>
            ))}
          </select>
        </div>

        <div className="flex justify-end space-x-3 pt-4 border-t border-slate-100">
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            Assign Technician
          </Button>
        </div>
      </form>
    </Modal>
  );
};
