import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/client';
import { PageResponse, Part } from '../../types';
import { Card } from '../../components/ui/Card';
import { Table } from '../../components/ui/Table';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Spinner } from '../../components/ui/Spinner';
import { ErrorState } from '../../components/ui/ErrorState';
import { PartModal } from './PartModal';
import { ExportCSVButton } from '../../components/ui/ExportCSVButton';
import { Package, Plus, AlertTriangle } from 'lucide-react';

export const InventoryList: React.FC = () => {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const { data, isLoading, error, refetch } = useQuery<PageResponse<Part>>({
    queryKey: ['inventory-parts', search, page],
    queryFn: async () => {
      const res = await api.get('/inventory/parts', { params: { query: search, page, size: 10 } });
      return res.data.data;
    },
  });

  const columns = [
    {
      header: 'Part Name & SKU',
      accessor: (p: Part) => (
        <div>
          <div className="font-semibold text-slate-900">{p.name}</div>
          <div className="text-xs text-blue-600 font-mono">{p.sku}</div>
        </div>
      ),
    },
    { header: 'Category', accessor: (p: Part) => <Badge variant="gray">{p.category}</Badge> },
    { header: 'Compatible Device', accessor: (p: Part) => <span className="text-slate-600 text-xs">{p.compatibleDevice || 'Universal'}</span> },
    { header: 'Cost Price', accessor: (p: Part) => <span className="text-slate-500 font-mono text-xs">₹{p.purchasePrice}</span> },
    { header: 'Selling Price', accessor: (p: Part) => <span className="font-bold text-slate-900 font-mono text-xs">₹{p.sellingPrice}</span> },
    {
      header: 'Physical Stock',
      accessor: (p: Part) => (
        <div className="flex items-center space-x-2">
          <span className="font-bold text-slate-900">{p.quantityInStock}</span>
          {p.quantityInStock <= p.reorderLevel && (
            <span className="inline-flex items-center space-x-1 bg-amber-50 text-amber-700 border border-amber-200 px-1.5 py-0.5 rounded-md text-[10px] font-bold">
              <AlertTriangle className="w-3 h-3 text-amber-600" />
              <span>Low Stock</span>
            </span>
          )}
        </div>
      ),
    },
    {
      header: 'Reserved / Available',
      accessor: (p: Part) => (
        <div className="text-xs">
          <span className="text-amber-600 font-semibold">{p.reservedQuantity} reserved</span> /{' '}
          <span className="text-emerald-600 font-semibold">{p.availableQuantity} available</span>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6 font-sans">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Spare Parts &amp; Inventory</h1>
          <p className="text-xs text-slate-500 mt-1">Real-time stock management, optimistic locks &amp; reservations</p>
        </div>
        <div className="flex items-center space-x-2">
          <ExportCSVButton
            data={data?.content || []}
            filename="inventory_parts"
            headers={[
              { label: 'SKU', key: 'sku' },
              { label: 'Part Name', key: 'name' },
              { label: 'Category', key: 'category' },
              { label: 'Compatible Device', key: (p) => p.compatibleDevice || 'Universal' },
              { label: 'Purchase Price (INR)', key: 'purchasePrice' },
              { label: 'Selling Price (INR)', key: 'sellingPrice' },
              { label: 'Physical Stock', key: 'quantityInStock' },
              { label: 'Available Stock', key: 'availableQuantity' },
            ]}
          />
          <Button variant="primary" icon={<Plus className="w-4 h-4" />} onClick={() => setIsModalOpen(true)}>
            Add Spare Part
          </Button>
        </div>
      </div>

      <Card>
        <div className="mb-4 max-w-md">
          <Input
            placeholder="Search by part name, SKU, or device..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
          />
        </div>

        {isLoading ? (
          <Spinner />
        ) : error ? (
          <ErrorState message="Failed to load inventory parts" onRetry={refetch} />
        ) : (
          <>
            <Table columns={columns} data={data?.content || []} keyExtractor={(p) => p.id} emptyMessage="No spare parts found in inventory." />
            {data && data.totalPages > 1 && (
              <div className="flex items-center justify-between pt-4 text-xs text-slate-500">
                <span>
                  Page {data.page + 1} of {data.totalPages} ({data.totalElements} total SKUs)
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

      <PartModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={refetch} />
    </div>
  );
};
