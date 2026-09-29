import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { useTranslation } from 'react-i18next'
import type { TrendPoint } from '../types'

const currency = new Intl.NumberFormat('en-IN', {
  maximumFractionDigits: 0,
  minimumFractionDigits: 0,
})

function formatRupees(value: number) {
  return `₹${currency.format(value)}`
}

export function MonthlyTrendChart({ data }: { data: TrendPoint[] }) {
  const { t } = useTranslation()

  return (
    <div className="rounded-[10px] border border-rule bg-paper-raised px-5 py-4 md:px-6 md:py-5">
      <h3 className="m-0 mb-4 font-display text-lg font-medium">{t('reports.trendTitle')}</h3>
      <div className="h-72 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data} margin={{ top: 8, right: 12, bottom: 8, left: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#DFD3B8" />
            <XAxis dataKey="month" tick={{ fontSize: 12, fill: '#7C7062' }} />
            <YAxis
              tick={{ fontSize: 12, fill: '#7C7062' }}
              tickFormatter={(value: number) => currency.format(value)}
              width={72}
            />
            <Tooltip formatter={(value) => formatRupees(Number(value ?? 0))} />
            <Line type="monotone" dataKey="sales" stroke="#3D6B4F" strokeWidth={2} dot={false} />
            <Line
              type="monotone"
              dataKey="purchases"
              stroke="#A8481F"
              strokeWidth={2}
              dot={false}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
