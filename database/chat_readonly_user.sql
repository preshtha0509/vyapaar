-- Module 14: a MySQL account that can only READ.
--
-- The chat assistant runs AI-written SQL with this account. This grant list is the real security
-- boundary of the feature: even if the AI and the ChatSqlGuard both fail, the database itself
-- refuses to change anything, because this user holds no INSERT, UPDATE or DELETE privilege.
--
-- DO NOT COMMIT A REAL PASSWORD. Replace the placeholders, run it as an admin user, and keep the
-- password only in the CHAT_DB_PASSWORD environment variable.
--   <db_name>  : local = vyapaar, Aiven = whatever DB_NAME is set to on Render
--   <host>     : local = 'localhost', Aiven = '%'
--   <password> : a long random password

CREATE USER 'vyapaar_chat'@'<host>' IDENTIFIED BY '<password>';

-- Six business tables.
GRANT SELECT ON <db_name>.customers TO 'vyapaar_chat'@'<host>';
GRANT SELECT ON <db_name>.suppliers TO 'vyapaar_chat'@'<host>';
GRANT SELECT ON <db_name>.products  TO 'vyapaar_chat'@'<host>';
GRANT SELECT ON <db_name>.purchases TO 'vyapaar_chat'@'<host>';
GRANT SELECT ON <db_name>.sales     TO 'vyapaar_chat'@'<host>';
GRANT SELECT ON <db_name>.payments  TO 'vyapaar_chat'@'<host>';

-- Two computed balance views, so the assistant queries a real balance instead of inventing one.
GRANT SELECT ON <db_name>.v_customer_balance TO 'vyapaar_chat'@'<host>';
GRANT SELECT ON <db_name>.v_supplier_balance TO 'vyapaar_chat'@'<host>';

-- Deliberately NOT granted: receipts, receipt_lines, receipt_sends.
-- receipts.public_token is a bearer credential that opens a customer's receipt to anyone holding
-- it, and a text-to-SQL surface must never be able to read a credential. If receipt questions are
-- wanted later, add a view that exposes receipt_number, customer_id, receipt_date and subtotal
-- and leaves the token out, then grant SELECT on that view only.

FLUSH PRIVILEGES;

-- MANDATORY CHECK. Open a new connection as vyapaar_chat and run all three.
-- If the DELETE succeeds, stop and fix the grants before using the feature.
--   SELECT COUNT(*) FROM sales;    -- must work
--   DELETE FROM sales;             -- must fail: DELETE command denied
--   SELECT * FROM mysql.user;      -- must fail: access denied
