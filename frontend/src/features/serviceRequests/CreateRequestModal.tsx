import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/client';
import { Device } from '../../types';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../context/AuthContext';

interface CreateRequestModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const CreateRequestModal: React.FC<CreateRequestModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const { user } = useAuth();
  const [deviceId, setDeviceId] = useState<number | ''>('');
  const [problemTitle, setProblemTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'>('MEDIUM');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { data: devices } = useQuery<Device[]>({
    queryKey: ['my-devices', user?.customerId],
    queryFn: async () => {
      if (user?.customerId) {
        const res = await api.get(`/devices/customer/${user.customerId}`);
        return res.data.data;
      }
      return [];
    },
    enabled: isOpen && !!user?.customerId,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!deviceId) {
      setError('Please select a device');
      return;
    }

    setIsLoading(true);

    try {
      await api.post('/service-requests', {
        deviceId,
        problemTitle,
        description,
        priority,
      });
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to submit service request');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Submit Repair Service Request">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <div className="p-3 text-xs bg-red-50 text-red-600 rounded-lg border border-red-200">{error}</div>}

        <div className="space-y-1">
          <label className="block text-xs font-semibold text-slate-700">Select Your Registered Device</label>
          <select
            className="w-full px-3 py-2 text-sm bg-white border border-slate-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
            value={deviceId}
            onChange={(e) => setDeviceId(Number(e.target.value))}
            required
          >
            <option value="">-- Select Device --</option>
            {devices?.map((d) => (
              <option key={d.id} value={d.id}>
                {d.brand} {d.model} ({d.deviceType})
              </option>
            ))}
          </select>
        </div>

        <Input
          label="Problem Title"
          placeholder="e.g. Phone is not charging / Cracked Screen"
          value={problemTitle}
          onChange={(e) => setProblemTitle(e.target.value)}
          required
        />

        <div className="space-y-1">
          <label className="block text-xs font-semibold text-slate-700">Detailed Symptom Description</label>
          <textarea
            className="w-full px-3 py-2 text-sm bg-white border border-slate-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
            rows={3}
            placeholder="Describe what happens when issue occurs..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>

        <div className="space-y-1">
          <label className="block text-xs font-semibold text-slate-700">Priority Level</label>
          <select
            className="w-full px-3 py-2 text-sm bg-white border border-slate-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
            value={priority}
            onChange={(e) => setPriority(e.target.value as any)}
          >
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
            <option value="URGENT">URGENT</option>
          </select>
        </div>

        <div className="flex justify-end space-x-3 pt-4 border-t border-slate-100">
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            Submit Request
          </Button>
        </div>
      </form>
    </Modal>
  );
};
