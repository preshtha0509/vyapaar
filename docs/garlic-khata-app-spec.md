# Project Spec: Digital Khata App for Garlic Wholesale Business

> **Instructions for whoever/whatever reads this doc:** This is a complete build spec for a full-stack Java + SQL web application. Build it exactly as described below, section by section. If anything is ambiguous, prefer the simplest working implementation and note the assumption.

---

## 1. Project Name Options
Pick one (all are available-sounding, bilingual-friendly):
- **Khata+** (khata = ledger in Hindi, universally understood)
- **Lehna-Dena** (lit. "to take, to give" — describes udhari perfectly)
- **VyapaarKhata** (vyapaar = business)
- **Hisab Kitab** (means "accounts/reckoning" — very natural Hindi business phrase)
- **GarlicLedger** (if you want it to sound like a generic SaaS product for resume purposes)

Recommended: **Hisab Kitab** for the real product, **VyapaarKhata** if you want a more "startup-sounding" resume name.

---

## 2. Problem Statement (for resume/interview use)
A wholesale garlic trader manages customers, suppliers, stock, and credit (udhari) entirely on paper. This causes: no searchable history, error-prone balance tracking, no insight into who owes what, and no backup. This app digitizes the entire paper khata into a structured, queryable, bilingual system with live-calculated balances and a business insight dashboard.

---

## 3. Tech Stack

**Backend:**
- Java 17+
- Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Security for login)
- Maven or Gradle

**Database:**
- MySQL or PostgreSQL (PostgreSQL preferred for better analytics/window functions)
- Flyway or Liquibase for schema migrations

**Frontend:**
- React (Vite) + TypeScript
- TailwindCSS for styling
- Recharts for dashboard charts
- react-i18next for Hindi/English bilingual support

**Auth:**
- Simple JWT-based login (owner + optionally staff role)

**Deployment (resume-friendly, free-tier):**
- Backend: Render / Railway
- DB: Supabase (Postgres) / Railway Postgres
- Frontend: Vercel / Netlify

**Testing:**
- JUnit 5 + Mockito (backend)
- Basic API tests with Postman/REST Assured

---

## 4. Core Domain Concept — The Udhari (Credit) Logic

This is the most important design decision — explain this in interviews.

Do **NOT** store a single mutable "balance" field per customer/supplier. Instead:
- Every transaction (sale, purchase, payment) is an immutable ledger entry with a signed amount.
- The **balance is always computed** by summing ledger entries for that party, ordered by date.
- This mirrors how a real paper khata works (a running total you can always re-derive and audit), and avoids balance-drift bugs from partial updates.

Sign convention:
- Sale to customer → customer owes you more (+ to "receivable")
- Payment received from customer → reduces receivable
- Purchase from supplier → you owe supplier more (+ to "payable")
- Payment made to supplier → reduces payable

---

## 5. Database Schema (entities)

```sql
-- Master data
customers (id, name, phone, address, created_at)
suppliers (id, name, phone, address, created_at)
products (id, name, unit [kg/quintal/bag], created_at)

-- Transactions
purchases (id, supplier_id FK, product_id FK, quantity, rate, total_amount, purchase_date, notes)
sales (id, customer_id FK, product_id FK, quantity, rate, total_amount, sale_date, notes)

-- Money movement (the udhari core)
payments (
  id,
  party_type ENUM('customer','supplier'),
  party_id,          -- FK to customers.id or suppliers.id depending on party_type
  amount,
  direction ENUM('received','paid'),
  payment_date,
  mode ENUM('cash','upi','bank_transfer','cheque'),
  notes
)

users (id, username, password_hash, role ENUM('owner','staff'))
```

**Ledger view (computed, not stored)** — per customer/supplier:
```
running_balance = SUM(sales.total_amount) - SUM(payments WHERE direction='received')
```
```
running_balance_supplier = SUM(purchases.total_amount) - SUM(payments WHERE direction='paid')
```

Use a SQL VIEW or a backend service method to compute this per-party balance and full transaction history sorted by date — this is your "digital khata page" for that person.

---

## 6. Pages / Screens

### 6.1 Login Page
- Username/password. Language toggle (हिंदी / English) visible here too.

### 6.2 Dashboard (Home)
- Total to collect (sum of all customer balances > 0)
- Total to pay (sum of all supplier balances > 0)
- Top 5 customers by outstanding amount
- Top 5 suppliers you owe the most
- Monthly purchase vs sale trend (line/bar chart)
- Recent transactions feed
- Low-level stock/product volume summary (optional, if tracking inventory quantity)

### 6.3 Customers Page
- List with search + outstanding balance shown inline (color-coded: red = owes you)
- Add/Edit customer (name, phone, address)
- Click into a customer → **Customer Ledger Page**

### 6.4 Customer Ledger Page (per customer)
- Running balance at top
- Chronological table: date, type (sale/payment), amount, running total
- "Add Sale" and "Add Payment Received" buttons
- Export to PDF (for sending to customer if needed)

### 6.5 Suppliers Page
- Same pattern as Customers, but balance shown is what your father owes them

### 6.6 Supplier Ledger Page
- Same pattern as Customer Ledger, but with Purchases + Payments Made

### 6.7 Products Page
- List of products (mostly variants of garlic — e.g., "Garlic Grade A", "Garlic Grade B", "Peeled Garlic")
- Track unit of measure

### 6.8 New Purchase Page (form)
- Supplier (dropdown/search), Product, Quantity, Rate, Total (auto-calc), Date, Notes

### 6.9 New Sale Page (form)
- Customer (dropdown/search), Product, Quantity, Rate, Total (auto-calc), Date, Notes

### 6.10 Payments Page
- Record a payment received (from customer) or paid (to supplier)
- Party selector, amount, mode, date, notes

### 6.11 Reports/Insights Page (expanded dashboard)
- Date-range filter
- Total purchases vs total sales vs profit margin (if you also track selling rate vs buying rate)
- Best-paying vs slow-paying customers
- Supplier-wise purchase volume
- Export any report as CSV/PDF

### 6.12 Settings Page
- Language preference
- User/staff management (if multi-user)

---

## 7. Bilingual (Hindi/English) Implementation

- Use `react-i18next` with two JSON dictionaries: `en.json` and `hi.json`
- Keep all UI strings as translation keys, never hardcoded text, e.g. `t('dashboard.totalToCollect')`
- Store user's chosen language in local storage + user profile
- Numbers/dates: use `Intl.NumberFormat` and locale-aware date formatting
- Data itself (customer names, product names) stays as entered — no need to translate user data, only UI chrome

---

## 8. Suggested API Endpoints (REST)

```
POST   /api/auth/login
GET    /api/customers
POST   /api/customers
GET    /api/customers/{id}/ledger
GET    /api/suppliers
POST   /api/suppliers
GET    /api/suppliers/{id}/ledger
GET    /api/products
POST   /api/products
POST   /api/sales
POST   /api/purchases
POST   /api/payments
GET    /api/dashboard/summary
GET    /api/reports?from=&to=
```

---

## 9. Build Order (recommended sequence)

1. DB schema + migrations
2. Backend entities + repositories (JPA)
3. Core services: ledger calculation logic (this is the heart — test thoroughly)
4. REST controllers for customers/suppliers/products
5. Sales/Purchase/Payment endpoints
6. Dashboard summary endpoint (SQL aggregation queries)
7. Frontend scaffold + routing + i18n setup
8. Customer/Supplier list + ledger pages
9. Sale/Purchase/Payment forms
10. Dashboard charts (Recharts)
11. Auth + login flow
12. PDF/CSV export
13. Polish, deploy, seed with father's real (or anonymized sample) data

Estimated timeline: 8–9 days for a solid working version.

---

## 10. Resume/Interview Talking Points
- "Built a full-stack ledger system to digitize my father's wholesale garlic business, replacing paper khata records."
- "Designed an immutable, append-only transaction model so account balances are always derived and auditable, rather than stored as a mutable field — eliminating balance-drift bugs common in naive CRUD ledger apps."
- "Implemented bilingual UI (Hindi/English) using i18next for real-world usability by a non-English-first user."
- "Built SQL aggregation queries for a business analytics dashboard (receivables, payables, top debtors, monthly trends)."
