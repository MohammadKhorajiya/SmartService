import React from 'react';
import { RepairJobStatus } from '../../types';
import { CheckCircle2, Clock, Wrench, PackageCheck, AlertCircle } from 'lucide-react';

interface RepairTrackerWidgetProps {
  status: RepairJobStatus | string;
  jobNumber?: string;
  deviceInfo?: string;
}

export const RepairTrackerWidget: React.FC<RepairTrackerWidgetProps> = ({ status, jobNumber, deviceInfo }) => {
  const steps = [
    { key: 'DIAGNOSING', label: 'Received & Diagnosing', icon: Clock },
    { key: 'PENDING_APPROVAL', label: 'Quote / Estimate', icon: AlertCircle },
    { key: 'IN_REPAIR', label: 'Repairing Hardware', icon: Wrench },
    { key: 'READY_FOR_PICKUP', label: 'Ready for Pickup', icon: PackageCheck },
  ];

  const getActiveStepIndex = (st: string) => {
    switch (st) {
      case 'RECEIVED':
      case 'ASSIGNED':
      case 'DIAGNOSING':
        return 0;
      case 'PENDING_APPROVAL':
        return 1;
      case 'APPROVED':
      case 'IN_REPAIR':
        return 2;
      case 'READY_FOR_PICKUP':
      case 'COMPLETED':
      case 'DELIVERED':
        return 3;
      default:
        return 0;
    }
  };

  const currentIndex = getActiveStepIndex(status);

  return (
    <div className="bg-slate-900 text-white p-6 rounded-2xl shadow-md space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-800 pb-3">
        <div>
          <span className="text-[10px] font-bold text-blue-400 uppercase tracking-widest block">Live Repair Tracking</span>
          <h3 className="text-sm font-bold text-white mt-0.5">{deviceInfo || 'My Device Repair'}</h3>
        </div>
        {jobNumber && (
          <span className="font-mono text-xs text-blue-300 bg-blue-950 px-2.5 py-1 rounded-md border border-blue-800/50 self-start sm:self-auto">
            {jobNumber}
          </span>
        )}
      </div>

      {/* Visual Timeline Steps */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 pt-2">
        {steps.map((step, idx) => {
          const isDone = idx < currentIndex;
          const isCurrent = idx === currentIndex;
          const StepIcon = step.icon;

          return (
            <div
              key={step.key}
              className={`p-3 rounded-xl border transition-all ${
                isCurrent
                  ? 'bg-blue-600/20 border-blue-500 text-blue-300 ring-1 ring-blue-500/50'
                  : isDone
                  ? 'bg-slate-800/60 border-slate-700 text-emerald-400'
                  : 'bg-slate-800/30 border-slate-800/60 text-slate-500'
              }`}
            >
              <div className="flex items-center justify-between mb-2">
                <StepIcon className={`w-4 h-4 ${isCurrent ? 'text-blue-400 animate-pulse' : isDone ? 'text-emerald-400' : 'text-slate-600'}`} />
                {isDone ? (
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                ) : isCurrent ? (
                  <span className="inline-block w-2 h-2 rounded-full bg-blue-400 animate-ping" />
                ) : null}
              </div>
              <p className="text-[11px] font-bold tracking-tight leading-tight">{step.label}</p>
              <p className="text-[10px] opacity-75 mt-0.5 font-medium">{isDone ? 'Completed' : isCurrent ? 'In Progress' : 'Pending'}</p>
            </div>
          );
        })}
      </div>
    </div>
  );
};
