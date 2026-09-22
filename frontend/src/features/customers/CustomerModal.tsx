import React, { useState, useEffect } from 'react';
import { Customer } from '../../types';
import api from '../../api/client';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';

interface CustomerModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  customer?: Customer | null;
}

export const CustomerModal: React.FC<CustomerModalProps> = ({ isOpen, onClose, onSuccess, customer }) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (customer) {
      setName(customer.name);
      setEmail(customer.email);
      setPhone(customer.phone);
      setAddress(customer.address || '');
    } else {
      setName('');
      setEmail('');
      setPhone('');
      setAddress('');
    }
    setError('');
  }, [customer, isOpen]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      if (customer) {
        await api.put(`/customers/${customer.id}`, { name, email, phone, address });
      } else {
        await api.post('/customers', { name, email, phone, address });
      }
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save customer record');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={customer ? 'Edit Customer Profile' : 'Add New Customer'}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <div className="p-3 text-xs bg-red-50 text-red-600 rounded-lg border border-red-200">{error}</div>}

        <Input label="Full Name" placeholder="Jane Doe" value={name} onChange={(e) => setName(e.target.value)} required />
        <Input label="Email Address" type="email" placeholder="jane@example.com" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <Input label="Phone Number" type="tel" placeholder="+1 800 555 0199" value={phone} onChange={(e) => setPhone(e.target.value)} required />
        <Input label="Address" placeholder="123 Business St, Suite 400" value={address} onChange={(e) => setAddress(e.target.value)} />

        <div className="flex justify-end space-x-3 pt-4 border-t border-slate-100">
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            {customer ? 'Update Profile' : 'Save Customer'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
