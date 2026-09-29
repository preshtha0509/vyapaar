import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { api } from '../api/client'
import { BillView } from '../components/BillView'
import type { Customer, Product, Sale, SaleForm, SalePaymentStatus } from '../types'

const today = new Date().toISOString().slice(0, 10)

const emptyForm: SaleForm = {
  customerId: '',
  productId: '',
  quantity: '',
  rate: '',
  saleDate: today,
  paymentStatus: 'UDHARI',
  notes: '',
}

const currency = new Intl.NumberFormat('en-IN', {
  maximumFractionDigits: 2,
  minimumFractionDigits: 0,
})

function formatRupees(value: number) {
  return `₹${currency.format(value)}`
}

export function NewSalePage() {
  const { t } = useTranslation()
  const [customers, setCustomers] = useState<Customer[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [form, setForm] = useState<SaleForm>(emptyForm)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [savedSale, setSavedSale] = useState<Sale | null>(null)
  const [viewBillSaleId, setViewBillSaleId] = useState<number | null>(null)

  function unitLabel(unit: Product['unit']) {
    switch (unit) {
      case 'KG':
        return t('products.unitKg')
      case 'QUINTAL':
        return t('products.unitQuintal')
      case 'BAG':
        return t('products.unitBag')
    }
  }

  async function loadOptions() {
    setLoading(true)
    setError(null)
    try {
      const [customerResponse, productResponse] = await Promise.all([
        api.get<Customer[]>('/api/customers'),
        api.get<Product[]>('/api/products'),
      ])
      setCustomers(customerResponse.data)
      setProducts(productResponse.data)
    } catch {
      setError(t('transactions.saleLoadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadOptions()
  }, [])

  const selectedCustomer = useMemo(
    () => customers.find((customer) => String(customer.id) === form.customerId),
    [customers, form.customerId],
  )

  const selectedProduct = useMemo(
    () => products.find((product) => String(product.id) === form.productId),
    [products, form.productId],
  )

  const quantity = Number(form.quantity || 0)
  const rate = Number(form.rate || 0)
  const totalPreview = quantity * rate

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    setSavedSale(null)

    if (!form.customerId || !form.productId) {
      setError(t('transactions.saleSelectError'))
      setSaving(false)
      return
    }

    try {
      const response = await api.post<Sale>('/api/sales', {
        customer: { id: Number(form.customerId) },
        product: { id: Number(form.productId) },
        quantity,
        rate,
        saleDate: form.saleDate,
        paymentStatus: form.paymentStatus,
        ...(form.notes.trim() ? { notes: form.notes.trim() } : {}),
      })
      setSavedSale(response.data)
      setForm(emptyForm)
    } catch {
      setError(t('transactions.saleSaveError'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <main className="px-5 py-6 md:px-8 md:py-8">
      <section className="mb-5 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="m-0 mb-1 text-sm text-ink-soft">{t('transactions.entry')}</p>
          <h2 className="m-0 font-display text-3xl font-medium text-ink">{t('transactions.newSale')}</h2>
        </div>
        <div className="rounded-[10px] border border-rule bg-paper-raised px-4 py-3">
          <p className="m-0 mb-1 text-xs text-ink-soft">{t('common.calculatedTotal')}</p>
          <p className="m-0 font-mono text-xl font-medium text-credit">
            {formatRupees(totalPreview)}
          </p>
        </div>
      </section>

      {error && (
        <div className="mb-4 rounded-[10px] border border-rule bg-debit-bg px-4 py-3 text-sm text-debit">
          {error}
        </div>
      )}

      {savedSale && (
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3 rounded-[10px] border border-rule bg-credit-bg px-4 py-3 text-sm text-credit">
          <span>
            {t('transactions.saleSaved', { id: savedSale.id, total: formatRupees(savedSale.total) })}
          </span>
          <button
            type="button"
            onClick={() => setViewBillSaleId(savedSale.id)}
            className="rounded-md border border-credit bg-paper px-3 py-1 text-xs font-medium text-credit hover:bg-paper-raised"
          >
            {t('transactions.viewBill')}
          </button>
        </div>
      )}

      <section className="grid grid-cols-1 overflow-hidden rounded-[10px] border border-rule bg-paper-raised md:grid-cols-[1fr_0.8fr]">
        <form onSubmit={handleSubmit} className="border-b border-rule px-4 py-4 md:border-b-0 md:border-r">
          <h3 className="m-0 font-display text-lg font-medium">{t('transactions.saleDetails')}</h3>
          <p className="m-0 mt-1 text-sm text-ink-soft">{t('transactions.saleHelp')}</p>

          <div className="mt-5 grid gap-3 md:grid-cols-2">
            <label className="grid gap-1.5 text-sm md:col-span-2">
              <span className="text-xs text-ink-soft">{t('common.customer')}</span>
              <select
                required
                value={form.customerId}
                onChange={(event) => setForm({ ...form, customerId: event.target.value })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              >
                <option value="">
                  {loading ? t('transactions.loadingCustomers') : t('transactions.selectCustomer')}
                </option>
                {customers.map((customer) => (
                  <option key={customer.id} value={customer.id}>
                    {customer.name} - {customer.city}
                  </option>
                ))}
              </select>
            </label>

            <label className="grid gap-1.5 text-sm md:col-span-2">
              <span className="text-xs text-ink-soft">{t('common.product')}</span>
              <select
                required
                value={form.productId}
                onChange={(event) => setForm({ ...form, productId: event.target.value })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              >
                <option value="">
                  {loading ? t('transactions.loadingProducts') : t('transactions.selectProduct')}
                </option>
                {products.map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.name} - {unitLabel(product.unit)}
                  </option>
                ))}
              </select>
            </label>

            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.quantity')}</span>
              <input
                required
                min="0.01"
                type="number"
                step="0.01"
                value={form.quantity}
                onChange={(event) => setForm({ ...form, quantity: event.target.value })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              />
            </label>

            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.rate')}</span>
              <input
                required
                min="0.01"
                type="number"
                step="0.01"
                value={form.rate}
                onChange={(event) => setForm({ ...form, rate: event.target.value })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              />
            </label>

            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('transactions.saleDate')}</span>
              <input
                required
                type="date"
                value={form.saleDate}
                onChange={(event) => setForm({ ...form, saleDate: event.target.value })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              />
            </label>

            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.paymentStatus')}</span>
              <select
                required
                value={form.paymentStatus}
                onChange={(event) =>
                  setForm({ ...form, paymentStatus: event.target.value as SalePaymentStatus })
                }
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              >
                <option value="UDHARI">{t('common.udhari')}</option>
                <option value="CASH">{t('common.cash')}</option>
              </select>
            </label>

            <label className="grid gap-1.5 text-sm md:col-span-2">
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
            {saving ? t('transactions.saving') : t('transactions.saveSale')}
          </button>
        </form>

        <aside className="px-4 py-4">
          <h3 className="m-0 font-display text-lg font-medium">{t('transactions.preview')}</h3>
          <div className="mt-5 grid gap-3">
            <div className="rounded-[10px] border border-rule bg-paper px-4 py-3">
              <p className="m-0 mb-1 text-xs text-ink-soft">{t('common.customer')}</p>
              <p className="m-0 text-sm font-medium">{selectedCustomer?.name ?? t('common.notSelected')}</p>
            </div>
            <div className="rounded-[10px] border border-rule bg-paper px-4 py-3">
              <p className="m-0 mb-1 text-xs text-ink-soft">{t('common.product')}</p>
              <p className="m-0 text-sm font-medium">{selectedProduct?.name ?? t('common.notSelected')}</p>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="rounded-[10px] border border-rule bg-paper px-4 py-3">
                <p className="m-0 mb-1 text-xs text-ink-soft">{t('common.quantity')}</p>
                <p className="m-0 font-mono text-sm">{quantity || 0}</p>
              </div>
              <div className="rounded-[10px] border border-rule bg-paper px-4 py-3">
                <p className="m-0 mb-1 text-xs text-ink-soft">{t('common.rate')}</p>
                <p className="m-0 font-mono text-sm">{formatRupees(rate)}</p>
              </div>
            </div>
            <div
              className={[
                'rounded-[10px] border border-rule px-4 py-3',
                form.paymentStatus === 'UDHARI' ? 'bg-debit-bg text-debit' : 'bg-credit-bg text-credit',
              ].join(' ')}
            >
              <p className="m-0 mb-1 text-xs">{t('common.status')}</p>
              <p className="m-0 font-mono text-lg font-medium">
                {form.paymentStatus === 'UDHARI' ? t('common.udhari') : t('common.cash')}
              </p>
            </div>
          </div>
        </aside>
      </section>

      {viewBillSaleId && (
        <BillView saleId={viewBillSaleId} onClose={() => setViewBillSaleId(null)} />
      )}
    </main>
  )
}
