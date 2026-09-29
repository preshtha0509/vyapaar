import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import axios from 'axios'
import { api } from '../api/client'
import { FreshnessBadge } from '../components/FreshnessBadge'
import { GarlicIcon } from '../components/GarlicIcon'
import type { Product, ProductForm, ProductUnit } from '../types'

const emptyForm: ProductForm = {
  name: '',
  unit: 'QUINTAL',
  openingStock: '',
}

const unitOptions: ProductUnit[] = ['KG', 'QUINTAL', 'BAG']

export function ProductsPage() {
  const { t } = useTranslation()
  const [products, setProducts] = useState<Product[]>([])
  const [form, setForm] = useState<ProductForm>(emptyForm)
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [deletingId, setDeletingId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  function unitLabel(unit: ProductUnit) {
    switch (unit) {
      case 'KG':
        return t('products.unitKg')
      case 'QUINTAL':
        return t('products.unitQuintal')
      case 'BAG':
        return t('products.unitBag')
    }
  }

  async function loadProducts() {
    setLoading(true)
    setError(null)
    try {
      const response = await api.get<Product[]>('/api/products')
      setProducts(response.data)
    } catch {
      setError(t('products.loadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadProducts()
  }, [])

  const filteredProducts = useMemo(() => {
    const query = search.trim().toLowerCase()
    if (!query) return products
    return products.filter((product) =>
      [product.name, product.unit].join(' ').toLowerCase().includes(query),
    )
  }, [products, search])

  const stockedCount = useMemo(
    () => products.filter((product) => product.ageInDays !== null).length,
    [products],
  )

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await api.post('/api/products', {
        name: form.name.trim(),
        unit: form.unit,
        stockQuantity: form.openingStock ? parseFloat(form.openingStock) : 0,
      })
      setForm(emptyForm)
      await loadProducts()
    } catch {
      setError(t('products.saveError'))
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(product: Product) {
    const confirmMessage = t('products.deleteConfirm', { name: product.name })
    if (!window.confirm(confirmMessage)) {
      return
    }

    setDeletingId(product.id)
    setError(null)
    try {
      await api.delete(`/api/products/${product.id}`)
      await loadProducts()
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        setError(String(err.response.data.message))
      } else {
        setError(t('products.deleteError'))
      }
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <main className="px-5 py-6 md:px-8 md:py-8">
      <section className="mb-5 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="m-0 mb-1 text-sm text-ink-soft">{t('products.eyebrow')}</p>
          <div className="flex items-center gap-2">
            <GarlicIcon className="h-5 w-5 text-cloth" />
            <h2 className="m-0 font-display text-3xl font-medium text-ink">{t('products.title')}</h2>
          </div>
        </div>
        <div className="rounded-[10px] border border-rule bg-paper-raised px-4 py-3">
          <p className="m-0 mb-1 text-xs text-ink-soft">{t('products.restocked')}</p>
          <p className="m-0 font-mono text-xl font-medium text-credit">
            {stockedCount} / {products.length}
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
              <h3 className="m-0 font-display text-lg font-medium">{t('products.list')}</h3>
              <p className="m-0 mt-1 text-sm text-ink-soft">{t('products.listHelp')}</p>
            </div>
            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder={t('products.search')}
              className="w-full rounded-lg border border-rule bg-paper px-3 py-2 text-sm outline-none md:max-w-72"
            />
          </div>

          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left text-sm">
              <thead>
                <tr className="border-b border-rule text-xs uppercase tracking-normal text-ink-soft">
                  <th className="px-4 py-3 font-medium">{t('common.name')}</th>
                  <th className="px-4 py-3 font-medium">{t('common.unit')}</th>
                  <th className="px-4 py-3 font-medium">{t('common.stock')}</th>
                  <th className="px-4 py-3 text-right font-medium">{t('common.freshness')}</th>
                  <th className="px-4 py-3 text-right font-medium">{t('common.status')}</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td className="px-4 py-6 text-center text-ink-soft" colSpan={5}>
                      {t('products.loading')}
                    </td>
                  </tr>
                ) : filteredProducts.length === 0 ? (
                  <tr>
                    <td className="px-4 py-6 text-center text-ink-soft" colSpan={5}>
                      {t('products.empty')}
                    </td>
                  </tr>
                ) : (
                  filteredProducts.map((product) => {
                    const stock = product.stockQuantity ?? 0
                    const isNegative = stock < 0
                    return (
                      <tr key={product.id} className="border-b border-rule last:border-b-0">
                        <td className="px-4 py-3">
                          <p className="m-0 font-medium text-ink">{product.name}</p>
                        </td>
                        <td className="px-4 py-3 font-mono text-xs text-ink">
                          {unitLabel(product.unit)}
                        </td>
                        <td className="px-4 py-3 font-mono text-xs text-ink">
                          {isNegative ? (
                            <span className="inline-block rounded-full bg-debit-bg px-2.5 py-1 text-xs font-medium text-debit">
                              {stock} {unitLabel(product.unit)}
                            </span>
                          ) : (
                            <span>
                              {stock} {unitLabel(product.unit)}
                            </span>
                          )}
                        </td>
                        <td className="px-4 py-3 text-right">
                          <FreshnessBadge ageInDays={product.ageInDays} />
                        </td>
                        <td className="px-4 py-3 text-right">
                          <button
                            type="button"
                            disabled={deletingId === product.id}
                            onClick={() => handleDelete(product)}
                            className="rounded-md border border-rule bg-paper px-2.5 py-1 text-xs font-medium text-debit hover:bg-debit-bg disabled:opacity-50"
                          >
                            {deletingId === product.id ? t('common.loading') : t('products.delete')}
                          </button>
                        </td>
                      </tr>
                    )
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="px-4 py-4">
          <h3 className="m-0 font-display text-lg font-medium">{t('products.add')}</h3>
          <p className="m-0 mt-1 text-sm text-ink-soft">{t('products.formHelp')}</p>

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
              <span className="text-xs text-ink-soft">{t('common.unit')}</span>
              <select
                required
                value={form.unit}
                onChange={(event) => setForm({ ...form, unit: event.target.value as ProductUnit })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              >
                {unitOptions.map((unit) => (
                  <option key={unit} value={unit}>
                    {unitLabel(unit)}
                  </option>
                ))}
              </select>
            </label>
            <label className="grid gap-1.5 text-sm">
              <span className="text-xs text-ink-soft">{t('common.openingStock')} ({t('common.notesOptional')})</span>
              <input
                type="number"
                step="any"
                placeholder="0"
                value={form.openingStock}
                onChange={(event) => setForm({ ...form, openingStock: event.target.value })}
                className="rounded-lg border border-rule bg-paper px-3 py-2 outline-none"
              />
            </label>
          </div>

          <button
            type="submit"
            disabled={saving}
            className="mt-5 w-full rounded-lg border border-cloth bg-cloth px-4 py-2.5 text-sm font-medium text-paper disabled:opacity-60"
          >
            {saving ? t('common.saving') : t('products.save')}
          </button>
        </form>
      </section>
    </main>
  )
}
