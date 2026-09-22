import React from 'react';
import { Badge } from './Badge';

export const StatusBadge: React.FC<{ status: string }> = ({ status }) => {
  const getVariant = (st: string) => {
    switch (st.toUpperCase()) {
      case 'COMPLETED':
      case 'APPROVED':
      case 'PAID':
      case 'ACTIVE':
      case 'AVAILABLE':
        return 'green';

      case 'IN_REPAIR':
      case 'DIAGNOSING':
      case 'ASSIGNED':
      case 'PARTIALLY_PAID':
        return 'blue';

      case 'WAITING_FOR_APPROVAL':
      case 'WAITING_FOR_PARTS':
      case 'READY_FOR_QC':
      case 'READY_FOR_PICKUP':
      case 'REQUESTED':
      case 'SENT':
      case 'PENDING':
        return 'amber';

      case 'REJECTED':
      case 'QC_FAILED':
      case 'CANCELLED':
      case 'FAILED':
      case 'SUSPENDED':
      case 'INACTIVE':
        return 'red';

      default:
        return 'gray';
    }
  };

  const formattedStatus = status.replace(/_/g, ' ');

  return <Badge variant={getVariant(status)}>{formattedStatus}</Badge>;
};
