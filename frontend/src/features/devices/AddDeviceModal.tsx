import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/client';
import { Customer } from '../../types';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../context/AuthContext';

interface AddDeviceModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const AddDeviceModal: React.FC<AddDeviceModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const { user } = useAuth();
  const [customerId, setCustomerId] = useState<number | ''>(user?.customerId || '');
  const [brand, setBrand] = useState('');
  const [model, setModel] = useState('');
  const [serialNumber, setSerialNumber] = useState('');
  const [imei, setImei] = useState('');
  const [deviceType, setDeviceType] = useState('Mobile');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { data: customers } = useQuery<Customer[]>({
    queryKey: ['all-customers'],
    queryFn: async () => {
      const res = await api.get('/customers?size=100');
      return res.data.data.content;
    },
    enabled: user?.role !== 'CUSTOMER',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const targetCustId = user?.role === 'CUSTOMER' ? user.customerId : customerId;
    if (!targetCustId) {
      setError('Please select a customer owner');
      return;
    }

    setIsLoading(true);

    try {
      await api.post('/devices', {
        customerId: targetCustId,
        brand,
        model,
        serialNumber,
        imei,
        deviceType,
      });
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to register device');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Register Customer Device">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <div className="p-3 text-xs bg-red-50 text-red-600 rounded-lg border border-red-200">{error}</div>}

        {user?.role !== 'CUSTOMER' && (
          <div className="space-y-1">
            <label className="block text-xs font-semibold text-slate-700">Customer Owner</label>
            <select
              className="w-full px-3 py-2 text-sm bg-white border border-slate-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
              value={customerId}
              onChange={(e) => setCustomerId(Number(e.target.value))}
              required
            >
              <option value="">-- Select Customer --</option>
              {customers?.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name} ({c.email})
                </option>
              ))}
            </select>
          </div>
        )}

        <div className="grid grid-cols-2 gap-4">
          <Input label="Brand" placeholder="e.g. Apple / Dell" value={brand} onChange={(e) => setBrand(e.target.value)} required />
          <Input label="Model" placeholder="e.g. iPhone 15 / XPS 15" value={model} onChange={(e) => setModel(e.target.value)} required />
        </div>

        <div className="space-y-1">
          <label className="block text-xs font-semibold text-slate-700">Device Type</label>
          <select
            className="w-full px-3 py-2 text-sm bg-white border border-slate-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
            value={deviceType}
            onChange={(e) => setDeviceType(e.target.value)}
          >
            <option value="Mobile">Mobile Phone</option>
            <option value="Laptop">Laptop</option>
            <option value="Tablet">Tablet</option>
            <option value="Desktop">Desktop PC</option>
            <option value="Smartwatch">Smartwatch</option>
            <option value="Other">Other Electronics</option>
          </select>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Input label="Serial Number (Optional)" placeholder="SN-998822" value={serialNumber} onChange={(e) => setSerialNumber(e.target.value)} />
          <Input label="IMEI Number (Optional)" placeholder="356891100223344" value={imei} onChange={(e) => setImei(e.target.value)} />
        </div>

        <div className="flex justify-end space-x-3 pt-4 border-t border-slate-100">
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            Register Device
          </Button>
        </div>
      </form>
    </Modal>
  );
};
