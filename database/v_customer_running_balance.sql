-- View for Customer Running Udhari Balance Over Time
CREATE OR REPLACE VIEW v_customer_running_balance AS
WITH customer_tx AS (
    SELECT 
        id AS customer_id, 
        DATE(created_at) AS transaction_date, 
        opening_balance AS amount
    FROM customers
    WHERE opening_balance <> 0

    UNION ALL

    SELECT 
        customer_id, 
        sale_date AS transaction_date, 
        total AS amount
    FROM sales

    UNION ALL

    SELECT 
        party_id AS customer_id, 
        payment_date AS transaction_date, 
        -amount AS amount
    FROM payments
    WHERE party_type = 'CUSTOMER'
)
SELECT 
    customer_id,
    transaction_date,
    SUM(amount) AS transaction_amount,
    SUM(SUM(amount)) OVER (PARTITION BY customer_id ORDER BY transaction_date ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_balance
FROM customer_tx
GROUP BY customer_id, transaction_date;
