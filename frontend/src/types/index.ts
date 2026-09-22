export type Role = 'ADMIN' | 'MANAGER' | 'STAFF' | 'TECHNICIAN' | 'CUSTOMER';
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  customerId?: number;
  technicianId?: number;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: number;
  email: string;
  fullName: string;
  role: Role;
  customerId?: number;
  technicianId?: number;
}

export interface Customer {
  id: number;
  userId?: number;
  name: string;
  email: string;
  phone: string;
  address?: string;
  createdAt: string;
}

export interface Device {
  id: number;
  customerId: number;
  customerName?: string;
  brand: string;
  model: string;
  serialNumber?: string;
  imei?: string;
  deviceType: string;
  createdAt: string;
}

export interface ServiceRequest {
  id: number;
  customerId: number;
  customerName: string;
  customerPhone?: string;
  deviceId: number;
  deviceBrand: string;
  deviceModel: string;
  problemTitle: string;
  description?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status: 'REQUESTED' | 'ACCEPTED' | 'REJECTED' | 'CONVERTED_TO_JOB' | 'CANCELLED';
  createdAt: string;
}

export type RepairJobStatus =
  | 'REQUESTED'
  | 'ASSIGNED'
  | 'DIAGNOSING'
  | 'ESTIMATE_CREATED'
  | 'WAITING_FOR_APPROVAL'
  | 'APPROVED'
  | 'REJECTED'
  | 'WAITING_FOR_PARTS'
  | 'IN_REPAIR'
  | 'READY_FOR_QC'
  | 'QC_FAILED'
  | 'READY_FOR_PICKUP'
  | 'COMPLETED'
  | 'CANCELLED';

export interface RepairJob {
  id: number;
  jobNumber: string;
  serviceRequestId?: number;
  customerId: number;
  customerName: string;
  customerPhone?: string;
  customerEmail?: string;
  deviceId: number;
  deviceBrand: string;
  deviceModel: string;
  deviceSerialNumber?: string;
  deviceImei?: string;
  technicianId?: number;
  technicianName?: string;
  status: RepairJobStatus;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  laborCost: number;
  totalEstimatedCost: number;
  totalActualCost: number;
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
  updatedAt: string;
}

export interface RepairJobStatusHistory {
  id: number;
  repairJobId: number;
  oldStatus?: RepairJobStatus;
  newStatus: RepairJobStatus;
  changedByUserId?: number;
  changedByUserName: string;
  remarks?: string;
  createdAt: string;
}

export interface Diagnosis {
  id: number;
  repairJobId: number;
  jobNumber: string;
  technicianId: number;
  technicianName: string;
  symptoms?: string;
  findings: string;
  recommendedRepair?: string;
  estimatedLaborCost?: number;
  notes?: string;
  createdAt: string;
}

export interface Part {
  id: number;
  sku: string;
  name: string;
  category: string;
  compatibleDevice?: string;
  purchasePrice: number;
  sellingPrice: number;
  quantityInStock: number;
  reservedQuantity: number;
  availableQuantity: number;
  reorderLevel: number;
  active: boolean;
  createdAt: string;
}

export interface EstimateItem {
  id: number;
  partId?: number;
  itemDescription: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface Estimate {
  id: number;
  estimateNumber: string;
  repairJobId: number;
  jobNumber: string;
  customerId: number;
  customerName: string;
  status: 'DRAFT' | 'SENT' | 'APPROVED' | 'REJECTED';
  totalPartsCost: number;
  laborCost: number;
  taxAmount: number;
  totalAmount: number;
  rejectionReason?: string;
  approvedAt?: string;
  rejectedAt?: string;
  createdAt: string;
  items: EstimateItem[];
}

export interface InvoiceItem {
  id: number;
  description: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  itemType: 'PART' | 'LABOR' | 'SERVICE' | 'TAX';
}

export interface Invoice {
  id: number;
  invoiceNumber: string;
  repairJobId: number;
  jobNumber: string;
  customerId: number;
  customerName: string;
  customerEmail: string;
  customerPhone?: string;
  deviceBrand: string;
  deviceModel: string;
  subtotal: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount: number;
  paymentStatus: 'PENDING' | 'PARTIALLY_PAID' | 'PAID' | 'FAILED' | 'REFUNDED';
  createdAt: string;
  items: InvoiceItem[];
}

export interface DashboardSummary {
  totalCustomers: number;
  activeRepairs: number;
  pendingEstimates: number;
  completedRepairs: number;
  totalRevenue: number;
  lowStockCount: number;
  repairsByStatus: Record<string, number>;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  code?: string;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
