export type Customer = {
  id: number
  name: string
  phone: string
  city: string
  openingBalance: number
  gstNumber?: string | null
  notes?: string | null
  createdAt?: string
}

export type CustomerForm = {
  name: string
  phone: string
  city: string
  openingBalance: string
  gstNumber: string
  notes: string
}

export type Supplier = {
  id: number
  name: string
  phone: string
  city: string
  openingBalance: number
  gstNumber?: string | null
  notes?: string | null
  createdAt?: string
}

export type SupplierForm = {
  name: string
  phone: string
  city: string
  openingBalance: string
  gstNumber: string
  notes: string
}

export type ProductUnit = 'KG' | 'QUINTAL' | 'BAG'

export type Product = {
  id: number
  name: string
  unit: ProductUnit
  ageInDays: number | null
  stockQuantity: number | null
}

export type ProductForm = {
  name: string
  unit: ProductUnit
  openingStock: string
}

export type SalePaymentStatus = 'CASH' | 'UDHARI'

export type Sale = {
  id: number
  customer: Customer
  product: Product
  quantity: number
  rate: number
  total: number
  saleDate: string
  paymentStatus: SalePaymentStatus
  notes?: string | null
}

export type SaleForm = {
  customerId: string
  productId: string
  quantity: string
  rate: string
  saleDate: string
  paymentStatus: SalePaymentStatus
  notes: string
}

export type PurchasePaymentStatus = 'PAID' | 'PENDING'

export type Purchase = {
  id: number
  supplier: Supplier
  product: Product
  quantity: number
  rate: number
  total: number
  purchaseDate: string
  paymentStatus: PurchasePaymentStatus
  notes?: string | null
}

export type PurchaseForm = {
  supplierId: string
  productId: string
  quantity: string
  rate: string
  purchaseDate: string
  paymentStatus: PurchasePaymentStatus
  notes: string
}

export type PaymentPartyType = 'CUSTOMER' | 'SUPPLIER'

export type PaymentMode = 'CASH' | 'UPI' | 'BANK_TRANSFER' | 'CHEQUE'

export type Payment = {
  id: number
  partyType: PaymentPartyType
  partyId: number
  amount: number
  paymentDate: string
  mode: PaymentMode
  notes?: string | null
}

export type PaymentForm = {
  partyType: PaymentPartyType
  partyId: string
  amount: string
  paymentDate: string
  mode: PaymentMode
  notes: string
}

export type BalanceRow = {
  id: number
  name: string
  balance: number
}

export type DashboardSummary = {
  totalToReceive: number
  totalToPay: number
  topCustomers: BalanceRow[]
  topSuppliers: BalanceRow[]
}

export type ReportsSummary = {
  totalSales: number
  totalPurchases: number
  grossDifference: number
}

export type TrendPoint = {
  month: string
  sales: number
  purchases: number
}

export type SupplierVolumeRow = {
  supplier: string
  total: number
}
