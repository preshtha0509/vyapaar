import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import type { BalanceRow, DashboardSummary } from '../types'

const currency = new Intl.NumberFormat('en-IN', {
  maximumFractionDigits: 2,
  minimumFractionDigits: 0,
})

function formatRupees(value: number) {
  return `₹${currency.format(value)}`
}

function BalanceList({ rows, emptyText }: { rows: BalanceRow[]; emptyText: string }) {
  const { t } = useTranslation()

  if (rows.length === 0) {
    return <p className="m-0 py-5 text-sm text-ink-soft">{emptyText}</p>
  }

  return (
    <div className="divide-y divide-rule">
      {rows.map((row) => (
        <div key={row.id} className="flex items-center justify-between gap-4 py-3">
          <div>
            <p className="m-0 text-sm font-medium text-ink">{row.name || `Party #${row.id}`}</p>
            <p className="m-0 mt-1 text-xs text-ink-soft">
              {t('common.id')} {row.id}
            </p>
          </div>
          <p className="m-0 whitespace-nowrap font-mono text-sm font-medium text-debit">
            {formatRupees(row.balance)}
          </p>
        </div>
      ))}
    </div>
  )
}

export function DashboardPage() {
  const { t } = useTranslation()
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  async function loadSummary() {
    setLoading(true)
    setError(null)
    try {
      const response = await api.get<DashboardSummary>('/api/dashboard/summary')
      setSummary(response.data)
    } catch {
      setError(t('dashboard.loadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadSummary()
  }, [])

  const netExposure = useMemo(() => {
    if (!summary) return 0
    return summary.totalToReceive - summary.totalToPay
  }, [summary])

  return (
    <main className="px-5 py-6 md:px-8 md:py-8">
      <section className="mb-5 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="m-0 mb-1 text-sm text-ink-soft">{t('dashboard.eyebrow')}</p>
          <h2 className="m-0 font-display text-3xl font-medium text-ink">{t('dashboard.title')}</h2>
        </div>
        <button
          type="button"
          onClick={() => void loadSummary()}
          className="w-fit rounded-lg border border-rule bg-paper-raised px-4 py-2 text-sm font-medium text-cloth"
        >
          {t('common.refresh')}
        </button>
      </section>

      {error && (
        <div className="mb-4 rounded-[10px] border border-rule bg-debit-bg px-4 py-3 text-sm text-debit">
          {error}
        </div>
      )}

      {loading ? (
        <div className="rounded-[10px] border border-rule bg-paper-raised px-5 py-8 text-center text-sm text-ink-soft">
          {t('dashboard.loading')}
        </div>
      ) : (
        <div className="grid gap-5">
          <section className="grid grid-cols-1 gap-3 md:grid-cols-3">
            <div className="rounded-[10px] border border-rule bg-paper-raised px-5 py-4">
              <p className="m-0 mb-1.5 text-xs text-ink-soft">{t('dashboard.totalToReceive')}</p>
              <p className="m-0 font-mono text-2xl font-medium text-credit">
                {formatRupees(summary?.totalToReceive ?? 0)}
              </p>
            </div>
            <div className="rounded-[10px] border border-rule bg-paper-raised px-5 py-4">
              <p className="m-0 mb-1.5 text-xs text-ink-soft">{t('dashboard.totalToPay')}</p>
              <p className="m-0 font-mono text-2xl font-medium text-debit">
                {formatRupees(summary?.totalToPay ?? 0)}
              </p>
            </div>
            <div className="rounded-[10px] border border-rule bg-paper-raised px-5 py-4">
              <p className="m-0 mb-1.5 text-xs text-ink-soft">{t('dashboard.netPosition')}</p>
              <p
                className={[
                  'm-0 font-mono text-2xl font-medium',
                  netExposure >= 0 ? 'text-credit' : 'text-debit',
                ].join(' ')}
              >
                {formatRupees(netExposure)}
              </p>
            </div>
          </section>

          <section className="grid grid-cols-1 overflow-hidden rounded-[10px] border border-rule bg-paper-raised md:grid-cols-2">
            <div className="border-b border-rule px-5 py-4 md:border-b-0 md:border-r">
              <div className="mb-2 flex items-center justify-between gap-3">
                <div>
                  <h3 className="m-0 font-display text-lg font-medium">{t('dashboard.topCustomers')}</h3>
                  <p className="m-0 mt-1 text-sm text-ink-soft">{t('dashboard.topCustomersHelp')}</p>
                </div>
                <Link to="/customers" className="text-sm font-medium text-cloth no-underline">
                  {t('dashboard.view')}
                </Link>
              </div>
              <BalanceList
                rows={summary?.topCustomers ?? []}
                emptyText={t('dashboard.emptyCustomers')}
              />
            </div>

            <div className="px-5 py-4">
              <div className="mb-2 flex items-center justify-between gap-3">
                <div>
                  <h3 className="m-0 font-display text-lg font-medium">{t('dashboard.topSuppliers')}</h3>
                  <p className="m-0 mt-1 text-sm text-ink-soft">{t('dashboard.topSuppliersHelp')}</p>
                </div>
                <Link to="/suppliers" className="text-sm font-medium text-cloth no-underline">
                  {t('dashboard.view')}
                </Link>
              </div>
              <BalanceList
                rows={summary?.topSuppliers ?? []}
                emptyText={t('dashboard.emptySuppliers')}
              />
            </div>
          </section>

          <section className="grid grid-cols-1 gap-3 md:grid-cols-4">
            <Link
              to="/sales/new"
              className="rounded-[10px] border border-rule bg-paper-raised px-4 py-3 text-sm font-medium text-ink no-underline"
            >
              {t('dashboard.newSale')}
            </Link>
            <Link
              to="/purchases/new"
              className="rounded-[10px] border border-rule bg-paper-raised px-4 py-3 text-sm font-medium text-ink no-underline"
            >
              {t('dashboard.newPurchase')}
            </Link>
            <Link
              to="/payments"
              className="rounded-[10px] border border-rule bg-paper-raised px-4 py-3 text-sm font-medium text-ink no-underline"
            >
              {t('dashboard.recordPayment')}
            </Link>
            <Link
              to="/products"
              className="rounded-[10px] border border-rule bg-paper-raised px-4 py-3 text-sm font-medium text-ink no-underline"
            >
              {t('nav.products')}
            </Link>
          </section>
        </div>
      )}
    </main>
  )
}
