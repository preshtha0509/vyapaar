"""
Generates database/sample_data.sql for VYAPAAR.

Deterministic: a fixed seed means re-running produces an identical file, so the SQL can be
committed and reviewed like any other source.

Shape of the data is chosen so every report in the app has something real to show:
  - sales spread over 7 months          -> monthly trend and sales-growth (LAG) reports
  - purchases spread across age buckets -> supplier payment aging (0-30 / 31-60 / 61-90 / 90+)
  - a realistic spread of udhari        -> top-customers-by-balance, dashboard totals
  - stock derived from purchases-sales  -> products page, negative-stock badge
"""
import random
from datetime import date, timedelta

random.seed(20261021)

TODAY = date(2026, 10, 21)
START = date(2026, 4, 1)          # Indian financial year start
SPAN = (TODAY - START).days

CUSTOMERS = [
    ("Sharma Traders", "Nagpur"), ("Verma Kirana Stores", "Wardha"),
    ("Gupta Provision", "Amravati"), ("Jain Brothers", "Akola"),
    ("Agrawal Trading Co", "Nagpur"), ("Patil General Stores", "Chandrapur"),
    ("Deshmukh Kirana", "Yavatmal"), ("Bansal Agro Mart", "Hinganghat"),
    ("Khandelwal Stores", "Nagpur"), ("Mehta Sabzi Mandi", "Bhandara"),
    ("Rathi Wholesale", "Gondia"), ("Chandak Trading", "Katol"),
    ("Joshi Kirana Bhandar", "Wardha"), ("Lahoti Provision", "Amravati"),
    ("Mundhada Stores", "Akola"), ("Pardeshi Vegetables", "Nagpur"),
    ("Thakur Agro", "Chandrapur"), ("Bhutada Traders", "Yavatmal"),
    ("Maheshwari Mart", "Nagpur"), ("Sarda Kirana", "Bhandara"),
    ("Toshniwal Stores", "Hinganghat"), ("Kabra Wholesale", "Gondia"),
    ("Dhoot Provision", "Wardha"), ("Nandurkar Kirana", "Katol"),
    ("Ingle Vegetable Mart", "Amravati"), ("Shrivastav Stores", "Nagpur"),
    ("Pande Trading Co", "Akola"), ("Kalantri Agro", "Yavatmal"),
    ("Zanwar Provision", "Nagpur"), ("Wankhede Kirana", "Chandrapur"),
]

SUPPLIERS = [
    ("Patil Farms", "Nashik"), ("Shinde Agro Produce", "Pune"),
    ("Jadhav Krushi Kendra", "Satara"), ("More Farms", "Sangli"),
    ("Kale Agro", "Solapur"), ("Pawar Brothers Farm", "Ahmednagar"),
    ("Salunkhe Produce", "Nashik"), ("Gaikwad Farms", "Pune"),
    ("Bhosale Agro", "Chhatrapati Sambhajinagar"), ("Nikam Krushi", "Satara"),
    ("Chavan Farms", "Solapur"), ("Thorat Agro Supply", "Ahmednagar"),
]

# (name, unit, rate_low, rate_high, qty_low, qty_high)
PRODUCTS = [
    ("Garlic Grade A",     "KG",      140, 230,  10, 160),
    ("Garlic Grade B",     "KG",      100, 160,  15, 200),
    ("Garlic Grade C",     "KG",       70, 115,  20, 250),
    ("Garlic Bulk Lot",    "QUINTAL", 9000, 21000, 1,  10),
    ("Garlic Peeled",      "BAG",      800, 1700,  2,  30),
    ("Garlic Seed Bulb",   "BAG",      950, 1900,  2,  22),
    ("Garlic Dried Flake", "BAG",      600, 1250,  3,  35),
    ("Garlic Small Grade", "KG",        55,  95,  25, 300),
]

MODES = ["CASH", "UPI", "UPI", "BANK_TRANSFER", "CHEQUE"]   # UPI weighted, it is the common one


def phone():
    return str(random.choice([6, 7, 8, 9])) + "".join(str(random.randint(0, 9)) for _ in range(9))


def a_day(skew_recent=False):
    """A date between START and TODAY. skew_recent biases towards the last two months."""
    if skew_recent and random.random() < 0.45:
        return TODAY - timedelta(days=random.randint(0, 60))
    return START + timedelta(days=random.randint(0, SPAN))


def money(x):
    return round(x, 2)


def q(s):
    return "'" + s.replace("'", "''") + "'"


lines = []
w = lines.append

w("-- VYAPAAR sample data. GENERATED FILE -- do not edit by hand.")
w("--")
w("-- Realistic demo data for a wholesale garlic business, covering 2026-04-01 to 2026-10-21")
w("-- so that every report has something to show.")
w("--")
w("-- WARNING: this DELETES everything in the six business tables first. Never run it on a")
w("-- database holding real books. It is safe on a demo or a fresh database.")
w("--")
w("-- Load it with:")
w("--   mysql --host=HOST --port=PORT --user=avnadmin --password --ssl-mode=REQUIRED \\")
w("--         vyapaar < database/sample_data.sql")
w("")
w("SET FOREIGN_KEY_CHECKS = 0;")
w("DELETE FROM payments;")
w("DELETE FROM sales;")
w("DELETE FROM purchases;")
w("DELETE FROM products;")
w("DELETE FROM customers;")
w("DELETE FROM suppliers;")
w("ALTER TABLE payments  AUTO_INCREMENT = 1;")
w("ALTER TABLE sales     AUTO_INCREMENT = 1;")
w("ALTER TABLE purchases AUTO_INCREMENT = 1;")
w("ALTER TABLE products  AUTO_INCREMENT = 1;")
w("ALTER TABLE customers AUTO_INCREMENT = 1;")
w("ALTER TABLE suppliers AUTO_INCREMENT = 1;")
w("SET FOREIGN_KEY_CHECKS = 1;")
w("")

# ---------------------------------------------------------------- customers
w("-- " + "-" * 70)
w("-- Customers: shopkeepers and mandi traders who buy on credit (udhari).")
w("-- " + "-" * 70)
rows = []
for i, (name, city) in enumerate(CUSTOMERS, start=1):
    opening = 0.0 if random.random() < 0.72 else money(random.randint(5, 90) * 100)
    gst = ("27" + "".join(str(random.randint(0, 9)) for _ in range(7))
           + "".join(random.choice("ABCDEFGHJKLMNPQRSTUVWXYZ") for _ in range(5)) + "1Z"
           + str(random.randint(0, 9))) if random.random() < 0.45 else None
    created = START - timedelta(days=random.randint(30, 400))
    rows.append("(%d, %s, %s, %s, %s, %s, NULL, '%s 09:%02d:00')" % (
        i, q(name), q(phone()), q(city), opening,
        q(gst) if gst else "NULL", created.isoformat(), random.randint(0, 59)))
w("INSERT INTO customers (id, name, phone, city, opening_balance, gst_number, notes, created_at) VALUES")
w(",\n".join(rows) + ";")
w("")

# ---------------------------------------------------------------- suppliers
w("-- " + "-" * 70)
w("-- Suppliers: farms and krushi kendras the business buys from.")
w("-- " + "-" * 70)
rows = []
for i, (name, city) in enumerate(SUPPLIERS, start=1):
    opening = 0.0 if random.random() < 0.8 else money(random.randint(10, 120) * 100)
    created = START - timedelta(days=random.randint(60, 500))
    rows.append("(%d, %s, %s, %s, %s, NULL, NULL, '%s 10:%02d:00')" % (
        i, q(name), q(phone()), q(city), opening, created.isoformat(), random.randint(0, 59)))
w("INSERT INTO suppliers (id, name, phone, city, opening_balance, gst_number, notes, created_at) VALUES")
w(",\n".join(rows) + ";")
w("")

# ---------------------------------------------------------------- transactions
# Build sales and purchases first, so stock and restock dates can be derived from them.
sales = []
for _ in range(480):
    cid = random.randint(1, len(CUSTOMERS))
    pid = random.randint(1, len(PRODUCTS))
    _, _, lo, hi, qlo, qhi = PRODUCTS[pid - 1]
    quantity = money(random.uniform(qlo, qhi)) if PRODUCTS[pid - 1][1] == "KG" else float(random.randint(qlo, qhi))
    rate = money(random.uniform(lo, hi))
    d = a_day()
    status = "CASH" if random.random() < 0.38 else "UDHARI"
    sales.append((cid, pid, quantity, rate, money(quantity * rate), d, status))

purchases = []
for _ in range(220):
    sid = random.randint(1, len(SUPPLIERS))
    pid = random.randint(1, len(PRODUCTS))
    _, _, lo, hi, qlo, qhi = PRODUCTS[pid - 1]
    # buy in larger lots than you sell, at a lower rate
    quantity = money(random.uniform(qlo, qhi) * 2.2) if PRODUCTS[pid - 1][1] == "KG" else float(random.randint(qlo, qhi) * 2)
    rate = money(random.uniform(lo, hi) * 0.78)
    d = a_day()
    status = "PAID" if random.random() < 0.55 else "PENDING"
    purchases.append((sid, pid, quantity, rate, money(quantity * rate), d, status))

# ---------------------------------------------------------------- products
w("-- " + "-" * 70)
w("-- Products. stock_quantity is derived from the purchases and sales below, so the")
w("-- Products page agrees with the transaction history instead of contradicting it.")
w("-- " + "-" * 70)
rows = []
for i, (name, unit, *_rest) in enumerate(PRODUCTS, start=1):
    bought = sum(p[2] for p in purchases if p[1] == i)
    sold = sum(s[2] for s in sales if s[1] == i)
    stock = money(bought - sold)
    if i == 3:                         # one product left short on purpose: the app allows
        stock = money(-random.uniform(15, 90))   # negative stock and the UI flags it in red
    elif stock < 0:
        stock = money(abs(stock) * 0.3 + random.uniform(20, 120))
    restock = max((p[5] for p in purchases if p[1] == i), default=None)
    rows.append("(%d, %s, %s, %s, %s, 1)" % (
        i, q(name), q(unit), stock,
        q(restock.isoformat()) if restock else "NULL"))
w("INSERT INTO products (id, name, unit, stock_quantity, last_restocked_date, active) VALUES")
w(",\n".join(rows) + ";")
w("")

# ---------------------------------------------------------------- purchases
w("-- " + "-" * 70)
w("-- Purchases: %d rows, spread so the supplier aging report fills all four buckets." % len(purchases))
w("-- " + "-" * 70)
rows = []
for i, (sid, pid, quantity, rate, total, d, status) in enumerate(purchases, start=1):
    rows.append("(%d, %d, %d, %s, %s, %s, %s, %s, NULL)" % (
        i, sid, pid, quantity, rate, total, q(d.isoformat()), q(status)))
w("INSERT INTO purchases (id, supplier_id, product_id, quantity, rate, total, purchase_date, payment_status, notes) VALUES")
w(",\n".join(rows) + ";")
w("")

# ---------------------------------------------------------------- sales
w("-- " + "-" * 70)
w("-- Sales: %d rows. One row per product, which is why a day's receipt groups several." % len(sales))
w("-- " + "-" * 70)
rows = []
for i, (cid, pid, quantity, rate, total, d, status) in enumerate(sales, start=1):
    rows.append("(%d, %d, %d, %s, %s, %s, %s, %s, NULL)" % (
        i, cid, pid, quantity, rate, total, q(d.isoformat()), q(status)))
w("INSERT INTO sales (id, customer_id, product_id, quantity, rate, total, sale_date, payment_status, notes) VALUES")
w(",\n".join(rows) + ";")
w("")

# ---------------------------------------------------------------- payments
# Customers pay back most but not all of what they owe, which is what creates the udhari spread.
w("-- " + "-" * 70)
w("-- Payments. Each customer settles a share of their sales, so balances are plausible:")
w("-- a few fully paid up, most part-paid, a handful who have paid nothing.")
w("-- " + "-" * 70)
payments = []
for cid in range(1, len(CUSTOMERS) + 1):
    owed = sum(s[4] for s in sales if s[0] == cid)
    if owed <= 0:
        continue
    roll = random.random()
    if roll < 0.10:
        share = 0.0                                  # has paid nothing yet
    elif roll < 0.28:
        share = random.uniform(0.97, 1.0)            # settled up
    else:
        share = random.uniform(0.35, 0.9)            # part paid
    target = owed * share
    paid = 0.0
    while paid < target - 500:
        amount = money(min(target - paid, random.uniform(2000, 45000)))
        if amount < 100:
            break
        payments.append(("CUSTOMER", cid, amount, a_day(skew_recent=True), random.choice(MODES)))
        paid += amount

for sid in range(1, len(SUPPLIERS) + 1):
    owed = sum(p[4] for p in purchases if p[0] == sid)
    if owed <= 0:
        continue
    target = owed * random.uniform(0.4, 0.95)
    paid = 0.0
    while paid < target - 1000:
        amount = money(min(target - paid, random.uniform(10000, 90000)))
        if amount < 500:
            break
        payments.append(("SUPPLIER", sid, amount, a_day(), random.choice(MODES)))
        paid += amount

random.shuffle(payments)
rows = []
for i, (ptype, pid, amount, d, mode) in enumerate(payments, start=1):
    rows.append("(%d, %s, %d, %s, %s, %s, NULL)" % (
        i, q(ptype), pid, amount, q(d.isoformat()), q(mode)))
w("INSERT INTO payments (id, party_type, party_id, amount, payment_date, mode, notes) VALUES")
w(",\n".join(rows) + ";")
w("")

total_rows = len(CUSTOMERS) + len(SUPPLIERS) + len(PRODUCTS) + len(sales) + len(purchases) + len(payments)
w("-- " + "-" * 70)
w("-- Totals: %d customers, %d suppliers, %d products, %d purchases, %d sales, %d payments"
  % (len(CUSTOMERS), len(SUPPLIERS), len(PRODUCTS), len(purchases), len(sales), len(payments)))
w("-- %d rows in all." % total_rows)
w("-- " + "-" * 70)

out = "D:/Projects/vyapaar/database/sample_data.sql"
with open(out, "w", encoding="utf-8", newline="\n") as fh:
    fh.write("\n".join(lines) + "\n")

print("wrote", out)
print("customers %d | suppliers %d | products %d | purchases %d | sales %d | payments %d"
      % (len(CUSTOMERS), len(SUPPLIERS), len(PRODUCTS), len(purchases), len(sales), len(payments)))
print("TOTAL ROWS:", total_rows)
