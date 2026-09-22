import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/client';
import { Invoice, PageResponse } from '../../types';
import { Card } from '../../components/ui/Card';
import { Table } from '../../components/ui/Table';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { Spinner } from '../../components/ui/Spinner';
import { ErrorState } from '../../components/ui/ErrorState';
import { InvoiceDetailModal } from './InvoiceDetailModal';
import { ExportCSVButton } from '../../components/ui/ExportCSVButton';
import { FileText, Eye, CreditCard } from 'lucide-react';

export const InvoiceList: React.FC = () => {
  const [page, setPage] = useState(0);
  const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const { data, isLoading, error, refetch } = useQuery<PageResponse<Invoice>>({
    queryKey: ['invoices', page],
    queryFn: async () => {
      const res = await api.get('/invoices', { params: { page, size: 10 } });
      return res.data.data;
    },
  });

  const columns = [
    {
      header: 'Invoice Number',
      accessor: (inv: Invoice) => (
        <div>
          <div className="font-bold text-blue-600 font-mono text-xs">{inv.invoiceNumber}</div>
          <div className="text-xs text-slate-500">Job: {inv.jobNumber}</div>
        </div>
      ),
    },
    { header: 'Customer', accessor: (inv: Invoice) => <span className="font-semibold text-slate-900">{inv.customerName}</span> },
    { header: 'Device', accessor: (inv: Invoice) => <span className="text-slate-600 text-xs">{inv.deviceBrand} {inv.deviceModel}</span> },
    { header: 'Total Amount', accessor: (inv: Invoice) => <span className="font-extrabold text-slate-900 font-mono">₹{inv.totalAmount}</span> },
    { header: 'Payment Status', accessor: (inv: Invoice) => <StatusBadge status={inv.paymentStatus} /> },
    {
      header: 'Issued Date & Time',
      accessor: (inv: Invoice) => (
        <div className="text-slate-600 text-xs font-medium">
          <div>{new Date(inv.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })}</div>
          <div className="text-[11px] text-slate-400 font-mono">{new Date(inv.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true })}</div>
        </div>
      ),
    },
    {
      header: 'Actions',
      accessor: (inv: Invoice) => (
        <Button
          variant="outline"
          size="sm"
          onClick={() => {
            setSelectedInvoice(inv);
            setIsModalOpen(true);
          }}
          icon={<Eye className="w-3.5 h-3.5" />}
        >
          View &amp; Pay
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-6 font-sans">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Invoices &amp; Billing</h1>
          <p className="text-xs text-slate-500 mt-1">Customer invoices, online payments, and GST compliance</p>
        </div>
        <div className="flex items-center space-x-2">
          <ExportCSVButton
            data={data?.content || []}
            filename="invoices_and_billing"
            headers={[
              { label: 'Invoice #', key: 'invoiceNumber' },
              { label: 'Customer', key: 'customerName' },
              { label: 'Device', key: (inv) => `${inv.deviceBrand} ${inv.deviceModel}` },
              { label: 'Total Amount (INR)', key: 'totalAmount' },
              { label: 'Payment Status', key: 'paymentStatus' },
              { label: 'Issued Date & Time', key: (inv) => new Date(inv.createdAt).toLocaleString() },
            ]}
          />
        </div>
      </div>

      <Card>
        {isLoading ? (
          <Spinner />
        ) : error ? (
          <ErrorState message="Failed to load invoices" onRetry={refetch} />
        ) : (
          <>
            <Table columns={columns} data={data?.content || []} keyExtractor={(inv) => inv.id} emptyMessage="No invoices generated yet." />
            {data && data.totalPages > 1 && (
              <div className="flex items-center justify-between pt-4 text-xs text-slate-500">
                <span>
                  Page {data.page + 1} of {data.totalPages} ({data.totalElements} total invoices)
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

      <InvoiceDetailModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={refetch} invoice={selectedInvoice} />
    </div>
  );
};
