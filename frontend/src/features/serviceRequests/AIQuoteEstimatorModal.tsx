import React, { useState } from 'react';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Sparkles, Clock, Wrench, ShieldCheck, ArrowRight, CheckCircle2, AlertTriangle, Zap } from 'lucide-react';
import api from '../../api/client';

interface AIQuoteEstimatorModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

const COMMON_ISSUES = [
  { id: 'screen', title: 'Cracked / Unresponsive Screen', avgTime: '1.5 Hours', basePrice: 2500, parts: ['OLED Display Assembly'], complexity: 'Medium' },
  { id: 'battery', title: 'Rapid Battery Drain / Degradation', avgTime: '45 Mins', basePrice: 950, parts: ['High-Capacity Li-Ion Battery'], complexity: 'Low' },
  { id: 'water', title: 'Liquid / Water Damage Exposure', avgTime: '4.0 Hours', basePrice: 3800, parts: ['Ultrasonic Cleaning', 'Corrosion Treatment'], complexity: 'High' },
  { id: 'charging', title: 'Charging Port / Loose Connector', avgTime: '1.0 Hour', basePrice: 800, parts: ['USB-C / Lightning Flex Cable'], complexity: 'Medium' },
  { id: 'motherboard', title: 'No Power / Board Chip Failure', avgTime: '5.0 Hours', basePrice: 4500, parts: ['Power IC Micro-Soldering'], complexity: 'Very High' },
];

export const AIQuoteEstimatorModal: React.FC<AIQuoteEstimatorModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const [selectedDevice, setSelectedDevice] = useState('iPhone 15 Pro');
  const [selectedIssueId, setSelectedIssueId] = useState('screen');
  const [isEmergencyPass, setIsEmergencyPass] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const currentIssue = COMMON_ISSUES.find((i) => i.id === selectedIssueId) || COMMON_ISSUES[0];
  const finalPrice = isEmergencyPass ? currentIssue.basePrice + 500 : currentIssue.basePrice;

  const handleBookEstimate = async () => {
    setIsSubmitting(true);
    try {
      // Simulate booking request with AI estimates
      await new Promise((res) => setTimeout(res, 800));
      alert(`AI Repair Estimate Created!\nDevice: ${selectedDevice}\nEstimated Cost: ₹${finalPrice}\nEstimated Time: ${currentIssue.avgTime}`);
      if (onSuccess) onSuccess();
      onClose();
    } catch (err) {
      alert('Failed to book AI estimate ticket.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="🤖 AI Smart Repair Cost & Time Estimator" maxWidth="2xl">
      <div className="space-y-6 font-sans">
        {/* Header Banner */}
        <div className="bg-gradient-to-r from-blue-900 via-indigo-900 to-slate-900 text-white p-5 rounded-2xl shadow-md flex items-center justify-between">
          <div>
            <div className="flex items-center space-x-2">
              <Sparkles className="w-5 h-5 text-amber-400 animate-spin" />
              <span className="text-xs font-bold uppercase tracking-wider text-amber-300">AI Diagnostic Intelligence</span>
            </div>
            <h3 className="text-base font-extrabold mt-1">Smart Instant Repair Quote Engine</h3>
            <p className="text-xs text-slate-300 mt-0.5">Calculates exact repair time, spare parts needed, and cost instantly</p>
          </div>
          <div className="bg-amber-400/20 text-amber-300 border border-amber-400/30 px-3 py-1.5 rounded-xl text-xs font-bold">
            98% Estimate Accuracy
          </div>
        </div>

        {/* Step 1: Select Device & Issue */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Target Device Model</label>
            <select
              value={selectedDevice}
              onChange={(e) => setSelectedDevice(e.target.value)}
              className="w-full text-xs font-medium bg-slate-50 border border-slate-300 rounded-lg p-2.5 text-slate-900 focus:ring-2 focus:ring-blue-500"
            >
              <option value="iPhone 15 Pro">Apple iPhone 15 Pro</option>
              <option value="iPhone 14 / 13">Apple iPhone 14 / 13</option>
              <option value="Samsung Galaxy S24 Ultra">Samsung Galaxy S24 Ultra</option>
              <option value="Dell XPS 15 Laptop">Dell XPS 15 9530 Laptop</option>
              <option value="MacBook Pro M2/M3">MacBook Pro M2 / M3</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Select Reported Issue</label>
            <select
              value={selectedIssueId}
              onChange={(e) => setSelectedIssueId(e.target.value)}
              className="w-full text-xs font-medium bg-slate-50 border border-slate-300 rounded-lg p-2.5 text-slate-900 focus:ring-2 focus:ring-blue-500"
            >
              {COMMON_ISSUES.map((issue) => (
                <option key={issue.id} value={issue.id}>
                  {issue.title}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Express Priority Pass Toggle */}
        <div className="bg-amber-50 border border-amber-200 rounded-xl p-3.5 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-amber-500 text-white rounded-lg">
              <Zap className="w-5 h-5 fill-current" />
            </div>
            <div>
              <span className="text-xs font-bold text-slate-900 block">VIP Express Repair Pass (+₹500)</span>
              <span className="text-[11px] text-slate-600">Bypasses queue for 1-hour priority technician inspection</span>
            </div>
          </div>
          <input
            type="checkbox"
            checked={isEmergencyPass}
            onChange={(e) => setIsEmergencyPass(e.target.checked)}
            className="w-4 h-4 text-blue-600 rounded border-slate-300 focus:ring-blue-500 cursor-pointer"
          />
        </div>

        {/* AI Estimation Result Card */}
        <div className="bg-slate-900 text-white p-5 rounded-2xl space-y-4 border border-slate-800 shadow-inner">
          <div className="flex items-center justify-between text-xs border-b border-slate-800 pb-3">
            <span className="text-slate-400 font-medium">AI Smart Assessment Result</span>
            <Badge variant="blue">Complexity: {currentIssue.complexity}</Badge>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-slate-800/60 p-3 rounded-xl border border-slate-700/50">
              <div className="flex items-center space-x-1.5 text-blue-400 text-xs font-medium mb-1">
                <Clock className="w-4 h-4" />
                <span>Est. Completion Time</span>
              </div>
              <span className="text-lg font-bold text-white">{currentIssue.avgTime}</span>
            </div>

            <div className="bg-slate-800/60 p-3 rounded-xl border border-slate-700/50">
              <div className="flex items-center space-x-1.5 text-emerald-400 text-xs font-medium mb-1">
                <Wrench className="w-4 h-4" />
                <span>Required Spare Parts</span>
              </div>
              <span className="text-xs font-semibold text-slate-200 block truncate">{currentIssue.parts.join(', ')}</span>
            </div>

            <div className="bg-slate-800/60 p-3 rounded-xl border border-slate-700/50">
              <div className="flex items-center space-x-1.5 text-amber-400 text-xs font-medium mb-1">
                <ShieldCheck className="w-4 h-4" />
                <span>Est. Repair Price</span>
              </div>
              <span className="text-xl font-extrabold text-amber-400 font-mono">₹{finalPrice}</span>
            </div>
          </div>

          <div className="flex items-center justify-between text-[11px] text-slate-400 pt-1">
            <div className="flex items-center space-x-1 text-emerald-400">
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>Includes 90-Day Official Digital Warranty Certificate</span>
            </div>
            <span>Tax GST (18%) calculated at checkout</span>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex justify-end space-x-3 pt-2">
          <Button variant="outline" size="sm" onClick={onClose}>
            Close
          </Button>
          <Button variant="primary" size="sm" isLoading={isSubmitting} onClick={handleBookEstimate} icon={<ArrowRight className="w-4 h-4" />}>
            Book Ticket at Estimated Price (₹{finalPrice})
          </Button>
        </div>
      </div>
    </Modal>
  );
};
