import React from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../../api/client';
import { DashboardSummary } from '../../types';
import { Card } from '../../components/ui/Card';
import { Spinner } from '../../components/ui/Spinner';
import { ErrorState } from '../../components/ui/ErrorState';
import { useAuth } from '../../context/AuthContext';
import {
  Users,
  Wrench,
  Clock,
  CheckCircle2,
  DollarSign,
  AlertTriangle,
  PlusCircle,
  TrendingUp,
} from 'lucide-react';
import { RepairTrackerWidget } from '../../components/ui/RepairTrackerWidget';
import { AIQuoteEstimatorModal } from '../serviceRequests/AIQuoteEstimatorModal';
import { DigitalWarrantyModal } from '../../components/ui/DigitalWarrantyModal';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';
import { Link } from 'react-router-dom';
import { Sparkles, ShieldCheck } from 'lucide-react';

export const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const [isAIEstimatorOpen, setIsAIEstimatorOpen] = React.useState(false);
  const [isWarrantyOpen, setIsWarrantyOpen] = React.useState(false);

  const { data, isLoading, error, refetch } = useQuery<DashboardSummary>({
    queryKey: ['dashboard-summary'],
    queryFn: async () => {
      const res = await api.get('/dashboard/summary');
      return res.data.data;
    },
  });

  if (isLoading) return <Spinner size="lg" />;
  if (error) return <ErrorState message="Failed to load dashboard statistics" onRetry={refetch} />;

  const summary = data || {
    totalCustomers: 0,
    activeRepairs: 0,
    pendingEstimates: 0,
    completedRepairs: 0,
    totalRevenue: 0,
    lowStockCount: 0,
    repairsByStatus: {},
  };

  const chartData = Object.entries(summary.repairsByStatus).map(([status, count]) => ({
    status: status.replace(/_/g, ' '),
    count,
  }));

  // CUSTOMER PORTAL VIEW
  if (user?.role === 'CUSTOMER') {
    return (
      <div className="space-y-8 font-sans">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
          <div>
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Customer Service Portal</h1>
            <p className="text-xs text-slate-500 mt-1">
              Welcome back, <span className="font-semibold text-slate-700">{user.fullName}</span>! Manage your repair requests and device trackings online.
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <button
              onClick={() => setIsAIEstimatorOpen(true)}
              className="inline-flex items-center space-x-1.5 min-h-[44px] bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 text-slate-950 text-xs font-bold px-3 py-2 rounded-xl shadow-sm transition-all cursor-pointer whitespace-nowrap"
              title="Get AI Smart Repair Quote"
            >
              <Sparkles className="w-3.5 h-3.5 fill-current animate-bounce" />
              <span>AI Quote</span>
            </button>
            <button
              onClick={() => setIsWarrantyOpen(true)}
              className="inline-flex items-center space-x-1.5 min-h-[44px] bg-slate-900 hover:bg-slate-800 text-amber-300 border border-slate-700/60 text-xs font-semibold px-3 py-2 rounded-xl shadow-sm transition-colors cursor-pointer whitespace-nowrap"
              title="View Digital Warranty Certificate"
            >
              <ShieldCheck className="w-3.5 h-3.5 text-amber-400" />
              <span>Digital Warranty</span>
            </button>
            <Link
              to="/service-requests"
              className="inline-flex items-center space-x-1.5 min-h-[44px] bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold px-3.5 py-2 rounded-xl shadow-sm transition-colors whitespace-nowrap"
            >
              <PlusCircle className="w-3.5 h-3.5" />
              <span>Create Request</span>
            </Link>
          </div>
        </div>

        {/* Customer Quick KPI Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          <Link to="/repair-jobs" className="block group">
            <Card className="group-hover:border-blue-400 transition-colors">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Active Repairs</p>
                  <h3 className="text-2xl font-bold text-slate-900 mt-1">{summary.activeRepairs}</h3>
                </div>
                <div className="p-3 bg-blue-50 text-blue-600 rounded-xl group-hover:bg-blue-600 group-hover:text-white transition-colors">
                  <Wrench className="w-6 h-6" />
                </div>
              </div>
            </Card>
          </Link>

          <Link to="/repair-jobs" className="block group">
            <Card className="group-hover:border-amber-400 transition-colors">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Pending Estimates</p>
                  <h3 className="text-2xl font-bold text-slate-900 mt-1">{summary.pendingEstimates}</h3>
                </div>
                <div className="p-3 bg-amber-50 text-amber-600 rounded-xl group-hover:bg-amber-600 group-hover:text-white transition-colors">
                  <Clock className="w-6 h-6" />
                </div>
              </div>
            </Card>
          </Link>

          <Link to="/repair-jobs" className="block group">
            <Card className="group-hover:border-emerald-400 transition-colors">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Completed Repairs</p>
                  <h3 className="text-2xl font-bold text-slate-900 mt-1">{summary.completedRepairs}</h3>
                </div>
                <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl group-hover:bg-emerald-600 group-hover:text-white transition-colors">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
              </div>
            </Card>
          </Link>

          <Link to="/devices" className="block group">
            <Card className="group-hover:border-indigo-400 transition-colors">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">My Devices</p>
                  <h3 className="text-2xl font-bold text-slate-900 mt-1">Manage</h3>
                </div>
                <div className="p-3 bg-indigo-50 text-indigo-600 rounded-xl group-hover:bg-indigo-600 group-hover:text-white transition-colors">
                  <Users className="w-6 h-6" />
                </div>
              </div>
            </Card>
          </Link>
        </div>

        {/* Live Repair Status Timeline Banner */}
        <RepairTrackerWidget
          status={summary.activeRepairs > 0 ? 'IN_REPAIR' : summary.pendingEstimates > 0 ? 'PENDING_APPROVAL' : 'COMPLETED'}
          deviceInfo="Live Device Tracking & Status Progress"
        />

        {/* Customer Help & Workflow Shortcuts */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card title="Need Device Repair?" subtitle="Log a new service ticket in seconds">
            <div className="space-y-4 pt-2">
              <p className="text-xs text-slate-600 leading-relaxed">
                Report broken screens, battery failures, water damage, or software issues directly to our service technicians.
              </p>
              <Link
                to="/service-requests"
                className="inline-flex items-center justify-center w-full bg-blue-50 text-blue-600 hover:bg-blue-100 text-xs font-semibold py-2.5 rounded-lg transition-colors"
              >
                Log New Request
              </Link>
            </div>
          </Card>

          <Card title="Track Live Status" subtitle="Check repair diagnosis and estimates">
            <div className="space-y-4 pt-2">
              <p className="text-xs text-slate-600 leading-relaxed">
                Stay informed with real-time updates as your equipment moves from inspection to repair and final pickup.
              </p>
              <Link
                to="/repair-jobs"
                className="inline-flex items-center justify-center w-full bg-slate-100 text-slate-700 hover:bg-slate-200 text-xs font-semibold py-2.5 rounded-lg transition-colors"
              >
                View Repair Pipeline
              </Link>
            </div>
          </Card>

          <Card title="My Invoices & Receipts" subtitle="Approve estimates & view invoices">
            <div className="space-y-4 pt-2">
              <p className="text-xs text-slate-600 leading-relaxed">
                Review transparent price breakdowns for parts and labor, approve repair quotes, and access billing records.
              </p>
              <Link
                to="/invoices"
                className="inline-flex items-center justify-center w-full bg-slate-100 text-slate-700 hover:bg-slate-200 text-xs font-semibold py-2.5 rounded-lg transition-colors"
              >
                View Invoices
              </Link>
            </div>
          </Card>
        </div>

        {/* Unique Feature Modals */}
        <AIQuoteEstimatorModal isOpen={isAIEstimatorOpen} onClose={() => setIsAIEstimatorOpen(false)} />
        <DigitalWarrantyModal isOpen={isWarrantyOpen} onClose={() => setIsWarrantyOpen(false)} customerName={user?.fullName} />
      </div>
    );
  }

  // ADMIN / MANAGER / STAFF / TECHNICIAN VIEW
  return (
    <div className="space-y-8 font-sans">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Admin Dashboard</h1>
          <p className="text-xs text-slate-500 mt-1">
            Welcome back, <span className="font-semibold text-slate-700">{user?.fullName}</span>! Operational &amp; Revenue Overview.
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <Link
            to="/repair-jobs"
            className="inline-flex items-center space-x-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold px-4 py-2 rounded-lg shadow-sm transition-colors"
          >
            <Wrench className="w-4 h-4" />
            <span>View Repair Jobs</span>
          </Link>
        </div>
      </div>

      {/* Admin KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <Card className="hover:border-blue-300 transition-colors">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Active Repairs</p>
              <h3 className="text-2xl font-bold text-slate-900 mt-1">{summary.activeRepairs}</h3>
            </div>
            <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
              <Wrench className="w-6 h-6" />
            </div>
          </div>
        </Card>

        <Card className="hover:border-amber-300 transition-colors">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Pending Estimates</p>
              <h3 className="text-2xl font-bold text-slate-900 mt-1">{summary.pendingEstimates}</h3>
            </div>
            <div className="p-3 bg-amber-50 text-amber-600 rounded-xl">
              <Clock className="w-6 h-6" />
            </div>
          </div>
        </Card>

        <Card className="hover:border-emerald-300 transition-colors">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Completed Repairs</p>
              <h3 className="text-2xl font-bold text-slate-900 mt-1">{summary.completedRepairs}</h3>
            </div>
            <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
              <CheckCircle2 className="w-6 h-6" />
            </div>
          </div>
        </Card>

        <Card className="hover:border-indigo-300 transition-colors">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Revenue</p>
              <h3 className="text-2xl font-bold text-slate-900 mt-1">₹{summary.totalRevenue.toLocaleString()}</h3>
            </div>
            <div className="p-3 bg-indigo-50 text-indigo-600 rounded-xl">
              <DollarSign className="w-6 h-6" />
            </div>
          </div>
        </Card>
      </div>

      {/* Secondary Metrics & Chart */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2" title="Repair Jobs Pipeline Distribution" subtitle="Active job counts grouped by status stage">
          <div className="h-64 w-full pt-4">
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                  <XAxis dataKey="status" tick={{ fontSize: 10, fill: '#64748B' }} interval={0} angle={-15} textAnchor="end" />
                  <YAxis tick={{ fontSize: 11, fill: '#64748B' }} allowDecimals={false} />
                  <Tooltip
                    contentStyle={{ backgroundColor: '#FFFFFF', borderRadius: '8px', border: '1px solid #E2E8F0', fontSize: '12px' }}
                  />
                  <Bar dataKey="count" fill="#2563EB" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-xs text-slate-400">
                No active repair pipeline data yet
              </div>
            )}
          </div>
        </Card>

        <Card title="Quick Operational Health" subtitle="Business summary metrics">
          <div className="space-y-4 pt-2">
            <div className="flex items-center justify-between p-3 bg-slate-50 rounded-lg border border-slate-100">
              <div className="flex items-center space-x-3">
                <Users className="w-5 h-5 text-slate-600" />
                <span className="text-xs font-semibold text-slate-700">Total Customers</span>
              </div>
              <span className="text-sm font-bold text-slate-900">{summary.totalCustomers}</span>
            </div>

            <div className="flex items-center justify-between p-3 bg-amber-50/50 rounded-lg border border-amber-200/60">
              <div className="flex items-center space-x-3">
                <AlertTriangle className="w-5 h-5 text-amber-600" />
                <span className="text-xs font-semibold text-amber-900">Low Stock Parts Alert</span>
              </div>
              <span className="text-sm font-bold text-amber-700">{summary.lowStockCount} SKUs</span>
            </div>

            <div className="flex items-center justify-between p-3 bg-emerald-50/50 rounded-lg border border-emerald-200/60">
              <div className="flex items-center space-x-3">
                <TrendingUp className="w-5 h-5 text-emerald-600" />
                <span className="text-xs font-semibold text-emerald-900">Service Status</span>
              </div>
              <span className="text-xs font-semibold text-emerald-700 bg-emerald-100 px-2 py-0.5 rounded-full">Operational</span>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};



