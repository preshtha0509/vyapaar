import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { NavLink } from 'react-router-dom'

type NavItem = {
  labelKey: string
  to: string
  icon?: string
}

const navItems: NavItem[] = [
  { labelKey: 'nav.dashboard', to: '/dashboard' },
  { labelKey: 'nav.customers', to: '/customers' },
  { labelKey: 'nav.suppliers', to: '/suppliers' },
  { labelKey: 'nav.products', to: '/products' },
  { labelKey: 'nav.sales', to: '/sales/new' },
  { labelKey: 'nav.purchases', to: '/purchases/new' },
  { labelKey: 'nav.payments', to: '/payments' },
  { labelKey: 'nav.reports', to: '/reports' },
]

export function Sidebar() {
  const { t } = useTranslation()
  const [mobileOpen, setMobileOpen] = useState(false)

  return (
    <>
      {/* Mobile Top Bar */}
      <div className="flex items-center justify-between border-b border-rule bg-paper px-4 py-3 md:hidden">
        <h1 className="m-0 font-display text-xl font-medium tracking-tight text-cloth">
          VYAPAAR
        </h1>
        <button
          type="button"
          onClick={() => setMobileOpen(!mobileOpen)}
          className="rounded-lg border border-rule bg-paper-raised px-3 py-1.5 text-xs font-medium text-ink"
        >
          {mobileOpen ? t('common.close') : t('common.menu')}
        </button>
      </div>

      {/* Vertical Sidebar for Desktop & Mobile Collapsible */}
      <aside
        className={[
          'w-full border-b border-rule bg-paper px-4 py-5 md:w-64 md:min-h-screen md:flex-shrink-0 md:border-b-0 md:border-r',
          mobileOpen ? 'block' : 'hidden md:block',
        ].join(' ')}
      >
        <div className="mb-6 px-3 hidden md:block">
          <h1 className="m-0 font-display text-2xl font-medium tracking-tight text-cloth">
            VYAPAAR
          </h1>
        </div>

        <nav className="flex flex-col gap-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) =>
                [
                  'flex items-center rounded-lg px-3 py-2.5 text-sm font-medium transition-colors no-underline',
                  isActive
                    ? 'border-l-4 border-cloth bg-paper-raised text-cloth font-semibold shadow-xs pl-2.5'
                    : 'text-ink-soft hover:bg-paper-raised hover:text-ink',
                ].join(' ')
              }
            >
              {t(item.labelKey)}
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  )
}
