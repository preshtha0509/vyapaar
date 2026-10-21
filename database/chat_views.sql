-- Module 14: balance views used by the AI chat assistant.
--
-- These repeat the LedgerService formula so the assistant never has to invent a balance column:
--   customer balance = opening balance + sales - payments received
--   supplier balance = opening balance + purchases - payments made
--
-- Hibernate's ddl-auto does not create views, so run this file by hand once on every database
-- (local MySQL, and again on Aiven). Re-running it is safe: CREATE OR REPLACE.

CREATE OR REPLACE VIEW v_customer_balance AS
SELECT
    c.id   AS customer_id,
    c.name AS customer_name,
    c.city AS city,
    (
        c.opening_balance
        + COALESCE((SELECT SUM(s.total) FROM sales s WHERE s.customer_id = c.id), 0)
        - COALESCE((SELECT SUM(p.amount) FROM payments p
                    WHERE p.party_type = 'CUSTOMER' AND p.party_id = c.id), 0)
    ) AS balance
FROM customers c;

CREATE OR REPLACE VIEW v_supplier_balance AS
SELECT
    sp.id   AS supplier_id,
    sp.name AS supplier_name,
    sp.city AS city,
    (
        sp.opening_balance
        + COALESCE((SELECT SUM(pu.total) FROM purchases pu WHERE pu.supplier_id = sp.id), 0)
        - COALESCE((SELECT SUM(pay.amount) FROM payments pay
                    WHERE pay.party_type = 'SUPPLIER' AND pay.party_id = sp.id), 0)
    ) AS balance
FROM suppliers sp;

-- Check before going further: pick one customer and compare this view's balance with what
-- GET /api/ledger/customer/{id}/balance returns. They must match exactly.
--   SELECT * FROM v_customer_balance ORDER BY balance DESC;
--   SELECT * FROM v_supplier_balance ORDER BY balance DESC;
