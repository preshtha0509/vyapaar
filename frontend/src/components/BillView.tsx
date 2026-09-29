import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { api } from '../api/client'

interface BillResponse {
  saleId: number
  customerPhone: string
  billText: string
}

interface BillViewProps {
  saleId: number
  onClose?: () => void
}

export function BillView({ saleId, onClose }: BillViewProps) {
  const { i18n, t } = useTranslation()
  const [bill, setBill] = useState<BillResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadBill() {
      setLoading(true)
      setError(null)
      try {
        const lang = i18n.language.startsWith('hi') ? 'hi' : 'en'
        const res = await api.get<BillResponse>(`/api/sales/${saleId}/bill?lang=${lang}`)
        setBill(res.data)
      } catch {
        setError(t('bill.error'))
      } finally {
        setLoading(false)
      }
    }
    void loadBill()
  }, [saleId, i18n.language, t])

  const handlePrint = () => window.print()

  const handleSms = () => {
    if (!bill) return
    const body = encodeURIComponent(bill.billText)
    window.open(`sms:${bill.customerPhone}?body=${body}`)
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-lg rounded-[10px] border border-rule bg-paper-raised p-6 shadow-lg">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="font-display text-xl font-medium text-ink">
            {t('bill.title')} #{saleId}
          </h3>
          {onClose && (
            <button
              type="button"
              onClick={onClose}
              className="text-sm text-ink-soft hover:text-ink"
            >
              {t('bill.close')}
            </button>
          )}
        </div>

        {loading && <p className="py-4 text-sm text-ink-soft">{t('bill.loading')}</p>}
        {error && <p className="py-4 text-sm text-debit">{error}</p>}

        {bill && (
          <>
            <pre className="overflow-x-auto whitespace-pre-wrap rounded-lg border border-rule bg-paper p-4 font-mono text-xs text-ink">
              {bill.billText}
            </pre>

            <div className="mt-4 flex justify-end gap-3 print:hidden">
              <button
                type="button"
                onClick={handlePrint}
                className="rounded-lg border border-rule bg-paper px-4 py-2 text-sm font-medium text-ink hover:bg-paper-raised"
              >
                {t('bill.print')}
              </button>
              <button
                type="button"
                onClick={handleSms}
                className="rounded-lg border border-cloth bg-cloth px-4 py-2 text-sm font-medium text-paper"
              >
                {t('bill.sendSms')}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
