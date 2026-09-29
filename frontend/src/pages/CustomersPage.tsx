import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import axios from 'axios'
import { api } from '../api/client'
import type { Customer, CustomerForm } from '../types'

type CustomerRow = Customer & {
  balance: number | null
}

const emptyForm: CustomerForm = {
  name: '',
  phone: '',
  city: '',
  openingBalance: '0',
  gstNumber: '',
  notes: '',
}

const currency = new Intl.NumberFormat('en-IN', {
  maximumFractionDigits: 2,
  minimumFractionDigits: 0,
})

export function CustomersPage() {
  const { t } = useTranslation()
  const [customers, setCustomers] = useState<CustomerRow[]>([])
  const [form, setForm] = useState<CustomerForm>(emptyForm)
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function formatRupees(value: number | null) {
    if (value === null) return t('common.checking')
    return `₹${currency.format(value)}`
  }

  async function loadCustomers() {
    setLoading(true)
    setError(null)
    try {
      const response = await api.get<Customer[]>('/api/customers')
      const rows = response.data.map((customer) => ({ ...customer, balance: null }))
      setCustomers(rows)

      const balances = await Promise.all(
        rows.map(async (customer) => {
          try {
            const balance = await api.get<number>(`/api/ledger/customer/${customer.id}/balance`)
            return { id: customer.id, balance: balance.data }
          } catch {
            return { id: customer.id, balance: null }
          }
        }),
      )

      setCustomers((current) =>
        current.map((customer) => {
          const match = balances.find((balance) => balance.id === customer.id)
          return match ? { ...customer, balance: match.balance } : customer
        }),
      )
    } catch {
      setError(t('parties.customerLoadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadCustomers()
  }, [])

  const filteredCustomers = useMemo(() => {
    const query = search.trim().toLowerCase()
    if (!query) return customers
    return customers.filter((customer) =>
      [customer.name, customer.phone, customer.city, customer.gstNumber ?? '']
        .join(' ')
        .toLowerCase()
        .includes(query),
    )
  }, [customers, search])

  const totalOutstanding = useMemo(
    () =>
      customers.reduce((sum, customer) => {
        if (customer.balance === null || customer.balance <= 0) return sum
        return sum + customer.balance
      }, 0),
    [customers],
  )

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const payload = {
        name: form.name.trim(),
        phone: form.phone.trim(),
        city: form.city.trim(),
        openingBalance: Number(form.openingBalance || 0),
        ...(form.gstNumber.trim() ? { gstNumber: form.gstNumber.trim() } : {}),
        ...(form.notes.trim() ? { notes: form.notes.trim() } : {}),
      }

      await api.post('/api/customers', payload)
      setForm(emptyForm)
      await loadCustomers()
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) {
        const serverMessage =
          (typeof err.response?.data === 'object' && err.response?.data?.message) ||
          (typeof err.response?.data === 'string' ? err.response.data : null) ||
          err.message
        setError(`${t('parties.customerSaveError')}: ${serverMessage}`)
      } else if (err instanceof Error) {
        setError(`${t('parties.customerSaveError')}: ${err.message}`)
      } else {
        setError(t('parties.customerSaveError'))
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <main className="px-5 py-6 md:px-8 md:py-8">
      <section className="mb-5 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="m-0 mb-1 text-sm text-ink-soft">{t('parties.eyebrow')}</p>
          <h2 className="m-0 font-display text-3xl font-medium text-ink">{t('parties.customerTitle')}</h2>
        </div>
        <div className="rounded-[10px] border border-rule bg-paper-raised px-4 py-3">
          <p className="m-0 mb-1 text-xs text-ink-soft">{t('parties.customerTotal')}</p>
          <p className="m-0 font-mono text-xl font-medium text-debit">
            {formatRupees(totalOutstanding)}
          </p>
        </div>
      </section>

      {error && (
        <div className="mb-4 rounded-[10px] border border-rule bg-debit-bg px-4 py-3 text-sm text-debit">
          {error}
        </div>
      )}

      <section className="grid grid-cols-1 overflow-hidden rounded-[10px] border border-rule bg-paper-raised md:grid-cols-[1.45fr_0.85fr]">
        <div className="border-b border-rule md:border-b-0 md:border-r">
          <div className="flex flex-col gap-3 border-b border-rule px-4 py-4 md:flex-row md:items-center md:justify-between">
            <div>
              <h3 className="m-0 font-display text-lg font-medium">{t('parties.ledgerSpread')}</h3>
              <p className="m-0 mt-1 text-sm text-ink-soft">{t('parties.customerHelp')}</p>
            </div>
            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder={t('common.searchNamePhoneCity')}
              className="w-full rounded-lg border border-rule bg-paper px-3 py-2 text-sm outline-none md:max-w-72"
            />
          </div>

          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left text-sm">
              <thead>
                <tr className="border-b border-rule text-xs uppercase tracking-normal text-ink-soft">
                  <th className="px-4 py-3 font-medium">{t('common.name')}</th>
                  <th className="px-4 py-3 font-medium">{t('common.phone')}</th>
                  <th className="px-4 py-3 font-medium">{t('common.city')}</th>
                  <th className="px-4 py-3 text-right font-medium">{t('common.balance')}</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td className="px-4 py-6 text-center text-ink-soft" colSpan={4}>
                      {t('parties.loadingCustomers')}
                    </td>
                  </tr>
                ) : filteredCustomers.length === 0 ? (
                  <tr>
                    <td className="px-4 py-6 text-center text-ink-soft" colSpan={4}>
                      {t('parties.noCustomers')}
                    </td>
                  </tr>
                ) : (
                  filteredCustomers.map((customer) => (
                    <tr key={customer.id} className="border-b border-rule last:border-b-0">
                      <td className="px-4 py-3">
                        <p className="m-0 font-medium text-ink">{customer.name}</p>
                        {customer.gstNumber && (
                          <p className="m-0 mt-1 text-xs text-ink-soft">
                            {t('common.gstPrefix')}{customer.gstNumber}
                          </p>
                        )}
                      </td>
                      <td className="px-4 py-3 font-mono text-xs text-ink">{customer.phone}</td>
                      <td className="px-4 py-3 text-ink-soft">{customer.city}</td>
                      <td
                        className={[
                          'px-4 py-3 text-right font-mono font-medium',
                          customer.balance !== null && customer.balance > 0 ? 'text-debit' : 'text-credit',
                        ].join(' ')}
                      >
                        {formatRupees(customer.balance)}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="px-4 py-4">
          <h3 className="m-0 font-display text-lg font-medium">{t('parties.addCustomer')}</h3>
          <p className="m-0 mt-1 text-sm text-ink-soft">{t('parties.customerFormHelp')}</p>

          <div className="mt-5 grid gap-3">
            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.name')}</span>
              <input
                required
                value={form.name}
                onChange={(event) => setForm({ ...form, name: event.target.value })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              />
            </label>
            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.phone')}</span>
              <input
                required
                maxLength={15}
                value={form.phone}
                onChange={(event) => setForm({ ...form, phone: event.target.value })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              />
            </label>
            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.city')}</span>
              <input
                required
                value={form.city}
                onChange={(event) => setForm({ ...form, city: event.target.value })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              />
            </label>
            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.openingBalance')}</span>
              <input
                required
                type="number"
                step="0.01"
                value={form.openingBalance}
                onChange={(event) => setForm({ ...form, openingBalance: event.target.value })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              />
            </label>
            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.gstOptional')}</span>
              <input
                value={form.gstNumber}
                onChange={(event) => setForm({ ...form, gstNumber: event.target.value })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              />
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
            disabled={saving}
            className="mt-5 w-full rounded-lg border border-cloth bg-cloth px-4 py-2.5 text-sm font-medium text-paper disabled:opacity-60"
          >
            {saving ? t('common.saving') : t('parties.saveCustomer')}
          </button>
        </form>
      </section>
    </main>
  )
}
