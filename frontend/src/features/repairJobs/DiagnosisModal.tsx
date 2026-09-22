import React, { useState } from 'react';
import api from '../../api/client';
import { RepairJob } from '../../types';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';

interface DiagnosisModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  job: RepairJob | null;
}

export const DiagnosisModal: React.FC<DiagnosisModalProps> = ({ isOpen, onClose, onSuccess, job }) => {
  const [symptoms, setSymptoms] = useState('');
  const [findings, setFindings] = useState('');
  const [recommendedRepair, setRecommendedRepair] = useState('');
  const [estimatedLaborCost, setEstimatedLaborCost] = useState<number | ''>(500);
  const [notes, setNotes] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!job) return;
    setIsLoading(true);
    setError('');

    try {
      await api.post('/diagnoses', {
        repairJobId: job.id,
        symptoms,
        findings,
        recommendedRepair,
        estimatedLaborCost: Number(estimatedLaborCost),
        notes,
      });
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save diagnosis');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Technician Diagnosis — ${job?.jobNumber}`}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <div className="p-3 text-xs bg-red-50 text-red-600 rounded-lg border border-red-200">{error}</div>}

        <Input label="Observed Symptoms" placeholder="e.g. No power / Intermittent charging" value={symptoms} onChange={(e) => setSymptoms(e.target.value)} />
        
        <div className="space-y-1">
          <label className="block text-xs font-semibold text-slate-700">Diagnosis Findings *</label>
          <textarea
            className="w-full px-3 py-2 text-sm bg-white border border-slate-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
            rows={3}
            placeholder="Detailed technical findings from hardware diagnostic check..."
            value={findings}
            onChange={(e) => setFindings(e.target.value)}
            required
          />
        </div>

        <Input label="Recommended Repair Action" placeholder="e.g. Replace charging IC chip & port assembly" value={recommendedRepair} onChange={(e) => setRecommendedRepair(e.target.value)} />
        <Input label="Estimated Labor Cost (₹)" type="number" value={estimatedLaborCost} onChange={(e) => setEstimatedLaborCost(Number(e.target.value))} required />

        <div className="flex justify-end space-x-3 pt-4 border-t border-slate-100">
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            Save Diagnosis
          </Button>
        </div>
      </form>
    </Modal>
  );
};
