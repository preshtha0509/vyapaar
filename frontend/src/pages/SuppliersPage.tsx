import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { api } from '../api/client'
import type { Supplier, SupplierForm } from '../types'

type SupplierRow = Supplier & {
  balance: number | null
}

const emptyForm: SupplierForm = {
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

export function SuppliersPage() {
  const { t } = useTranslation()
  const [suppliers, setSuppliers] = useState<SupplierRow[]>([])
  const [form, setForm] = useState<SupplierForm>(emptyForm)
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function formatRupees(value: number | null) {
    if (value === null) return t('common.checking')
    return `₹${currency.format(value)}`
  }

  async function loadSuppliers() {
    setLoading(true)
    setError(null)
    try {
      const response = await api.get<Supplier[]>('/api/suppliers')
      const rows = response.data.map((supplier) => ({ ...supplier, balance: null }))
      setSuppliers(rows)

      const balances = await Promise.all(
        rows.map(async (supplier) => {
          try {
            const balance = await api.get<number>(`/api/ledger/supplier/${supplier.id}/balance`)
            return { id: supplier.id, balance: balance.data }
          } catch {
            return { id: supplier.id, balance: null }
          }
        }),
      )

      setSuppliers((current) =>
        current.map((supplier) => {
          const match = balances.find((balance) => balance.id === supplier.id)
          return match ? { ...supplier, balance: match.balance } : supplier
        }),
      )
    } catch {
      setError(t('parties.supplierLoadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadSuppliers()
  }, [])

  const filteredSuppliers = useMemo(() => {
    const query = search.trim().toLowerCase()
    if (!query) return suppliers
    return suppliers.filter((supplier) =>
      [supplier.name, supplier.phone, supplier.city, supplier.gstNumber ?? '']
        .join(' ')
        .toLowerCase()
        .includes(query),
    )
  }, [suppliers, search])

  const totalPayable = useMemo(
    () =>
      suppliers.reduce((sum, supplier) => {
        if (supplier.balance === null || supplier.balance <= 0) return sum
        return sum + supplier.balance
      }, 0),
    [suppliers],
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

      await api.post('/api/suppliers', payload)
      setForm(emptyForm)
      await loadSuppliers()
    } catch {
      setError(t('parties.supplierSaveError'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <main className="px-5 py-6 md:px-8 md:py-8">
      <section className="mb-5 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="m-0 mb-1 text-sm text-ink-soft">{t('parties.eyebrow')}</p>
          <h2 className="m-0 font-display text-3xl font-medium text-ink">{t('parties.supplierTitle')}</h2>
        </div>
        <div className="rounded-[10px] border border-rule bg-paper-raised px-4 py-3">
          <p className="m-0 mb-1 text-xs text-ink-soft">{t('parties.supplierTotal')}</p>
          <p className="m-0 font-mono text-xl font-medium text-debit">
            {formatRupees(totalPayable)}
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
              <p className="m-0 mt-1 text-sm text-ink-soft">{t('parties.supplierHelp')}</p>
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
                  <th className="px-4 py-3 text-right font-medium">{t('common.payable')}</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td className="px-4 py-6 text-center text-ink-soft" colSpan={4}>
                      {t('parties.loadingSuppliers')}
                    </td>
                  </tr>
                ) : filteredSuppliers.length === 0 ? (
                  <tr>
                    <td className="px-4 py-6 text-center text-ink-soft" colSpan={4}>
                      {t('parties.noSuppliers')}
                    </td>
                  </tr>
                ) : (
                  filteredSuppliers.map((supplier) => (
                    <tr key={supplier.id} className="border-b border-rule last:border-b-0">
                      <td className="px-4 py-3">
                        <p className="m-0 font-medium text-ink">{supplier.name}</p>
                        {supplier.gstNumber && (
                          <p className="m-0 mt-1 text-xs text-ink-soft">
                            {t('common.gstPrefix')}{supplier.gstNumber}
                          </p>
                        )}
                      </td>
                      <td className="px-4 py-3 font-mono text-xs text-ink">{supplier.phone}</td>
                      <td className="px-4 py-3 text-ink-soft">{supplier.city}</td>
                      <td
                        className={[
                          'px-4 py-3 text-right font-mono font-medium',
                          supplier.balance !== null && supplier.balance > 0 ? 'text-debit' : 'text-credit',
                        ].join(' ')}
                      >
                        {formatRupees(supplier.balance)}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="px-4 py-4">
          <h3 className="m-0 font-display text-lg font-medium">{t('parties.addSupplier')}</h3>
          <p className="m-0 mt-1 text-sm text-ink-soft">{t('parties.supplierFormHelp')}</p>

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
            {saving ? t('common.saving') : t('parties.saveSupplier')}
          </button>
        </form>
      </section>
    </main>
  )
}
