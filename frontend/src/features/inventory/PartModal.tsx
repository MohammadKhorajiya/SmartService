import React, { useState } from 'react';
import api from '../../api/client';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';

interface PartModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const PartModal: React.FC<PartModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const [sku, setSku] = useState('');
  const [name, setName] = useState('');
  const [category, setCategory] = useState('Screen');
  const [compatibleDevice, setCompatibleDevice] = useState('');
  const [purchasePrice, setPurchasePrice] = useState<number | ''>(500);
  const [sellingPrice, setSellingPrice] = useState<number | ''>(1200);
  const [quantityInStock, setQuantityInStock] = useState<number | ''>(10);
  const [reorderLevel, setReorderLevel] = useState<number | ''>(5);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      await api.post('/inventory/parts', {
        sku,
        name,
        category,
        compatibleDevice,
        purchasePrice: Number(purchasePrice),
        sellingPrice: Number(sellingPrice),
        quantityInStock: Number(quantityInStock),
        reorderLevel: Number(reorderLevel),
      });
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to add spare part');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Add Spare Part SKU">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <div className="p-3 text-xs bg-red-50 text-red-600 rounded-lg border border-red-200">{error}</div>}

        <div className="grid grid-cols-2 gap-4">
          <Input label="SKU Code" placeholder="PART-IPH15-PORT" value={sku} onChange={(e) => setSku(e.target.value)} required />
          <div className="space-y-1">
            <label className="block text-xs font-semibold text-slate-700">Category</label>
            <select
              className="w-full px-3 py-2 text-sm bg-white border border-slate-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
            >
              <option value="Screen">Display / Screen</option>
              <option value="Battery">Battery</option>
              <option value="Charging Port">Charging Port</option>
              <option value="RAM">RAM / Memory</option>
              <option value="Storage">SSD / Storage</option>
              <option value="IC Chip">Logic Board IC</option>
              <option value="Other">Other Component</option>
            </select>
          </div>
        </div>

        <Input label="Part Name" placeholder="iPhone 15 OLED Display Original" value={name} onChange={(e) => setName(e.target.value)} required />
        <Input label="Compatible Devices" placeholder="iPhone 15 / 15 Pro" value={compatibleDevice} onChange={(e) => setCompatibleDevice(e.target.value)} />

        <div className="grid grid-cols-2 gap-4">
          <Input label="Cost Price ₹" type="number" value={purchasePrice} onChange={(e) => setPurchasePrice(Number(e.target.value))} required />
          <Input label="Selling Price ₹" type="number" value={sellingPrice} onChange={(e) => setSellingPrice(Number(e.target.value))} required />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Input label="Initial Quantity in Stock" type="number" value={quantityInStock} onChange={(e) => setQuantityInStock(Number(e.target.value))} required />
          <Input label="Low Stock Reorder Threshold" type="number" value={reorderLevel} onChange={(e) => setReorderLevel(Number(e.target.value))} required />
        </div>

        <div className="flex justify-end space-x-3 pt-4 border-t border-slate-100">
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            Save Part
          </Button>
        </div>
      </form>
    </Modal>
  );
};
