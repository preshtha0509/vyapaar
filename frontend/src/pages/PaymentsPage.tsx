import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { api } from '../api/client'
import type {
  Customer,
  Payment,
  PaymentForm,
  PaymentMode,
  PaymentPartyType,
  Supplier,
} from '../types'

const today = new Date().toISOString().slice(0, 10)

const emptyForm: PaymentForm = {
  partyType: 'CUSTOMER',
  partyId: '',
  amount: '',
  paymentDate: today,
  mode: 'CASH',
  notes: '',
}

const paymentModes: PaymentMode[] = ['CASH', 'UPI', 'BANK_TRANSFER', 'CHEQUE']

const currency = new Intl.NumberFormat('en-IN', {
  maximumFractionDigits: 2,
  minimumFractionDigits: 0,
})

function formatRupees(value: number) {
  return `₹${currency.format(value)}`
}

export function PaymentsPage() {
  const { t } = useTranslation()
  const [customers, setCustomers] = useState<Customer[]>([])
  const [suppliers, setSuppliers] = useState<Supplier[]>([])
  const [form, setForm] = useState<PaymentForm>(emptyForm)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [savedPayment, setSavedPayment] = useState<Payment | null>(null)
  const [newBalance, setNewBalance] = useState<number | null>(null)

  function modeLabel(mode: PaymentMode) {
    switch (mode) {
      case 'CASH':
        return t('payments.modeCash')
      case 'UPI':
        return t('payments.modeUpi')
      case 'BANK_TRANSFER':
        return t('payments.modeBankTransfer')
      case 'CHEQUE':
        return t('payments.modeCheque')
    }
  }

  async function loadParties() {
    setLoading(true)
    setError(null)
    try {
      const [customerResponse, supplierResponse] = await Promise.all([
        api.get<Customer[]>('/api/customers'),
        api.get<Supplier[]>('/api/suppliers'),
      ])
      setCustomers(customerResponse.data)
      setSuppliers(supplierResponse.data)
    } catch {
      setError(t('payments.loadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadParties()
  }, [])

  const parties = form.partyType === 'CUSTOMER' ? customers : suppliers

  const selectedParty = useMemo(
    () => parties.find((party) => String(party.id) === form.partyId),
    [parties, form.partyId],
  )

  const amount = Number(form.amount || 0)

  function updatePartyType(partyType: PaymentPartyType) {
    setForm({ ...form, partyType, partyId: '' })
    setSavedPayment(null)
    setNewBalance(null)
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    setSavedPayment(null)
    setNewBalance(null)

    if (!form.partyId) {
      setError(t('payments.selectError'))
      setSaving(false)
      return
    }

    try {
      const response = await api.post<Payment>('/api/payments', {
        partyType: form.partyType,
        partyId: Number(form.partyId),
        amount,
        paymentDate: form.paymentDate,
        mode: form.mode,
        ...(form.notes.trim() ? { notes: form.notes.trim() } : {}),
      })

      const balanceUrl =
        form.partyType === 'CUSTOMER'
          ? `/api/ledger/customer/${form.partyId}/balance`
          : `/api/ledger/supplier/${form.partyId}/balance`
      const balanceResponse = await api.get<number>(balanceUrl)

      setSavedPayment(response.data)
      setNewBalance(balanceResponse.data)
      setForm({ ...emptyForm, partyType: form.partyType })
    } catch {
      setError(t('payments.saveError'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <main className="px-5 py-6 md:px-8 md:py-8">
      <section className="mb-5 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="m-0 mb-1 text-sm text-ink-soft">{t('payments.eyebrow')}</p>
          <h2 className="m-0 font-display text-3xl font-medium text-ink">{t('payments.title')}</h2>
        </div>
        <div className="rounded-[10px] border border-rule bg-paper-raised px-4 py-3">
          <p className="m-0 mb-1 text-xs text-ink-soft">{t('payments.entryAmount')}</p>
          <p className="m-0 font-mono text-xl font-medium text-credit">{formatRupees(amount)}</p>
        </div>
      </section>

      {error && (
        <div className="mb-4 rounded-[10px] border border-rule bg-debit-bg px-4 py-3 text-sm text-debit">
          {error}
        </div>
      )}

      {savedPayment && (
        <div className="mb-4 rounded-[10px] border border-rule bg-credit-bg px-4 py-3 text-sm text-credit">
          {t('payments.saved', { id: savedPayment.id, balance: formatRupees(newBalance ?? 0) })}
        </div>
      )}

      <section className="grid grid-cols-1 overflow-hidden rounded-[10px] border border-rule bg-paper-raised md:grid-cols-[1fr_0.8fr]">
        <form onSubmit={handleSubmit} className="border-b border-rule px-4 py-4 md:border-b-0 md:border-r">
          <h3 className="m-0 font-display text-lg font-medium">{t('payments.details')}</h3>
          <p className="m-0 mt-1 text-sm text-ink-soft">{t('payments.help')}</p>

          <div className="mt-5 grid gap-3">
            <div className="grid grid-cols-2 gap-2 rounded-[10px] border border-rule bg-paper p-1">
              <button
                type="button"
                onClick={() => updatePartyType('CUSTOMER')}
                className={[
                  'rounded-lg border px-3 py-2 text-sm font-medium',
                  form.partyType === 'CUSTOMER'
                    ? 'border-rule bg-paper-raised text-cloth'
                    : 'border-transparent bg-paper text-ink-soft',
                ].join(' ')}
              >
                {t('common.customer')}
              </button>
              <button
                type="button"
                onClick={() => updatePartyType('SUPPLIER')}
                className={[
                  'rounded-lg border px-3 py-2 text-sm font-medium',
                  form.partyType === 'SUPPLIER'
                    ? 'border-rule bg-paper-raised text-cloth'
                    : 'border-transparent bg-paper text-ink-soft',
                ].join(' ')}
              >
                {t('common.supplier')}
              </button>
            </div>

            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">
                {form.partyType === 'CUSTOMER' ? t('common.customer') : t('common.supplier')}
              </span>
              <select
                required
                value={form.partyId}
                onChange={(event) => setForm({ ...form, partyId: event.target.value })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              >
                <option value="">
                  {loading
                    ? t('payments.loadingParties')
                    : form.partyType === 'CUSTOMER'
                      ? t('payments.selectCustomer')
                      : t('payments.selectSupplier')}
                </option>
                {parties.map((party) => (
                  <option key={party.id} value={party.id}>
                    {party.name} - {party.city}
                  </option>
                ))}
              </select>
            </label>

            <div className="grid gap-3 md:grid-cols-2">
              <label className="grid gap-1.5 text-sm">
                <span className="text-xs text-ink-soft">{t('common.amount')}</span>
                <input
                  required
                  min="0.01"
                  type="number"
                  step="0.01"
                  value={form.amount}
                  onChange={(event) => setForm({ ...form, amount: event.target.value })}
                  className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
                />
              </label>

              <label className="grid gap-1.5 text-sm">
                <span className="text-xs text-ink-soft">{t('common.date')}</span>
                <input
                  required
                  type="date"
                  value={form.paymentDate}
                  onChange={(event) => setForm({ ...form, paymentDate: event.target.value })}
                  className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
                />
              </label>
            </div>

            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.mode')}</span>
              <select
                required
                value={form.mode}
                onChange={(event) => setForm({ ...form, mode: event.target.value as PaymentMode })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              >
                {paymentModes.map((mode) => (
                  <option key={mode} value={mode}>
                    {modeLabel(mode)}
                  </option>
                ))}
              </select>
            </label>

            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.notesOptional')}</span>
              <textarea
                rows={3}
                value={form.notes}
                onChange={(event) => setForm({ ...form, notes: event.target.value })}
                className="resize-none rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              />
            </label>
          </div>

          <button
            type="submit"
            disabled={saving || loading}
            className="mt-5 w-full rounded-lg border border-cloth bg-cloth px-4 py-2.5 text-sm font-medium text-paper disabled:opacity-60"
          >
            {saving ? t('common.saving') : t('payments.save')}
          </button>
        </form>

        <aside className="px-4 py-4">
          <h3 className="m-0 font-display text-lg font-medium">{t('transactions.preview')}</h3>
          <div className="mt-5 grid gap-3">
            <div className="rounded-[10px] border border-rule bg-paper px-4 py-3">
              <p className="m-0 mb-1 text-xs text-ink-soft">{t('common.type')}</p>
              <p className="m-0 text-sm font-medium">
                {form.partyType === 'CUSTOMER'
                  ? t('payments.customerReceived')
                  : t('payments.supplierPaid')}
              </p>
            </div>
            <div className="rounded-[10px] border border-rule bg-paper px-4 py-3">
              <p className="m-0 mb-1 text-xs text-ink-soft">{t('common.party')}</p>
              <p className="m-0 text-sm font-medium">{selectedParty?.name ?? t('common.notSelected')}</p>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="rounded-[10px] border border-rule bg-credit-bg px-4 py-3 text-credit">
                <p className="m-0 mb-1 text-xs">{t('common.amount')}</p>
                <p className="m-0 font-mono text-sm">{formatRupees(amount)}</p>
              </div>
              <div className="rounded-[10px] border border-rule bg-paper px-4 py-3">
                <p className="m-0 mb-1 text-xs text-ink-soft">{t('common.mode')}</p>
                <p className="m-0 font-mono text-sm">{modeLabel(form.mode)}</p>
              </div>
            </div>
            <div className="rounded-[10px] border border-rule bg-paper px-4 py-3">
              <p className="m-0 mb-1 text-xs text-ink-soft">{t('payments.ledgerEffect')}</p>
              <p className="m-0 text-sm">
                {form.partyType === 'CUSTOMER'
                  ? t('payments.customerEffect')
                  : t('payments.supplierEffect')}
              </p>
            </div>
          </div>
        </aside>
      </section>
    </main>
  )
}
