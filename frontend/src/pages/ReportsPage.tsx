import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { api } from '../api/client'
import { MonthlyTrendChart } from '../components/MonthlyTrendChart'
import type { ReportsSummary, SupplierVolumeRow, TrendPoint } from '../types'

const today = new Date().toISOString().slice(0, 10)
const monthStart = new Date(new Date().getFullYear(), new Date().getMonth(), 1)
  .toISOString()
  .slice(0, 10)

const currency = new Intl.NumberFormat('en-IN', {
  maximumFractionDigits: 2,
  minimumFractionDigits: 0,
})

function formatRupees(value: number) {
  return `₹${currency.format(value)}`
}

export function ReportsPage() {
  const { t } = useTranslation()
  const [from, setFrom] = useState(monthStart)
  const [to, setTo] = useState(today)
  const [summary, setSummary] = useState<ReportsSummary | null>(null)
  const [trend, setTrend] = useState<TrendPoint[]>([])
  const [supplierVolume, setSupplierVolume] = useState<SupplierVolumeRow[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadReports = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [summaryResponse, trendResponse, supplierResponse] = await Promise.all([
        api.get<ReportsSummary>('/api/reports/summary', { params: { from, to } }),
        api.get<TrendPoint[]>('/api/reports/monthly-trend', { params: { months: 6 } }),
        api.get<SupplierVolumeRow[]>('/api/reports/supplier-volume', { params: { from, to } }),
      ])
      setSummary(summaryResponse.data)
      setTrend(trendResponse.data)
      setSupplierVolume(supplierResponse.data)
    } catch {
      setError(t('reports.loadError'))
    } finally {
      setLoading(false)
    }
  }, [from, to, t])

  useEffect(() => {
    void loadReports()
  }, [loadReports])

  const maxSupplierTotal = useMemo(
    () => supplierVolume.reduce((max, row) => Math.max(max, row.total), 0),
    [supplierVolume],
  )

  return (
    <main className="px-5 py-6 md:px-8 md:py-8">
      <section className="mb-5 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="m-0 mb-1 text-sm text-ink-soft">{t('reports.eyebrow')}</p>
          <h2 className="m-0 font-display text-3xl font-medium text-ink">{t('reports.title')}</h2>
        </div>
        <button
          type="button"
          onClick={() => void loadReports()}
          className="w-fit rounded-lg border border-rule bg-paper-raised px-4 py-2 text-sm font-medium text-cloth"
        >
          {t('common.refresh')}
        </button>
      </section>

      <section className="mb-5 flex flex-col gap-3 rounded-[10px] border border-rule bg-paper-raised px-4 py-4 md:flex-row md:items-end">
        <label className="grid gap-1.5 text-sm">
          <span className="text-xs text-ink-soft">{t('common.from')}</span>
          <input
            type="date"
            value={from}
            onChange={(event) => setFrom(event.target.value)}
            className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
          />
        </label>
        <label className="grid gap-1.5 text-sm">
          <span className="text-xs text-ink-soft">{t('common.to')}</span>
          <input
            type="date"
            value={to}
            onChange={(event) => setTo(event.target.value)}
            className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
          />
        </label>
        <button
          type="button"
          onClick={() => void loadReports()}
          className="rounded-lg border border-cloth bg-cloth px-4 py-2 text-sm font-medium text-paper"
        >
          {t('common.applyRange')}
        </button>
      </section>

      {error && (
        <div className="mb-4 rounded-[10px] border border-rule bg-debit-bg px-4 py-3 text-sm text-debit">
          {error}
        </div>
      )}

      {loading ? (
        <div className="rounded-[10px] border border-rule bg-paper-raised px-5 py-8 text-center text-sm text-ink-soft">
          {t('reports.loading')}
        </div>
      ) : (
        <div className="grid gap-5">
          <section className="grid grid-cols-1 gap-3 md:grid-cols-3">
            <div className="rounded-[10px] border border-rule bg-paper-raised px-5 py-4">
              <p className="m-0 mb-1.5 text-xs text-ink-soft">{t('common.totalSales')}</p>
              <p className="m-0 font-mono text-2xl font-medium text-credit">
                {formatRupees(summary?.totalSales ?? 0)}
              </p>
            </div>
            <div className="rounded-[10px] border border-rule bg-paper-raised px-5 py-4">
              <p className="m-0 mb-1.5 text-xs text-ink-soft">{t('common.totalPurchases')}</p>
              <p className="m-0 font-mono text-2xl font-medium text-debit">
                {formatRupees(summary?.totalPurchases ?? 0)}
              </p>
            </div>
            <div className="rounded-[10px] border border-rule bg-paper-raised px-5 py-4">
              <p className="m-0 mb-1.5 text-xs text-ink-soft">{t('common.grossDifference')}</p>
              <p
                className={[
                  'm-0 font-mono text-2xl font-medium',
                  (summary?.grossDifference ?? 0) >= 0 ? 'text-credit' : 'text-debit',
                ].join(' ')}
              >
                {formatRupees(summary?.grossDifference ?? 0)}
              </p>
            </div>
          </section>

          <MonthlyTrendChart data={trend} />

          <section className="rounded-[10px] border border-rule bg-paper-raised px-5 py-4 md:px-6 md:py-5">
            <h3 className="m-0 font-display text-lg font-medium">{t('reports.supplierVolume')}</h3>
            <p className="m-0 mt-1 text-sm text-ink-soft">{t('reports.supplierVolumeHelp')}</p>

            <div className="mt-4 grid gap-3">
              {supplierVolume.length === 0 ? (
                <p className="m-0 py-4 text-sm text-ink-soft">{t('reports.emptySupplierVolume')}</p>
              ) : (
                supplierVolume.map((row) => {
                  const width = maxSupplierTotal > 0 ? `${(row.total / maxSupplierTotal) * 100}%` : '0%'
                  return (
                    <div key={row.supplier} className="rounded-[10px] border border-rule bg-paper px-4 py-3">
                      <div className="mb-2 flex items-center justify-between gap-4">
                        <p className="m-0 text-sm font-medium text-ink">{row.supplier}</p>
                        <p className="m-0 whitespace-nowrap font-mono text-sm text-debit">
                          {formatRupees(row.total)}
                        </p>
                      </div>
                      <div className="h-2 overflow-hidden rounded-full border border-rule bg-paper-raised">
                        <div className="h-full bg-debit" style={{ width }} />
                      </div>
                    </div>
                  )
                })
              )}
            </div>
          </section>
        </div>
      )}
    </main>
  )
}
