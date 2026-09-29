# VYAPAAR — Complete Build Guide (Version 1)

Follow this top to bottom, one step at a time. Don't skip ahead — each module builds on the last. After every code step: save the file, restart the Spring Boot app, test the endpoint in Postman, THEN move to the next step. Don't move on if something doesn't compile.

Rules baked into this guide (per your project rules — not changing these):
- No mutable balance fields anywhere. Balances are always calculated.
- Keep fields minimal exactly as specified.
- One module fully done (entity → repo → service → controller → tested in Postman) before the next module starts.
- Package structure stays: `entity`, `repository`, `service`, `controller`, `exception`, `config`, `util`.

---

## ✅ STATUS: Already Done
- Project scaffolded, MySQL connected, `.gitignore`/env var fix applied for DB password
- **Customer module** fully built: entity, repository, service, controller (`GET /api/customers`, `POST /api/customers`)

---

## MODULE 2: Supplier

Identical shape to Customer. In IntelliJ, right-click `entity` → New → Java Class → `Supplier`.

**`entity/Supplier.java`**
```java
package com.vyapaar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Double openingBalance;

    private String gstNumber;

    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Supplier() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Double getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(Double openingBalance) { this.openingBalance = openingBalance; }
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

**`repository/SupplierRepository.java`**
```java
package com.vyapaar.repository;

import com.vyapaar.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
```

**`service/SupplierService.java`**
```java
package com.vyapaar.service;

import com.vyapaar.entity.Supplier;
import com.vyapaar.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }
}
```

**`controller/SupplierController.java`**
```java
package com.vyapaar.controller;

import com.vyapaar.entity.Supplier;
import com.vyapaar.service.SupplierService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<Supplier> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @PostMapping
    public Supplier addSupplier(@RequestBody Supplier supplier) {
        return supplierService.saveSupplier(supplier);
    }
}
```

**Test in Postman:** `POST http://localhost:8080/api/suppliers` with body:
```json
{ "name": "Ramesh Traders", "phone": "9876543210", "city": "Nagpur", "openingBalance": 0 }
```
Then `GET http://localhost:8080/api/suppliers` — you should see it back.

---

## MODULE 3: Product

**`entity/Product.java`**
```java
package com.vyapaar.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    public enum Unit { KG, QUINTAL, BAG }

    public Product() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }
}
```

**`repository/ProductRepository.java`**
```java
package com.vyapaar.repository;

import com.vyapaar.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

**`service/ProductService.java`**
```java
package com.vyapaar.service;

import com.vyapaar.entity.Product;
import com.vyapaar.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
```

**`controller/ProductController.java`**
```java
package com.vyapaar.controller;

import com.vyapaar.entity.Product;
import com.vyapaar.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }
}
```

**Test:** `POST http://localhost:8080/api/products`
```json
{ "name": "Garlic Grade A", "unit": "QUINTAL" }
```

---

## MODULE 4: Purchase (Supplier → Stock)

**`entity/Purchase.java`**
```java
package com.vyapaar.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private Double rate;

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    public enum PaymentStatus { PAID, PENDING }

    private String notes;

    public Purchase() {}

    public Long getId() { return id; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public Double getRate() { return rate; }
    public void setRate(Double rate) { this.rate = rate; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
```

**`repository/PurchaseRepository.java`**
```java
package com.vyapaar.repository;

import com.vyapaar.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findBySupplierId(Long supplierId);
}
```

**`service/PurchaseService.java`**
```java
package com.vyapaar.service;

import com.vyapaar.entity.Purchase;
import com.vyapaar.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;

    public PurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    public Purchase savePurchase(Purchase purchase) {
        // total is always calculated on the backend, never trusted from the frontend
        purchase.setTotal(purchase.getQuantity() * purchase.getRate());
        return purchaseRepository.save(purchase);
    }
}
```

**`controller/PurchaseController.java`**
```java
package com.vyapaar.controller;

import com.vyapaar.entity.Purchase;
import com.vyapaar.service.PurchaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public List<Purchase> getAllPurchases() {
        return purchaseService.getAllPurchases();
    }

    @PostMapping
    public Purchase addPurchase(@RequestBody Purchase purchase) {
        return purchaseService.savePurchase(purchase);
    }
}
```

**Test:** `POST http://localhost:8080/api/purchases`
```json
{
  "supplier": { "id": 1 },
  "product": { "id": 1 },
  "quantity": 10,
  "rate": 8000,
  "purchaseDate": "2026-07-22",
  "paymentStatus": "PENDING",
  "notes": "First lot"
}
```
(You don't need to send `total` — it's calculated for you.)

---

## MODULE 5: Sale (Customer → Cash/Udhari)

Same shape as Purchase, but linked to Customer with CASH/UDHARI status.

**`entity/Sale.java`**
```java
package com.vyapaar.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private Double rate;

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false)
    private LocalDate saleDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    public enum PaymentStatus { CASH, UDHARI }

    private String notes;

    public Sale() {}

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public Double getRate() { return rate; }
    public void setRate(Double rate) { this.rate = rate; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
```

**`repository/SaleRepository.java`**
```java
package com.vyapaar.repository;

import com.vyapaar.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByCustomerId(Long customerId);
}
```

**`service/SaleService.java`**
```java
package com.vyapaar.service;

import com.vyapaar.entity.Sale;
import com.vyapaar.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public Sale saveSale(Sale sale) {
        sale.setTotal(sale.getQuantity() * sale.getRate());
        return saleRepository.save(sale);
    }
}
```

**`controller/SaleController.java`**
```java
package com.vyapaar.controller;

import com.vyapaar.entity.Sale;
import com.vyapaar.service.SaleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public List<Sale> getAllSales() {
        return saleService.getAllSales();
    }

    @PostMapping
    public Sale addSale(@RequestBody Sale sale) {
        return saleService.saveSale(sale);
    }
}
```

**Test:** `POST http://localhost:8080/api/sales`
```json
{
  "customer": { "id": 1 },
  "product": { "id": 1 },
  "quantity": 5,
  "rate": 9000,
  "saleDate": "2026-07-22",
  "paymentStatus": "UDHARI",
  "notes": "Regular customer"
}
```

---

## MODULE 6: Payment (the udhari settling module)

A payment always belongs to either a customer or a supplier — never both. `partyType` tells you which table `partyId` points to.

**`entity/Payment.java`**
```java
package com.vyapaar.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyType partyType;

    public enum PartyType { CUSTOMER, SUPPLIER }

    @Column(nullable = false)
    private Long partyId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mode mode;

    public enum Mode { CASH, UPI, BANK_TRANSFER, CHEQUE }

    private String notes;

    public Payment() {}

    public Long getId() { return id; }
    public PartyType getPartyType() { return partyType; }
    public void setPartyType(PartyType partyType) { this.partyType = partyType; }
    public Long getPartyId() { return partyId; }
    public void setPartyId(Long partyId) { this.partyId = partyId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
```

**`repository/PaymentRepository.java`**
```java
package com.vyapaar.repository;

import com.vyapaar.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPartyTypeAndPartyId(Payment.PartyType partyType, Long partyId);
}
```

**`service/PaymentService.java`**
```java
package com.vyapaar.service;

import com.vyapaar.entity.Payment;
import com.vyapaar.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }
}
```

**`controller/PaymentController.java`**
```java
package com.vyapaar.controller;

import com.vyapaar.entity.Payment;
import com.vyapaar.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @PostMapping
    public Payment addPayment(@RequestBody Payment payment) {
        return paymentService.savePayment(payment);
    }
}
```

**Test:** `POST http://localhost:8080/api/payments`
```json
{
  "partyType": "CUSTOMER",
  "partyId": 1,
  "amount": 20000,
  "paymentDate": "2026-07-22",
  "mode": "CASH",
  "notes": "Partial payment"
}
```

---

## MODULE 7: Ledger (the heart of the app — read this twice)

**Rule:** No stored balance column, anywhere. This service is the *only* place balance is ever computed, and it always computes it fresh from raw data. This is your strongest interview talking point — protect it.

**`service/LedgerService.java`**
```java
package com.vyapaar.service;

import com.vyapaar.entity.Payment;
import com.vyapaar.repository.CustomerRepository;
import com.vyapaar.repository.PaymentRepository;
import com.vyapaar.repository.PurchaseRepository;
import com.vyapaar.repository.SaleRepository;
import com.vyapaar.repository.SupplierRepository;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {

    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;

    public LedgerService(SaleRepository saleRepository,
                          PurchaseRepository purchaseRepository,
                          PaymentRepository paymentRepository,
                          CustomerRepository customerRepository,
                          SupplierRepository supplierRepository) {
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
    }

    // How much this customer owes the business right now
    public Double getCustomerBalance(Long customerId) {
        double openingBalance = customerRepository.findById(customerId)
                .map(c -> c.getOpeningBalance())
                .orElse(0.0);

        double totalSales = saleRepository.findByCustomerId(customerId).stream()
                .mapToDouble(s -> s.getTotal())
                .sum();

        double totalReceived = paymentRepository
                .findByPartyTypeAndPartyId(Payment.PartyType.CUSTOMER, customerId).stream()
                .mapToDouble(p -> p.getAmount())
                .sum();

        return openingBalance + totalSales - totalReceived;
    }

    // How much the business owes this supplier right now
    public Double getSupplierBalance(Long supplierId) {
        double openingBalance = supplierRepository.findById(supplierId)
                .map(s -> s.getOpeningBalance())
                .orElse(0.0);

        double totalPurchases = purchaseRepository.findBySupplierId(supplierId).stream()
                .mapToDouble(p -> p.getTotal())
                .sum();

        double totalPaid = paymentRepository
                .findByPartyTypeAndPartyId(Payment.PartyType.SUPPLIER, supplierId).stream()
                .mapToDouble(p -> p.getAmount())
                .sum();

        return openingBalance + totalPurchases - totalPaid;
    }
}
```

**`controller/LedgerController.java`**
```java
package com.vyapaar.controller;

import com.vyapaar.service.LedgerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/customer/{id}/balance")
    public Double getCustomerBalance(@PathVariable Long id) {
        return ledgerService.getCustomerBalance(id);
    }

    @GetMapping("/supplier/{id}/balance")
    public Double getSupplierBalance(@PathVariable Long id) {
        return ledgerService.getSupplierBalance(id);
    }
}
```

**Test:** After adding a sale of ₹45000 (udhari) and a payment of ₹20000 to customer id 1:
`GET http://localhost:8080/api/ledger/customer/1/balance` → should return `25000.0`

*(A full chronological "ledger page" with running totals per transaction is a nice-to-have for later — not required for V1 to be usable. Skip it for now; the balance number above is what your father actually needs day to day.)*

---

## MODULE 8: Dashboard

**`controller/DashboardController.java`**
```java
package com.vyapaar.controller;

import com.vyapaar.entity.Customer;
import com.vyapaar.entity.Supplier;
import com.vyapaar.repository.CustomerRepository;
import com.vyapaar.repository.SupplierRepository;
import com.vyapaar.service.LedgerService;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final LedgerService ledgerService;

    public DashboardController(CustomerRepository customerRepository,
                                SupplierRepository supplierRepository,
                                LedgerService ledgerService) {
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.ledgerService = ledgerService;
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        List<Customer> customers = customerRepository.findAll();
        List<Supplier> suppliers = supplierRepository.findAll();

        Map<String, Double> customerBalances = new HashMap<>();
        double totalToReceive = 0;
        for (Customer c : customers) {
            double bal = ledgerService.getCustomerBalance(c.getId());
            customerBalances.put(c.getName(), bal);
            totalToReceive += bal;
        }

        Map<String, Double> supplierBalances = new HashMap<>();
        double totalToPay = 0;
        for (Supplier s : suppliers) {
            double bal = ledgerService.getSupplierBalance(s.getId());
            supplierBalances.put(s.getName(), bal);
            totalToPay += bal;
        }

        List<Map.Entry<String, Double>> topCustomers = customerBalances.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        List<Map.Entry<String, Double>> topSuppliers = supplierBalances.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalToReceive", totalToReceive);
        summary.put("totalToPay", totalToPay);
        summary.put("topCustomers", topCustomers);
        summary.put("topSuppliers", topSuppliers);
        return summary;
    }
}
```

**Test:** `GET http://localhost:8080/api/dashboard/summary`

*(Note: this loops in Java rather than doing pure SQL aggregation — that's intentional for V1 simplicity, since your data volume is small. If it ever gets slow with hundreds of customers, that's the one place to optimize later — not now.)*

---

## Backend is now functionally complete for V1. Before touching frontend:

1. Enable CORS so React can call the backend. Create `config/CorsConfig.java`:
```java
package com.vyapaar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
```

2. Commit your work: `git add . && git commit -m "Backend V1 complete: all modules + ledger + dashboard"`

---

## MODULE 9: Frontend Setup

In terminal, inside your project root (not inside `backend`):
```
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install
npm install -D tailwindcss @tailwindcss/vite
npm install react-router-dom axios react-i18next i18next
```

Configure Tailwind in `vite.config.ts` (add the Tailwind plugin) and add `@import "tailwindcss";` to `src/index.css`. Run `npm run dev` and confirm you see the default Vite page at `localhost:5173` before continuing.

---

## MODULE 10: Frontend Pages (build in this order)

1. **Router + layout shell** — nav bar with links to Dashboard, Customers, Suppliers, Products, Sales, Purchases, Payments, Settings. Add language toggle button in the nav.
2. **Customers page** — table listing all customers (name, phone, city, live balance via `/api/ledger/customer/{id}/balance`), "Add Customer" form.
3. **Suppliers page** — same pattern, using supplier endpoints.
4. **Products page** — simple table + add form.
5. **New Sale page** — dropdown of customers, dropdown of products, quantity, rate (auto-shows total), date, payment status, notes.
6. **New Purchase page** — same pattern with suppliers.
7. **Payments page** — party type toggle, party dropdown (loads customers or suppliers depending on toggle), amount, date, mode, notes.
8. **Dashboard page** — cards for total to receive / total to pay, list of top customers/suppliers, from `/api/dashboard/summary`.
9. **i18n** — `en.json` and `hi.json` dictionaries, wrap all UI text in `t('key')`.

Build and test each page fully before starting the next — same rule as the backend.

---

## MODULE 11: Reports & Insights

**One scope cut, made deliberately:** the spec mentioned ranking "best-paying vs slow-paying customers." That needs a due-date concept your data doesn't actually track (a sale doesn't have a payment deadline), so building it now would mean inventing a metric that doesn't map to anything real. Skipping it for V1 — nobody uses a stat that's guessing. What you get instead: a date-range summary, a monthly sales-vs-purchases trend chart, and purchase volume by supplier. All three map directly to fields you already have.

### Backend: add two lookup methods to existing repositories

In **`repository/SaleRepository.java`**, add inside the interface:
```java
List<Sale> findBySaleDateBetween(LocalDate from, LocalDate to);
```
(add `import java.time.LocalDate;` and `import java.util.List;` at the top if not already there)

In **`repository/PurchaseRepository.java`**, add inside the interface:
```java
List<Purchase> findByPurchaseDateBetween(LocalDate from, LocalDate to);
```
(same imports)

### `service/ReportsService.java`
```java
package com.vyapaar.service;

import com.vyapaar.entity.Purchase;
import com.vyapaar.entity.Sale;
import com.vyapaar.repository.PurchaseRepository;
import com.vyapaar.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportsService {

    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;

    public ReportsService(SaleRepository saleRepository, PurchaseRepository purchaseRepository) {
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
    }

    // Date-range summary: total sales, total purchases, and the gross
    // difference between them. This is NOT true profit margin — that would
    // need per-unit cost tracking (FIFO/average cost) which V1 doesn't do.
    // Labelled clearly on the frontend so it's never mistaken for real margin.
    public Map<String, Object> getSummary(LocalDate from, LocalDate to) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(from, to);
        List<Purchase> purchases = purchaseRepository.findByPurchaseDateBetween(from, to);

        double totalSales = sales.stream().mapToDouble(Sale::getTotal).sum();
        double totalPurchases = purchases.stream().mapToDouble(Purchase::getTotal).sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSales", totalSales);
        summary.put("totalPurchases", totalPurchases);
        summary.put("grossDifference", totalSales - totalPurchases);
        return summary;
    }

    // Monthly trend for the last N months, for the chart. Fills in zero for
    // any month with no activity so the chart axis doesn't skip months.
    public List<Map<String, Object>> getMonthlyTrend(int monthsBack) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.minusMonths(monthsBack - 1).atDay(1);

        List<Sale> sales = saleRepository.findBySaleDateBetween(start, LocalDate.now());
        List<Purchase> purchases = purchaseRepository.findByPurchaseDateBetween(start, LocalDate.now());

        Map<YearMonth, Double> salesByMonth = sales.stream().collect(Collectors.groupingBy(
                s -> YearMonth.from(s.getSaleDate()),
                Collectors.summingDouble(Sale::getTotal)));

        Map<YearMonth, Double> purchasesByMonth = purchases.stream().collect(Collectors.groupingBy(
                p -> YearMonth.from(p.getPurchaseDate()),
                Collectors.summingDouble(Purchase::getTotal)));

        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = monthsBack - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", month.format(labelFormat));
            row.put("sales", salesByMonth.getOrDefault(month, 0.0));
            row.put("purchases", purchasesByMonth.getOrDefault(month, 0.0));
            trend.add(row);
        }
        return trend;
    }

    // Purchase volume grouped by supplier, for the range given.
    public List<Map<String, Object>> getSupplierVolume(LocalDate from, LocalDate to) {
        List<Purchase> purchases = purchaseRepository.findByPurchaseDateBetween(from, to);

        Map<String, Double> bySupplier = purchases.stream().collect(Collectors.groupingBy(
                p -> p.getSupplier().getName(),
                Collectors.summingDouble(Purchase::getTotal)));

        return bySupplier.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("supplier", e.getKey());
                    row.put("total", e.getValue());
                    return row;
                })
                .collect(Collectors.toList());
    }
}
```

### `controller/ReportsController.java`
```java
package com.vyapaar.controller;

import com.vyapaar.service.ReportsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportsService.getSummary(from, to);
    }

    @GetMapping("/monthly-trend")
    public List<Map<String, Object>> getMonthlyTrend(
            @RequestParam(defaultValue = "6") int months) {
        return reportsService.getMonthlyTrend(months);
    }

    @GetMapping("/supplier-volume")
    public List<Map<String, Object>> getSupplierVolume(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportsService.getSupplierVolume(from, to);
    }
}
```

**Test in Postman:**
- `GET http://localhost:8080/api/reports/summary?from=2026-07-01&to=2026-07-31`
- `GET http://localhost:8080/api/reports/monthly-trend?months=6`
- `GET http://localhost:8080/api/reports/supplier-volume?from=2026-01-01&to=2026-07-31`

### Frontend: install the chart library
```
npm install recharts
```

### `components/MonthlyTrendChart.tsx`
```tsx
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";

type TrendPoint = { month: string; sales: number; purchases: number };

export function MonthlyTrendChart({ data }: { data: TrendPoint[] }) {
  return (
    <div className="bg-paper-raised border border-rule rounded-[10px] px-6 py-5">
      <h2 className="font-display font-medium text-base m-0 mb-4">Monthly sales vs purchases</h2>
      <ResponsiveContainer width="100%" height={260}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" stroke="#DFD3B8" />
          <XAxis dataKey="month" tick={{ fontSize: 12 }} />
          <YAxis tick={{ fontSize: 12 }} />
          <Tooltip formatter={(value: number) => "₹" + value.toLocaleString("en-IN")} />
          <Line type="monotone" dataKey="sales" stroke="#3D6B4F" strokeWidth={2} dot={false} />
          <Line type="monotone" dataKey="purchases" stroke="#A8481F" strokeWidth={2} dot={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
```

### `pages/Reports.tsx`
```tsx
import { useState } from "react";
import { MonthlyTrendChart } from "../components/MonthlyTrendChart";
import { formatRupees } from "../utils/formatRupees";

// Demo shape — replace with real fetch calls to /api/reports/* once the
// backend module above is built and tested.
const DEMO_TREND = [
  { month: "Feb 2026", sales: 210000, purchases: 160000 },
  { month: "Mar 2026", sales: 245000, purchases: 178000 },
  { month: "Apr 2026", sales: 198000, purchases: 152000 },
  { month: "May 2026", sales: 260000, purchases: 190000 },
  { month: "Jun 2026", sales: 289000, purchases: 205000 },
  { month: "Jul 2026", sales: 231000, purchases: 172000 },
];

const DEMO_SUPPLIER_VOLUME = [
  { supplier: "Ramesh Traders", total: 128000 },
  { supplier: "Patil Garlic Depot", total: 96500 },
  { supplier: "Shivam Agro", total: 61000 },
];

export function Reports() {
  const [from, setFrom] = useState("2026-07-01");
  const [to, setTo] = useState("2026-07-31");

  const totalSales = DEMO_TREND.at(-1)?.sales ?? 0;
  const totalPurchases = DEMO_TREND.at(-1)?.purchases ?? 0;

  return (
    <div className="px-6 md:px-10 py-8 pb-14">
      <div className="flex items-end gap-3 mb-6">
        <div>
          <label className="text-xs text-ink-soft block mb-1">From</label>
          <input
            type="date"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            className="border border-rule rounded-lg px-3 py-1.5 text-sm bg-paper-raised"
          />
        </div>
        <div>
          <label className="text-xs text-ink-soft block mb-1">To</label>
          <input
            type="date"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            className="border border-rule rounded-lg px-3 py-1.5 text-sm bg-paper-raised"
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-3.5 mb-6">
        <div className="bg-paper-raised border border-rule rounded-[10px] px-5 py-4">
          <p className="text-xs text-ink-soft m-0 mb-1.5">Total sales</p>
          <p className="font-mono text-xl font-medium m-0 text-credit">{formatRupees(totalSales)}</p>
        </div>
        <div className="bg-paper-raised border border-rule rounded-[10px] px-5 py-4">
          <p className="text-xs text-ink-soft m-0 mb-1.5">Total purchases</p>
          <p className="font-mono text-xl font-medium m-0 text-debit">{formatRupees(totalPurchases)}</p>
        </div>
        <div className="bg-paper-raised border border-rule rounded-[10px] px-5 py-4">
          <p className="text-xs text-ink-soft m-0 mb-1.5">Gross difference</p>
          <p className="font-mono text-xl font-medium m-0">{formatRupees(totalSales - totalPurchases)}</p>
        </div>
      </div>

      <div className="mb-6">
        <MonthlyTrendChart data={DEMO_TREND} />
      </div>

      <div className="bg-paper-raised border border-rule rounded-[10px] px-6 py-5">
        <h2 className="font-display font-medium text-base m-0 mb-4">Purchase volume by supplier</h2>
        <table className="w-full border-collapse text-sm">
          <tbody>
            {DEMO_SUPPLIER_VOLUME.map((row) => (
              <tr key={row.supplier}>
                <td className="py-2.5 border-b border-rule last:border-b-0">{row.supplier}</td>
                <td className="py-2.5 border-b border-rule last:border-b-0 text-right font-mono">
                  {formatRupees(row.total)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
```

**Note on "Gross difference":** this is sales minus purchases in the selected range, not profit — purchases in a range don't necessarily correspond to the garlic sold in that same range (stock carries over). Label it exactly as "Gross difference" in the UI, never "Profit," so it's never misread as something it isn't.

---

## MODULE 12: Bill Generation & SMS Sharing

**Decision made:** text-only SMS, no attachment. Sending SMS programmatically always needs a paid gateway (Twilio, MSG91, etc.) — even for plain text, since carriers don't allow free bulk sending from a server. The zero-cost V1 approach instead: an `sms:` link that opens your father's own phone's SMS app, pre-filled with the bill text and the customer's number. He taps send himself. **This only works from a phone browser** — clicking it on a desktop won't open anything, since desktops don't have an SMS app. If a fully automated "app sends it, no tap needed" experience matters later, that's a V2 feature requiring a paid gateway account — don't build that now.

**Bilingual, matching the rest of the app:** the bill is generated in whichever language the UI is currently set to — same English/Hindi toggle, not a separate setting. Customer names and product names are never translated (same rule as the rest of the app) — only the bill's own labels (Date, Customer, Total, Status, etc.) and the unit/status words change.

A bill is generated from an existing Sale record — one bill per sale, no separate "invoice" concept needed.

### `dto/BillResponse.java`
```java
package com.vyapaar.dto;

public class BillResponse {
    private String customerName;
    private String customerPhone;
    private String productName;
    private Double quantity;
    private String unit;
    private Double rate;
    private Double total;
    private String saleDate;
    private String paymentStatus;
    private String billText;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getRate() { return rate; }
    public void setRate(Double rate) { this.rate = rate; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getSaleDate() { return saleDate; }
    public void setSaleDate(String saleDate) { this.saleDate = saleDate; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getBillText() { return billText; }
    public void setBillText(String billText) { this.billText = billText; }
}
```

### `service/BillService.java`
```java
package com.vyapaar.service;

import com.vyapaar.dto.BillResponse;
import com.vyapaar.entity.Sale;
import com.vyapaar.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class BillService {

    private final SaleRepository saleRepository;

    public BillService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    public BillResponse generateBill(Long saleId, String lang) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found: " + saleId));

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy");
        boolean hindi = "hi".equalsIgnoreCase(lang);

        BillResponse bill = new BillResponse();
        bill.setCustomerName(sale.getCustomer().getName());
        bill.setCustomerPhone(sale.getCustomer().getPhone());
        bill.setProductName(sale.getProduct().getName());
        bill.setQuantity(sale.getQuantity());
        bill.setUnit(sale.getProduct().getUnit().name());
        bill.setRate(sale.getRate());
        bill.setTotal(sale.getTotal());
        bill.setSaleDate(sale.getSaleDate().format(dateFormat));
        bill.setPaymentStatus(sale.getPaymentStatus().name());
        bill.setBillText(hindi ? buildBillTextHindi(sale, dateFormat) : buildBillTextEnglish(sale, dateFormat));
        return bill;
    }

    private String buildBillTextEnglish(Sale sale, DateTimeFormatter dateFormat) {
        return "VYAPAAR - Bill\n" +
                "Date: " + sale.getSaleDate().format(dateFormat) + "\n" +
                "Customer: " + sale.getCustomer().getName() + "\n" +
                sale.getProduct().getName() + " - " + sale.getQuantity() + " " + unitLabel(sale.getProduct().getUnit().name(), false) +
                " @ \u20B9" + sale.getRate() + "\n" +
                "Total: \u20B9" + sale.getTotal() + "\n" +
                "Status: " + statusLabel(sale.getPaymentStatus().name(), false) + "\n" +
                "Thank you for your business.";
    }

    private String buildBillTextHindi(Sale sale, DateTimeFormatter dateFormat) {
        return "व्यापार - बिल\n" +
                "दिनांक: " + sale.getSaleDate().format(dateFormat) + "\n" +
                "ग्राहक: " + sale.getCustomer().getName() + "\n" +
                sale.getProduct().getName() + " - " + sale.getQuantity() + " " + unitLabel(sale.getProduct().getUnit().name(), true) +
                " @ \u20B9" + sale.getRate() + "\n" +
                "कुल: \u20B9" + sale.getTotal() + "\n" +
                "स्थिति: " + statusLabel(sale.getPaymentStatus().name(), true) + "\n" +
                "आपके व्यवसाय के लिए धन्यवाद।";
    }

    // Product/customer names are never translated (same rule as the rest of the
    // app) — only these fixed unit/status words change with language.
    private String unitLabel(String unit, boolean hindi) {
        if (!hindi) return unit;
        switch (unit) {
            case "KG": return "किलो";
            case "QUINTAL": return "क्विंटल";
            case "BAG": return "बोरी";
            default: return unit;
        }
    }

    private String statusLabel(String status, boolean hindi) {
        if (!hindi) return status;
        switch (status) {
            case "CASH": return "नकद";
            case "UDHARI": return "उधारी";
            default: return status;
        }
    }
}
```

### `controller/BillController.java`
```java
package com.vyapaar.controller;

import com.vyapaar.dto.BillResponse;
import com.vyapaar.service.BillService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/{id}/bill")
    public BillResponse getBill(@PathVariable Long id, @RequestParam(defaultValue = "en") String lang) {
        return billService.generateBill(id, lang);
    }
}
```

**Test:**
- `GET http://localhost:8080/api/sales/1/bill?lang=en`
- `GET http://localhost:8080/api/sales/1/bill?lang=hi` — confirm `billText` comes back in Hindi, with the customer's actual name unchanged.

### `components/BillView.tsx`
```tsx
import { useTranslation } from "react-i18next";

type Bill = {
  customerPhone: string;
  billText: string;
};

export function BillView({ bill }: { bill: Bill }) {
  const { t } = useTranslation();
  const smsLink = `sms:${bill.customerPhone}?body=${encodeURIComponent(bill.billText)}`;

  return (
    <div className="bg-paper-raised border border-rule rounded-[10px] px-6 py-5 max-w-md">
      <h2 className="font-display font-medium text-base m-0 mb-3">{t("bill.title", "Bill")}</h2>
      <pre className="font-mono text-sm whitespace-pre-wrap m-0 mb-4">{bill.billText}</pre>
      <div className="flex gap-2">
        <button
          onClick={() => window.print()}
          className="text-sm font-medium px-4 py-2 rounded-lg bg-paper-raised border border-rule"
        >
          {t("bill.print", "Print")}
        </button>
        <a
          href={smsLink}
          className="text-sm font-medium px-4 py-2 rounded-lg bg-cloth text-paper no-underline"
        >
          {t("bill.sendSms", "Send via SMS")}
        </a>
      </div>
    </div>
  );
}
```

When fetching the bill data for this component, pass the current app language along:
```ts
const { i18n } = useTranslation();
const response = await axios.get(`/api/sales/${saleId}/bill?lang=${i18n.language}`);
```
This is the one place the language toggle actually changes backend-generated content, not just frontend labels — worth testing both ways (switch the toggle, regenerate the same bill, confirm the text itself changes).

Add a "Bill" button next to each row in the Sales list/table that opens this view for that sale's data.

---

## MODULE 13: Garlic Freshness Display

**Decision made:** display-only. The app shows how old the current stock of each product is; it does NOT auto-adjust price. Your father sets the rate himself when recording a sale, same as always — the age is just information to help him decide.

This tracks age at the **product level** (when was this product type last restocked), not per individual purchase batch — full batch/lot tracking (FIFO inventory) would be real added complexity for a business that doesn't need it yet. If per-batch tracking is ever needed later, that's a genuine V2 redesign, not a small addition.

### Update `entity/Product.java` — add one field
```java
private java.time.LocalDate lastRestockedDate;

public java.time.LocalDate getLastRestockedDate() { return lastRestockedDate; }
public void setLastRestockedDate(java.time.LocalDate lastRestockedDate) { this.lastRestockedDate = lastRestockedDate; }
```

### Update `service/PurchaseService.java` — set it whenever a purchase is recorded
```java
package com.vyapaar.service;

import com.vyapaar.entity.Product;
import com.vyapaar.entity.Purchase;
import com.vyapaar.repository.ProductRepository;
import com.vyapaar.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;

    public PurchaseService(PurchaseRepository purchaseRepository, ProductRepository productRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
    }

    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    public Purchase savePurchase(Purchase purchase) {
        purchase.setTotal(purchase.getQuantity() * purchase.getRate());
        Purchase saved = purchaseRepository.save(purchase);

        // Every new purchase resets how "fresh" this product looks in the UI
        Product product = saved.getProduct();
        product.setLastRestockedDate(saved.getPurchaseDate());
        productRepository.save(product);

        return saved;
    }
}
```
(This replaces the version from Module 4 — same file, one added block.)

### `dto/ProductResponse.java`
```java
package com.vyapaar.dto;

public class ProductResponse {
    private Long id;
    private String name;
    private String unit;
    private Long ageInDays; // null if never restocked yet

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Long getAgeInDays() { return ageInDays; }
    public void setAgeInDays(Long ageInDays) { this.ageInDays = ageInDays; }
}
```

### Update `service/ProductService.java` — compute age fresh on every read, never store it
```java
package com.vyapaar.service;

import com.vyapaar.dto.ProductResponse;
import com.vyapaar.entity.Product;
import com.vyapaar.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(p -> {
            ProductResponse r = new ProductResponse();
            r.setId(p.getId());
            r.setName(p.getName());
            r.setUnit(p.getUnit().name());
            if (p.getLastRestockedDate() != null) {
                r.setAgeInDays(ChronoUnit.DAYS.between(p.getLastRestockedDate(), LocalDate.now()));
            }
            return r;
        }).collect(Collectors.toList());
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
```
(`getAllProducts()` now returns `List<ProductResponse>` instead of `List<Product>` — update `ProductController`'s return type to match. This is a deliberate change to the Module 3 contract, made here on purpose, not an accidental architecture drift.)

### `components/FreshnessBadge.tsx`
```tsx
type FreshnessBadgeProps = { ageInDays: number | null };

// Placeholder thresholds — nobody gave an exact cutoff, so these are a
// reasonable starting guess. Change these two numbers if your father has
// a different sense of "fresh" vs "getting old" for garlic.
const FRESH_THRESHOLD_DAYS = 7;
const AGING_THRESHOLD_DAYS = 14;

export function FreshnessBadge({ ageInDays }: FreshnessBadgeProps) {
  if (ageInDays === null) {
    return <span className="text-xs text-ink-soft">Not yet stocked</span>;
  }

  if (ageInDays > AGING_THRESHOLD_DAYS) {
    return (
      <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-debit-bg text-debit">
        {ageInDays} days old
      </span>
    );
  }
  if (ageInDays > FRESH_THRESHOLD_DAYS) {
    return (
      <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-[#F4E9D0] text-[#8A6A1F]">
        {ageInDays} days old
      </span>
    );
  }
  return (
    <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-credit-bg text-credit">
      {ageInDays} days old
    </span>
  );
}
```

Add this badge next to each row in the Products list, using the `ageInDays` field from `/api/products`.

### `components/GarlicIcon.tsx` — the one visual nod to "garlic shop," used sparingly
A literal garlic clipart/emoji would clash with the rest of the app's restrained look. This is a single-color line-art bulb, sized small, used only on the Products page header (next to "Products") — not repeated everywhere.
```tsx
export function GarlicIcon({ className = "" }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} xmlns="http://www.w3.org/2000/svg">
      <path
        d="M12 3c1 0 1.5 1 1.5 2 2.5 1 4 3.5 4 6.5 0 4-2.5 7.5-5.5 7.5S6.5 15.5 6.5 11.5c0-3 1.5-5.5 4-6.5 0-1 .5-2 1.5-2z"
        stroke="currentColor"
        strokeWidth="1.4"
        strokeLinejoin="round"
      />
      <path d="M12 5v14M9 8.5c0 2 1 3 3 3s3-1 3-3" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" />
    </svg>
  );
}
```
Use it like: `<GarlicIcon className="w-5 h-5 text-cloth" />` next to the "Products" page title — same maroon (`text-cloth`) as everything else, so it reads as part of the system, not a sticker stuck on top of it.

---

## Final Checklist Before Calling It V1
- [ ] All 6 entities working end to end (Postman-tested)
- [ ] Ledger balance is correct after mixed sales/purchases/payments
- [ ] Dashboard numbers match manual math
- [ ] Frontend can add a customer, a sale, a payment, and see the balance update
- [ ] Hindi/English toggle works on every page
- [ ] DB password is an env var, not committed to git
- [ ] `git commit` after every completed module, not just at the end
- [ ] Reports page shows correct totals for a manually-checked date range, and the chart matches real sale/purchase dates
- [ ] Tapping "Send via SMS" on a bill opens the phone's SMS app with the right number and text pre-filled (test on an actual phone browser, not desktop)
- [ ] Freshness badge shows the correct day count after a fresh purchase, and updates the next day

Come back to this file any time you need the next module's code — it's self-contained, so you don't need me in context to keep going.
