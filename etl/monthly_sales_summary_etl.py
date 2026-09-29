#!/usr/bin/env python3
"""
VYAPAAR - Standalone Monthly Sales Summary ETL Script
Extracts sales data from the `sales` table, transforms monthly metrics (revenue,
quantity, avg rate, MoM growth %), and loads the summary into `monthly_sales_summary`.
"""

import argparse
import logging
import os
import sys
from decimal import Decimal
import mysql.connector
from mysql.connector import Error

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)]
)
logger = logging.getLogger("sales_summary_etl")

def get_db_connection():
    """Establishes and returns a connection to the MySQL database."""
    host = os.getenv("DB_HOST", "localhost")
    port = int(os.getenv("DB_PORT", "3306"))
    user = os.getenv("DB_USER", "root")
    password = os.getenv("DB_PASSWORD")
    database = os.getenv("DB_NAME", "vyapaar")

    if not password:
        raise RuntimeError("DB_PASSWORD environment variable is required")

    logger.info(f"Connecting to MySQL database '{database}' at {host}:{port} as '{user}'")
    return mysql.connector.connect(
        host=host,
        port=port,
        user=user,
        password=password,
        database=database
    )

def ensure_target_table(cursor):
    """Ensures the `monthly_sales_summary` table exists in the database."""
    create_table_sql = """
    CREATE TABLE IF NOT EXISTS monthly_sales_summary (
        id INT AUTO_INCREMENT PRIMARY KEY,
        month VARCHAR(7) NOT NULL UNIQUE,
        total_revenue DECIMAL(15, 2) NOT NULL,
        total_quantity DECIMAL(15, 2) NOT NULL,
        avg_rate DECIMAL(15, 2) NOT NULL,
        mom_growth_pct DECIMAL(10, 2) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );
    """
    logger.info("Ensuring target table `monthly_sales_summary` exists...")
    cursor.execute(create_table_sql)

def extract_and_transform(cursor, start_date=None, end_date=None):
    """
    Extracts sales data aggregated by month and calculates MoM growth %.
    Returns a list of tuples containing (month, total_revenue, total_quantity, avg_rate, mom_growth_pct).
    """
    date_filter = ""
    params = []

    if start_date and end_date:
        date_filter = "WHERE sale_date BETWEEN %s AND %s"
        params = [start_date, end_date]
        logger.info(f"Extracting sales data filtered between {start_date} and {end_date}")
    else:
        logger.info("Extracting all available monthly sales data")

    query = f"""
    WITH monthly_sales AS (
        SELECT
            DATE_FORMAT(sale_date, '%Y-%m') AS month,
            SUM(total) AS total_revenue,
            SUM(quantity) AS total_quantity,
            CASE
                WHEN SUM(quantity) > 0 THEN ROUND(SUM(total) / SUM(quantity), 2)
                ELSE 0.0
            END AS avg_rate
        FROM sales
        {date_filter}
        GROUP BY DATE_FORMAT(sale_date, '%Y-%m')
    ),
    monthly_with_lag AS (
        SELECT
            month,
            total_revenue,
            total_quantity,
            avg_rate,
            LAG(total_revenue) OVER (ORDER BY month) AS prev_revenue
        FROM monthly_sales
    )
    SELECT
        month,
        total_revenue,
        total_quantity,
        avg_rate,
        CASE
            WHEN prev_revenue IS NULL THEN 0.0
            WHEN prev_revenue = 0 THEN 0.0
            ELSE ROUND(((total_revenue - prev_revenue) / prev_revenue) * 100, 2)
        END AS mom_growth_pct
    FROM monthly_with_lag
    ORDER BY month ASC;
    """

    cursor.execute(query, params)
    rows = cursor.fetchall()
    logger.info(f"Extracted and transformed {len(rows)} month summary row(s)")
    return rows

def load_summary_data(cursor, rows):
    """
    Loads transformed summary rows into `monthly_sales_summary` using UPSERT logic.
    """
    upsert_sql = """
    INSERT INTO monthly_sales_summary (month, total_revenue, total_quantity, avg_rate, mom_growth_pct)
    VALUES (%s, %s, %s, %s, %s)
    ON DUPLICATE KEY UPDATE
        total_revenue = VALUES(total_revenue),
        total_quantity = VALUES(total_quantity),
        avg_rate = VALUES(avg_rate),
        mom_growth_pct = VALUES(mom_growth_pct),
        created_at = CURRENT_TIMESTAMP;
    """

    for row in rows:
        month, total_revenue, total_quantity, avg_rate, mom_growth_pct = row
        logger.info(f"Loading month {month}: Revenue={total_revenue}, Quantity={total_quantity}, Avg Rate={avg_rate}, MoM Growth={mom_growth_pct}%")
        cursor.execute(upsert_sql, (month, total_revenue, total_quantity, avg_rate, mom_growth_pct))

def run_etl(start_date=None, end_date=None):
    """Main ETL workflow execution."""
    conn = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        ensure_target_table(cursor)
        rows = extract_and_transform(cursor, start_date, end_date)

        if rows:
            load_summary_data(cursor, rows)
            conn.commit()
            logger.info("ETL pipeline completed successfully!")
        else:
            logger.warning("No sales records found matching the extraction criteria.")

    except Error as e:
        logger.error(f"Database error during ETL pipeline execution: {e}")
        if conn:
            conn.rollback()
        sys.exit(1)
    finally:
        if conn and conn.is_connected():
            cursor.close()
            conn.close()
            logger.info("MySQL connection closed.")

def main():
    parser = argparse.ArgumentParser(description="VYAPAAR Monthly Sales Summary ETL Pipeline")
    parser.add_argument("--start-date", type=str, help="Start date (YYYY-MM-DD)", default=None)
    parser.add_argument("--end-date", type=str, help="End date (YYYY-MM-DD)", default=None)

    args = parser.parse_args()
    run_etl(args.start_date, args.end_date)

if __name__ == "__main__":
    main()
