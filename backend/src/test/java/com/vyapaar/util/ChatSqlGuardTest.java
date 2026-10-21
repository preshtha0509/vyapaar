package com.vyapaar.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The guard is the one part of Module 14 that can be tested without an API key, a database, or
 * money. Every case here is a query the AI could plausibly produce.
 */
class ChatSqlGuardTest {

    private void accepts(String sql) {
        assertDoesNotThrow(() -> ChatSqlGuard.validate(sql), sql);
    }

    private void rejects(String sql) {
        assertThrows(IllegalArgumentException.class, () -> ChatSqlGuard.validate(sql), sql);
    }

    @Test
    void acceptsNormalReadQueries() {
        accepts("SELECT * FROM sales");
        accepts("SELECT * FROM sales;");
        accepts("select count(*) from payments where mode = 'UPI'");
        accepts("SELECT customer_name, balance FROM v_customer_balance ORDER BY balance DESC LIMIT 5");
        accepts("SELECT supplier_name, balance FROM v_supplier_balance WHERE balance > 0");
        accepts("SELECT s.sale_date, c.name AS customer, p.name AS product, s.total "
                + "FROM sales s JOIN customers c ON c.id = s.customer_id "
                + "JOIN products p ON p.id = s.product_id "
                + "WHERE s.sale_date >= '2026-07-01' AND s.sale_date < '2026-08-01' LIMIT 100");
        accepts("SELECT name, stock_quantity FROM products WHERE active = 1 ORDER BY stock_quantity ASC");
        accepts("WITH m AS (SELECT MONTH(sale_date) AS mth, SUM(total) AS t FROM sales GROUP BY MONTH(sale_date)) "
                + "SELECT * FROM m");
        accepts("SELECT name FROM customers WHERE id IN (SELECT customer_id FROM sales WHERE total > 1000)");
        accepts("SELECT name FROM customers WHERE name LIKE 'Sharma%'");
    }

    @Test
    void ignoresDangerousWordsInsideQuotes() {
        accepts("SELECT * FROM products WHERE name LIKE '%update%'");
        accepts("SELECT * FROM payments WHERE notes = 'delete this later'");
    }

    @Test
    void rejectsAnythingThatChangesData() {
        rejects("DELETE FROM sales");
        rejects("DROP TABLE sales");
        rejects("UPDATE sales SET total = 0");
        rejects("INSERT INTO sales (total) VALUES (1)");
        rejects("TRUNCATE TABLE sales");
        rejects("ALTER TABLE sales ADD COLUMN x INT");
        rejects("GRANT ALL ON vyapaar.* TO 'x'@'%'");
        rejects("SET @a = 1");
        rejects("SELECT * FROM sales FOR UPDATE");
    }

    @Test
    void rejectsStackedQueriesAndComments() {
        rejects("SELECT 1 FROM sales; DROP TABLE sales");
        rejects("SELECT * FROM sales -- hello");
        rejects("SELECT * FROM sales /* x */");
        rejects("SELECT * FROM sales # x");
    }

    @Test
    void rejectsDangerousFunctionsAndFiles() {
        rejects("SELECT * FROM sales INTO OUTFILE '/tmp/x'");
        rejects("SELECT SLEEP(10) FROM sales");
        rejects("SELECT BENCHMARK(1000000, MD5('x')) FROM sales");
        rejects("SELECT LOAD_FILE('/etc/passwd') FROM sales");
        rejects("SELECT GET_LOCK('a', 10) FROM sales");
    }

    @Test
    void rejectsTablesOutsideTheAllowList() {
        rejects("SELECT * FROM users");
        rejects("SELECT * FROM mysql.user");
        rejects("SELECT table_name FROM information_schema.tables");
        rejects("SELECT * FROM sales JOIN secrets ON 1=1");
        // Module 15's tables are deliberately out of scope: receipts.public_token is a credential.
        rejects("SELECT public_token FROM receipts");
        rejects("SELECT * FROM receipt_lines");
    }

    @Test
    void rejectsQuoteAndEscapeTricks() {
        rejects("SELECT * FROM sales WHERE notes = 'abc");
        rejects("SELECT * FROM `sales`");
        rejects("SELECT * FROM sales WHERE notes = 'a\\' OR 1=1; DROP TABLE sales; --'");
    }

    @Test
    void rejectsEmptyInput() {
        rejects("   ");
        rejects(null);
        rejects("");
    }

    @Test
    void rejectsPlainTextThatIsNotAQuery() {
        rejects("I am not sure what you mean");
        rejects("EXPLAIN SELECT * FROM sales");
    }
}
