import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { api } from '../api/client'
import type { Customer, Receipt } from '../types'

const today = new Date().toISOString().slice(0, 10)

const currency = new Intl.NumberFormat('en-IN', {
  maximumFractionDigits: 2,
  minimumFractionDigits: 0,
})

function formatRupees(value: number) {
  return `₹${currency.format(value)}`
}

function formatQuantity(value: number) {
  return Number.isInteger(value) ? String(value) : String(value)
}

export function ReceiptsPage() {
  const { t } = useTranslation()
  const [customers, setCustomers] = useState<Customer[]>([])
  const [customerId, setCustomerId] = useState('')
  const [date, setDate] = useState(today)
  const [loading, setLoading] = useState(true)
  const [generating, setGenerating] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [receipt, setReceipt] = useState<Receipt | null>(null)
  const [sent, setSent] = useState(false)

  async function loadCustomers() {
    setLoading(true)
    setError(null)
    try {
      const response = await api.get<Customer[]>('/api/customers')
      setCustomers(response.data)
    } catch {
      setError(t('receipts.loadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadCustomers()
  }, [])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!customerId) {
      setError(t('receipts.selectError'))
      return
    }

    setGenerating(true)
    setError(null)
    setReceipt(null)
    setSent(false)

    try {
      const response = await api.post<Receipt>('/api/receipts', {
        customerId: Number(customerId),
        date,
      })
      setReceipt(response.data)
    } catch {
      // The backend returns 400 with a readable reason when there are no sales that day.
      setError(t('receipts.noSales'))
    } finally {
      setGenerating(false)
    }
  }

  // Record that the owner sent it, then hand over to WhatsApp. The audit row must not block the
  // send, so a failure here is swallowed deliberately.
  async function sendOnWhatsApp(current: Receipt) {
    try {
      await api.post(`/api/receipts/${current.publicToken}/sends`, {
        channel: 'WHATSAPP',
        recipient: null,
      })
    } catch {
      // Ignored on purpose: the receipt still needs to go out.
    }
    setSent(true)
    if (current.whatsAppLink) {
      window.open(current.whatsAppLink, '_blank', 'noopener')
    }
  }

  return (
    <main className="px-5 py-6 md:px-8 md:py-8">
      <section className="mb-5">
        <p className="m-0 mb-1 text-sm text-ink-soft">{t('receipts.eyebrow')}</p>
        <h2 className="m-0 font-display text-3xl font-medium text-ink">{t('receipts.title')}</h2>
        <p className="m-0 mt-2 text-sm text-ink-soft">{t('receipts.help')}</p>
      </section>

      {error && (
        <div className="mb-4 rounded-[10px] border border-rule bg-debit-bg px-4 py-3 text-sm text-debit">
          {error}
        </div>
      )}

      {sent && (
        <div className="mb-4 rounded-[10px] border border-rule bg-credit-bg px-4 py-3 text-sm text-credit">
          {t('receipts.sent')}
        </div>
      )}

      <section className="grid grid-cols-1 overflow-hidden rounded-[10px] border border-rule bg-paper-raised md:grid-cols-[1fr_1.2fr]">
        <form onSubmit={handleSubmit} className="border-b border-rule px-4 py-4 md:border-b-0 md:border-r">
          <h3 className="m-0 font-display text-lg font-medium">{t('receipts.details')}</h3>

          <div className="mt-5 grid gap-3">
            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.customer')}</span>
              <select
                required
                value={customerId}
                onChange={(event) => setCustomerId(event.target.value)}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              >
                <option value="">
                  {loading ? t('receipts.loadingCustomers') : t('receipts.selectCustomer')}
                </option>
                {customers.map((customer) => (
                  <option key={customer.id} value={customer.id}>
                    {customer.name} - {customer.city}
                  </option>
                ))}
              </select>
            </label>

            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.date')}</span>
              <input
                required
                type="date"
                value={date}
                onChange={(event) => setDate(event.target.value)}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              />
            </label>
          </div>

          <button
            type="submit"
            disabled={generating || loading}
            className="mt-5 w-full rounded-lg border border-cloth bg-cloth px-4 py-2.5 text-sm font-medium text-paper disabled:opacity-60"
          >
            {generating ? t('receipts.generating') : t('receipts.generate')}
          </button>
        </form>

        <aside className="px-4 py-4">
          <h3 className="m-0 font-display text-lg font-medium">{t('receipts.preview')}</h3>

          {!receipt ? (
            <p className="m-0 mt-5 text-sm text-ink-soft">{t('receipts.empty')}</p>
          ) : (
            <div className="mt-5 grid gap-3">
              <div className="grid grid-cols-2 gap-3">
                <div className="rounded-[10px] border border-rule bg-paper px-4 py-3">
                  <p className="m-0 mb-1 text-xs text-ink-soft">{t('receipts.receiptNumber')}</p>
                  <p className="m-0 font-mono text-sm font-medium">{receipt.receiptNumber}</p>
                </div>
                <div className="rounded-[10px] border border-rule bg-credit-bg px-4 py-3 text-credit">
                  <p className="m-0 mb-1 text-xs">{t('receipts.total')}</p>
                  <p className="m-0 font-mono text-sm font-medium">{formatRupees(receipt.subtotal)}</p>
                </div>
              </div>

              <div className="overflow-x-auto rounded-[10px] border border-rule">
                <table className="w-full border-collapse text-left text-xs">
                  <thead className="bg-paper-raised text-ink-soft">
                    <tr>
                      <th className="px-3 py-2 font-medium">{t('receipts.item')}</th>
                      <th className="px-3 py-2 text-right font-medium">{t('common.quantity')}</th>
                      <th className="px-3 py-2 text-right font-medium">{t('common.rate')}</th>
                      <th className="px-3 py-2 text-right font-medium">{t('common.amount')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {receipt.lines.map((line, index) => (
                      <tr key={index} className="border-t border-rule">
                        <td className="px-3 py-2">{line.productName}</td>
                        <td className="px-3 py-2 text-right font-mono">
                          {formatQuantity(line.quantity)} {line.unit}
                        </td>
                        <td className="px-3 py-2 text-right font-mono">{formatRupees(line.rate)}</td>
                        <td className="px-3 py-2 text-right font-mono">{formatRupees(line.lineTotal)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="grid gap-2 md:grid-cols-2">
                <a
                  href={receipt.pdfUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="rounded-lg border border-rule bg-paper px-4 py-2.5 text-center text-sm font-medium text-ink no-underline"
                >
                  {t('receipts.previewPdf')}
                </a>
                <button
                  type="button"
                  disabled={!receipt.whatsAppLink}
                  onClick={() => void sendOnWhatsApp(receipt)}
                  className="rounded-lg border border-cloth bg-cloth px-4 py-2.5 text-sm font-medium text-paper disabled:opacity-60"
                >
                  {t('receipts.sendWhatsapp')}
                </button>
              </div>

              {!receipt.whatsAppLink && (
                <p className="m-0 text-xs text-ink-soft">{t('receipts.noPhone')}</p>
              )}

              <div className="rounded-[10px] border border-rule bg-paper px-4 py-3">
                <p className="m-0 mb-1 text-xs text-ink-soft">{t('receipts.linkLabel')}</p>
                <p className="m-0 break-all font-mono text-xs">{receipt.receiptUrl}</p>
                <p className="m-0 mt-2 text-xs text-ink-soft">{t('receipts.linkWarning')}</p>
              </div>
            </div>
          )}
        </aside>
      </section>
    </main>
  )
}
