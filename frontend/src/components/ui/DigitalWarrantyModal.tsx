import React from 'react';
import { Modal } from './Modal';
import { Button } from './Button';
import { ShieldCheck, Award, QrCode, CheckCircle2, Calendar, Printer } from 'lucide-react';

interface DigitalWarrantyModalProps {
  isOpen: boolean;
  onClose: () => void;
  jobNumber?: string;
  customerName?: string;
  deviceName?: string;
}

export const DigitalWarrantyModal: React.FC<DigitalWarrantyModalProps> = ({
  isOpen,
  onClose,
  jobNumber = 'RJ-684419',
  customerName = 'John Doe Customer',
  deviceName = 'Apple iPhone 15 Pro',
}) => {
  const warrantyId = 'WARR-' + Math.floor(100000 + Math.random() * 900000);
  const expiryDate = new Date();
  expiryDate.setDate(expiryDate.getDate() + 90);

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="🛡️ Official Digital Warranty Certificate" maxWidth="lg">
      <div className="space-y-6 font-sans">
        {/* Certificate Card */}
        <div className="bg-gradient-to-br from-slate-900 via-slate-800 to-blue-950 text-white p-6 rounded-2xl border border-amber-400/40 shadow-xl space-y-6 relative overflow-hidden">
          {/* Watermark badge */}
          <div className="absolute -right-6 -bottom-6 opacity-10 pointer-events-none">
            <Award className="w-48 h-48 text-amber-300" />
          </div>

          {/* Certificate Header */}
          <div className="flex justify-between items-start border-b border-slate-700/60 pb-4">
            <div>
              <div className="flex items-center space-x-2 text-amber-400">
                <ShieldCheck className="w-5 h-5" />
                <span className="text-xs font-bold uppercase tracking-widest">Smart Service Guarantee</span>
              </div>
              <h2 className="text-lg font-extrabold text-white mt-1">Digital Certificate of Repair Warranty</h2>
            </div>
            <span className="text-xs font-mono font-bold text-amber-400 bg-amber-400/10 px-2.5 py-1 rounded-md border border-amber-400/30">
              {warrantyId}
            </span>
          </div>

          {/* Guarantee Coverage Highlights */}
          <div className="grid grid-cols-2 gap-4 text-xs">
            <div className="bg-slate-800/80 p-3 rounded-xl border border-slate-700">
              <span className="text-slate-400 block font-medium">Covered Device</span>
              <span className="font-bold text-white text-sm block mt-0.5">{deviceName}</span>
            </div>
            <div className="bg-slate-800/80 p-3 rounded-xl border border-slate-700">
              <span className="text-slate-400 block font-medium">Registered Customer</span>
              <span className="font-bold text-white text-sm block mt-0.5">{customerName}</span>
            </div>
          </div>

          {/* Coverage Timeline & QR Code Simulation */}
          <div className="flex items-center justify-between bg-slate-800/40 p-4 rounded-xl border border-slate-700/40">
            <div className="space-y-1">
              <div className="flex items-center space-x-2 text-emerald-400 text-xs font-semibold">
                <CheckCircle2 className="w-4 h-4" />
                <span>90-Day Full Hardware Protection Active</span>
              </div>
              <div className="flex items-center space-x-2 text-slate-300 text-xs font-medium">
                <Calendar className="w-3.5 h-3.5 text-slate-400" />
                <span>Valid Until: {expiryDate.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })}</span>
              </div>
            </div>

            {/* QR Code Simulation */}
            <div className="bg-white p-2 rounded-lg text-slate-900 shadow-md text-center">
              <QrCode className="w-10 h-10 mx-auto text-slate-900" />
              <span className="text-[8px] font-mono font-bold block mt-0.5 text-slate-600">VERIFIED</span>
            </div>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex justify-between items-center pt-2">
          <Button variant="outline" size="sm" onClick={() => window.print()} icon={<Printer className="w-4 h-4" />}>
            Print Certificate
          </Button>
          <Button variant="primary" size="sm" onClick={onClose}>
            Done &amp; Close
          </Button>
        </div>
      </div>
    </Modal>
  );
};
