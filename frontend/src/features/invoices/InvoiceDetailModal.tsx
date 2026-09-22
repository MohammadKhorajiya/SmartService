import React, { useState } from 'react';
import api from '../../api/client';
import { Invoice } from '../../types';
import { Modal } from '../../components/ui/Modal';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { Button } from '../../components/ui/Button';
import { CreditCard, Printer, CheckCircle } from 'lucide-react';

interface InvoiceDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  invoice: Invoice | null;
}

export const InvoiceDetailModal: React.FC<InvoiceDetailModalProps> = ({ isOpen, onClose, onSuccess, invoice }) => {
  const [isProcessing, setIsProcessing] = useState(false);

  if (!invoice) return null;

  const handleSimulatePayment = async () => {
    setIsProcessing(true);
    try {
      // 1. Create order
      const orderRes = await api.post(`/payments/create-order/${invoice.id}`);
      const orderData = orderRes.data.data;

      // 2. Simulate Razorpay/UPI gateway verification
      await api.post('/payments/verify', {
        invoiceId: invoice.id,
        razorpayOrderId: orderData.orderId,
        razorpayPaymentId: 'pay_sim_' + Math.floor(Math.random() * 1000000),
        paymentMethod: 'ONLINE',
      });

      alert('Payment processed and verified successfully! Job completed & inventory deducted.');
      onSuccess();
      onClose();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Payment simulation failed');
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Tax Invoice — ${invoice.invoiceNumber}`} maxWidth="xl">
      <div className="space-y-6 font-sans">
        {/* Printable Header */}
        <div className="flex justify-between items-start border-b border-slate-200 pb-4">
          <div>
            <h2 className="text-xl font-extrabold text-slate-900">SMART SERVICE PLATFORM</h2>
            <p className="text-xs text-slate-500">100 Tech Park, Suite 500, Cyber City</p>
            <p className="text-xs text-slate-500">GSTIN: 27AAAAA0000A1Z5 | Support: +1 800 555 0199</p>
          </div>
          <div className="text-right">
            <span className="text-xs font-bold text-slate-400 block uppercase">Invoice No</span>
            <span className="text-sm font-bold text-blue-600 font-mono">{invoice.invoiceNumber}</span>
            <div className="mt-1">
              <StatusBadge status={invoice.paymentStatus} />
            </div>
          </div>
        </div>

        {/* Customer & Job Info */}
        <div className="grid grid-cols-2 gap-4 text-xs bg-slate-50 p-4 rounded-xl border border-slate-200">
          <div>
            <span className="font-semibold text-slate-400 uppercase tracking-wider block">Billed To</span>
            <div className="font-bold text-slate-900">{invoice.customerName}</div>
            <div className="text-slate-500">{invoice.customerEmail}</div>
            <div className="text-slate-500">{invoice.customerPhone}</div>
          </div>
          <div>
            <span className="font-semibold text-slate-400 uppercase tracking-wider block">Service Item</span>
            <div className="font-bold text-slate-900">{invoice.deviceBrand} {invoice.deviceModel}</div>
            <div className="text-slate-500">Repair Job #: {invoice.jobNumber}</div>
            <div className="text-slate-500">Date & Time: {new Date(invoice.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })} {new Date(invoice.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true })}</div>
          </div>
        </div>

        {/* Line Items Table */}
        <div className="space-y-2">
          <h4 className="font-bold text-slate-900 text-xs uppercase tracking-wider">Itemized Billing Breakdown</h4>
          <table className="w-full text-left text-xs border border-slate-200 rounded-lg overflow-hidden">
            <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200">
              <tr>
                <th className="p-2.5">Description</th>
                <th className="p-2.5 text-center">Qty</th>
                <th className="p-2.5 text-right">Unit Price</th>
                <th className="p-2.5 text-right">Total Price</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {invoice.items.map((it) => (
                <tr key={it.id}>
                  <td className="p-2.5 font-medium text-slate-800">{it.description}</td>
                  <td className="p-2.5 text-center">{it.quantity}</td>
                  <td className="p-2.5 text-right font-mono">₹{it.unitPrice}</td>
                  <td className="p-2.5 text-right font-mono font-semibold">₹{it.totalPrice}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Total Summary */}
        <div className="flex justify-end">
          <div className="w-64 space-y-1.5 text-xs text-slate-700">
            <div className="flex justify-between">
              <span>Subtotal:</span>
              <span className="font-mono">₹{invoice.subtotal}</span>
            </div>
            <div className="flex justify-between">
              <span>GST Tax (18%):</span>
              <span className="font-mono">₹{invoice.taxAmount}</span>
            </div>
            <div className="flex justify-between font-bold text-sm text-slate-900 pt-2 border-t border-slate-200">
              <span>Total Payable Amount:</span>
              <span className="text-blue-600 font-mono">₹{invoice.totalAmount}</span>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex justify-between items-center pt-4 border-t border-slate-100">
          <Button variant="outline" size="sm" onClick={() => window.print()} icon={<Printer className="w-4 h-4" />}>
            Print Invoice
          </Button>

          {invoice.paymentStatus !== 'PAID' ? (
            <Button variant="primary" isLoading={isProcessing} onClick={handleSimulatePayment} icon={<CreditCard className="w-4 h-4" />}>
              Pay Online (Simulate Gateway)
            </Button>
          ) : (
            <div className="flex items-center space-x-1.5 text-emerald-600 font-semibold text-xs bg-emerald-50 px-3 py-1.5 rounded-lg border border-emerald-200">
              <CheckCircle className="w-4 h-4" />
              <span>Paid &amp; Verified</span>
            </div>
          )}
        </div>
      </div>
    </Modal>
  );
};
