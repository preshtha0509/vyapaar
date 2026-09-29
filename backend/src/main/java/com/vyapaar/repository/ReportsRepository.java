package com.vyapaar.repository;

import com.vyapaar.dto.CustomerBalanceRankDTO;
import com.vyapaar.dto.CustomerBalanceTrendDTO;
import com.vyapaar.dto.SalesTrendDTO;
import com.vyapaar.dto.SupplierAgingDTO;
import com.vyapaar.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportsRepository extends JpaRepository<Customer, Long> {

    @Query(value = """
        SELECT 
            transaction_date AS date, 
            transaction_amount AS transactionAmount, 
            running_balance AS runningBalance 
        FROM v_customer_running_balance 
        WHERE customer_id = :customerId 
        ORDER BY transaction_date ASC
        """, nativeQuery = true)
    List<CustomerBalanceTrendDTO> getCustomerBalanceTrend(@Param("customerId") Long customerId);

    @Query(value = """
        WITH monthly_sales AS (
            SELECT 
                DATE_FORMAT(sale_date, '%Y-%m') AS month,
                SUM(total) AS total_sales
            FROM sales
            GROUP BY DATE_FORMAT(sale_date, '%Y-%m')
        ),
        monthly_with_lag AS (
            SELECT 
                month,
                total_sales,
                LAG(total_sales) OVER (ORDER BY month) AS prev_month_sales
            FROM monthly_sales
        )
        SELECT 
            month,
            total_sales AS totalSales,
            CASE 
                WHEN prev_month_sales IS NULL THEN 0.0
                WHEN prev_month_sales = 0 THEN 0.0
                ELSE ROUND(((total_sales - prev_month_sales) / prev_month_sales) * 100, 2)
            END AS percentChange
        FROM monthly_with_lag
        ORDER BY month ASC
        """, nativeQuery = true)
    List<SalesTrendDTO> getSalesTrend();

    @Query(value = """
        WITH customer_balances AS (
            SELECT 
                c.id AS customer_id,
                c.name AS customer_name,
                (
                    c.opening_balance 
                    + COALESCE((SELECT SUM(s.total) FROM sales s WHERE s.customer_id = c.id), 0)
                    - COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.party_type = 'CUSTOMER' AND p.party_id = c.id), 0)
                ) AS balance
            FROM customers c
        ),
        ranked_balances AS (
            SELECT 
                DENSE_RANK() OVER (ORDER BY balance DESC) AS balance_rank,
                customer_name,
                balance
            FROM customer_balances
        )
        SELECT 
            balance_rank AS `rank`,
            customer_name AS customerName,
            balance AS outstandingBalance
        FROM ranked_balances
        WHERE balance_rank <= :limit
        ORDER BY balance_rank ASC, customer_name ASC
        """, nativeQuery = true)
    List<CustomerBalanceRankDTO> getTopCustomersByBalance(@Param("limit") int limit);

    @Query(value = """
        SELECT 
            s.name AS supplierName,
            COALESCE(SUM(CASE WHEN DATEDIFF(CURRENT_DATE, p.purchase_date) BETWEEN 0 AND 30 THEN p.total ELSE 0 END), 0.0) AS days0To30,
            COALESCE(SUM(CASE WHEN DATEDIFF(CURRENT_DATE, p.purchase_date) BETWEEN 31 AND 60 THEN p.total ELSE 0 END), 0.0) AS days31To60,
            COALESCE(SUM(CASE WHEN DATEDIFF(CURRENT_DATE, p.purchase_date) BETWEEN 61 AND 90 THEN p.total ELSE 0 END), 0.0) AS days61To90,
            COALESCE(SUM(CASE WHEN DATEDIFF(CURRENT_DATE, p.purchase_date) > 90 THEN p.total ELSE 0 END), 0.0) AS days90Plus,
            (
                s.opening_balance 
                + COALESCE((SELECT SUM(pu.total) FROM purchases pu WHERE pu.supplier_id = s.id), 0)
                - COALESCE((SELECT SUM(pay.amount) FROM payments pay WHERE pay.party_type = 'SUPPLIER' AND pay.party_id = s.id), 0)
            ) AS totalOutstanding
        FROM suppliers s
        LEFT JOIN purchases p ON p.supplier_id = s.id
        GROUP BY s.id, s.name, s.opening_balance
        ORDER BY totalOutstanding DESC, supplierName ASC
        """, nativeQuery = true)
    List<SupplierAgingDTO> getSupplierPaymentAging();
}
