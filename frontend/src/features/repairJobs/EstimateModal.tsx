import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/client';
import { Part, RepairJob } from '../../types';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Plus, Trash2 } from 'lucide-react';

interface EstimateModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  job: RepairJob | null;
}

interface ItemRow {
  partId?: number;
  itemDescription: string;
  quantity: number;
  unitPrice: number;
}

export const EstimateModal: React.FC<EstimateModalProps> = ({ isOpen, onClose, onSuccess, job }) => {
  const [laborCost, setLaborCost] = useState<number>(500);
  const [items, setItems] = useState<ItemRow[]>([{ itemDescription: '', quantity: 1, unitPrice: 0 }]);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { data: parts } = useQuery<Part[]>({
    queryKey: ['available-parts'],
    queryFn: async () => {
      const res = await api.get('/inventory/parts?size=100');
      return res.data.data.content;
    },
    enabled: isOpen,
  });

  const handleSelectPart = (index: number, partIdStr: string) => {
    const pId = Number(partIdStr);
    const selectedPart = parts?.find((p) => p.id === pId);
    const updated = [...items];
    if (selectedPart) {
      updated[index] = {
        partId: selectedPart.id,
        itemDescription: selectedPart.name,
        quantity: 1,
        unitPrice: selectedPart.sellingPrice,
      };
    } else {
      updated[index].partId = undefined;
    }
    setItems(updated);
  };

  const handleAddItem = () => {
    setItems([...items, { itemDescription: '', quantity: 1, unitPrice: 0 }]);
  };

  const handleRemoveItem = (idx: number) => {
    setItems(items.filter((_, i) => i !== idx));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!job) return;
    setIsLoading(true);
    setError('');

    try {
      await api.post('/estimates', {
        repairJobId: job.id,
        laborCost,
        taxRatePercentage: 18,
        items,
      });
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create estimate');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Create Cost Estimate — ${job?.jobNumber}`} maxWidth="xl">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <div className="p-3 text-xs bg-red-50 text-red-600 rounded-lg border border-red-200">{error}</div>}

        <Input label="Labor & Technical Service Charge (₹)" type="number" value={laborCost} onChange={(e) => setLaborCost(Number(e.target.value))} required />

        <div className="space-y-2 pt-2">
          <div className="flex items-center justify-between">
            <label className="text-xs font-semibold text-slate-700">Spare Parts &amp; Consumables Breakdown</label>
            <Button type="button" variant="outline" size="sm" icon={<Plus className="w-3.5 h-3.5" />} onClick={handleAddItem}>
              Add Line Item
            </Button>
          </div>

          {items.map((row, idx) => (
            <div key={idx} className="flex items-center space-x-2 bg-slate-50 p-2.5 rounded-lg border border-slate-200 text-xs">
              <select
                className="w-1/3 px-2 py-1.5 bg-white border border-slate-300 rounded-md text-xs focus:ring-2 focus:ring-blue-500 focus:outline-none"
                value={row.partId || ''}
                onChange={(e) => handleSelectPart(idx, e.target.value)}
              >
                <option value="">-- Custom Item --</option>
                {parts?.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name} (₹{p.sellingPrice})
                  </option>
                ))}
              </select>

              <input
                className="w-1/3 px-2 py-1.5 bg-white border border-slate-300 rounded-md text-xs"
                placeholder="Description"
                value={row.itemDescription}
                onChange={(e) => {
                  const updated = [...items];
                  updated[idx].itemDescription = e.target.value;
                  setItems(updated);
                }}
                required
              />

              <input
                className="w-16 px-2 py-1.5 bg-white border border-slate-300 rounded-md text-xs text-center"
                type="number"
                min={1}
                value={row.quantity}
                onChange={(e) => {
                  const updated = [...items];
                  updated[idx].quantity = Number(e.target.value);
                  setItems(updated);
                }}
              />

              <input
                className="w-24 px-2 py-1.5 bg-white border border-slate-300 rounded-md text-xs"
                type="number"
                placeholder="Unit Price ₹"
                value={row.unitPrice}
                onChange={(e) => {
                  const updated = [...items];
                  updated[idx].unitPrice = Number(e.target.value);
                  setItems(updated);
                }}
              />

              {items.length > 1 && (
                <button type="button" onClick={() => handleRemoveItem(idx)} className="p-1 text-red-500 hover:bg-red-50 rounded-md">
                  <Trash2 className="w-4 h-4" />
                </button>
              )}
            </div>
          ))}
        </div>

        <div className="flex justify-end space-x-3 pt-4 border-t border-slate-100">
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            Generate &amp; Send Estimate
          </Button>
        </div>
      </form>
    </Modal>
  );
};
