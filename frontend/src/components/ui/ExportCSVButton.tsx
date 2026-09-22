import React from 'react';
import { Button } from './Button';
import { Download } from 'lucide-react';

interface ExportCSVButtonProps<T> {
  data: T[];
  filename: string;
  headers: { label: string; key: keyof T | ((item: T) => any) }[];
}

export function ExportCSVButton<T>({ data, filename, headers }: ExportCSVButtonProps<T>) {
  const handleExport = () => {
    if (!data || data.length === 0) {
      alert('No data available to export.');
      return;
    }

    const headerRow = headers.map((h) => `"${h.label}"`).join(',');
    const rows = data.map((item) =>
      headers
        .map((h) => {
          let val = typeof h.key === 'function' ? h.key(item) : item[h.key];
          if (val === null || val === undefined) val = '';
          const strVal = String(val).replace(/"/g, '""');
          return `"${strVal}"`;
        })
        .join(',')
    );

    const csvContent = 'data:text/csv;charset=utf-8,\uFEFF' + [headerRow, ...rows].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `${filename}_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <Button variant="outline" size="sm" onClick={handleExport} icon={<Download className="w-4 h-4" />}>
      Export CSV
    </Button>
  );
}
