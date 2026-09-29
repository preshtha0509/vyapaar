import { lazy, Suspense } from 'react'
import { useTranslation } from 'react-i18next'
import { Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './components/AppShell'
import { CustomersPage } from './pages/CustomersPage'
import { DashboardPage } from './pages/DashboardPage'
import { NewPurchasePage } from './pages/NewPurchasePage'
import { NewSalePage } from './pages/NewSalePage'
import { PaymentsPage } from './pages/PaymentsPage'
import { ProductsPage } from './pages/ProductsPage'
import { SuppliersPage } from './pages/SuppliersPage'

const ReportsPage = lazy(async () => {
  const module = await import('./pages/ReportsPage')
  return { default: module.ReportsPage }
})

function ReportsLoader() {
  const { t } = useTranslation()

  return (
    <div className="px-5 py-8 text-center text-sm text-ink-soft" role="status">
      {t('reports.loading')}
    </div>
  )
}

function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="customers" element={<CustomersPage />} />
        <Route path="suppliers" element={<SuppliersPage />} />
        <Route path="products" element={<ProductsPage />} />
        <Route path="sales/new" element={<NewSalePage />} />
        <Route path="purchases/new" element={<NewPurchasePage />} />
        <Route path="payments" element={<PaymentsPage />} />
        <Route
          path="reports"
          element={
            <Suspense fallback={<ReportsLoader />}>
              <ReportsPage />
            </Suspense>
          }
        />
      </Route>
    </Routes>
  )
}

export default App
