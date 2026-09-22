import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/client';
import { Customer, PageResponse } from '../../types';
import { Card } from '../../components/ui/Card';
import { Table } from '../../components/ui/Table';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Spinner } from '../../components/ui/Spinner';
import { ErrorState } from '../../components/ui/ErrorState';
import { CustomerModal } from './CustomerModal';
import { ExportCSVButton } from '../../components/ui/ExportCSVButton';
import { UserPlus, Search, Edit2 } from 'lucide-react';

export const CustomerList: React.FC = () => {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const { data, isLoading, error, refetch } = useQuery<PageResponse<Customer>>({
    queryKey: ['customers', search, page],
    queryFn: async () => {
      const res = await api.get('/customers', {
        params: { query: search, page, size: 10 },
      });
      return res.data.data;
    },
  });

  const columns = [
    {
      header: 'Customer Name',
      accessor: (c: Customer) => (
        <div>
          <div className="font-semibold text-slate-900">{c.name}</div>
          <div className="text-xs text-slate-500">ID: #{c.id}</div>
        </div>
      ),
    },
    { header: 'Email', accessor: (c: Customer) => <span className="text-slate-600">{c.email}</span> },
    { header: 'Phone', accessor: (c: Customer) => <span className="text-slate-600 font-mono text-xs">{c.phone}</span> },
    { header: 'Address', accessor: (c: Customer) => <span className="text-slate-500 text-xs truncate max-w-xs block">{c.address || 'N/A'}</span> },
    {
      header: 'Actions',
      accessor: (c: Customer) => (
        <Button
          variant="ghost"
          size="sm"
          onClick={() => {
            setSelectedCustomer(c);
            setIsModalOpen(true);
          }}
          icon={<Edit2 className="w-3.5 h-3.5" />}
        >
          Edit
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-6 font-sans">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Customers Directory</h1>
          <p className="text-xs text-slate-500 mt-1">Manage registered customer profiles and details</p>
        </div>
        <div className="flex items-center space-x-2">
          <ExportCSVButton
            data={data?.content || []}
            filename="customers_directory"
            headers={[
              { label: 'Customer ID', key: 'id' },
              { label: 'Name', key: 'name' },
              { label: 'Email', key: 'email' },
              { label: 'Phone', key: 'phone' },
              { label: 'Address', key: (c) => c.address || 'N/A' },
            ]}
          />
          <Button
            variant="primary"
            icon={<UserPlus className="w-4 h-4" />}
            onClick={() => {
              setSelectedCustomer(null);
              setIsModalOpen(true);
            }}
          >
            Add Customer
          </Button>
        </div>
      </div>

      <Card>
        <div className="mb-4 max-w-md">
          <Input
            placeholder="Search by name, email, or phone..."
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
          <ErrorState message="Failed to load customers" onRetry={refetch} />
        ) : (
          <>
            <Table columns={columns} data={data?.content || []} keyExtractor={(c) => c.id} emptyMessage="No customers registered yet." />
            {data && data.totalPages > 1 && (
              <div className="flex items-center justify-between pt-4 text-xs text-slate-500">
                <span>
                  Page {data.page + 1} of {data.totalPages} ({data.totalElements} total customers)
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

      <CustomerModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={refetch}
        customer={selectedCustomer}
      />
    </div>
  );
};
