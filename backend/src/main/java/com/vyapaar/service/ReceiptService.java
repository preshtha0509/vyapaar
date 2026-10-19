package com.vyapaar.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vyapaar.dto.ReceiptLineResponse;
import com.vyapaar.dto.ReceiptResponse;
import com.vyapaar.entity.Customer;
import com.vyapaar.entity.Receipt;
import com.vyapaar.entity.ReceiptLine;
import com.vyapaar.entity.ReceiptSend;
import com.vyapaar.entity.Sale;
import com.vyapaar.repository.CustomerRepository;
import com.vyapaar.repository.ReceiptLineRepository;
import com.vyapaar.repository.ReceiptRepository;
import com.vyapaar.repository.ReceiptSendRepository;
import com.vyapaar.repository.SaleRepository;
import com.vyapaar.util.Money;
import com.vyapaar.util.PhoneNumbers;
import com.vyapaar.util.ReceiptTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Builds the day's receipt for a customer, renders it as a PDF, and prepares the WhatsApp link the
 * owner taps to send it.
 *
 * Creating a receipt never touches any balance. Balances stay computed in LedgerService.
 */
@Service
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ReceiptRepository receiptRepository;
    private final ReceiptLineRepository receiptLineRepository;
    private final ReceiptSendRepository receiptSendRepository;

    private final String publicUrl;
    private final String businessName;
    private final String businessPhone;

    public ReceiptService(SaleRepository saleRepository,
                          CustomerRepository customerRepository,
                          ReceiptRepository receiptRepository,
                          ReceiptLineRepository receiptLineRepository,
                          ReceiptSendRepository receiptSendRepository,
                          @Value("${receipt.public-url:http://localhost:8080}") String publicUrl,
                          @Value("${receipt.business-name:VYAPAAR}") String businessName,
                          @Value("${receipt.business-phone:}") String businessPhone) {
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.receiptRepository = receiptRepository;
        this.receiptLineRepository = receiptLineRepository;
        this.receiptSendRepository = receiptSendRepository;
        this.publicUrl = stripTrailingSlash(publicUrl);
        this.businessName = businessName;
        this.businessPhone = businessPhone;
    }

    /**
     * Returns the existing receipt for that customer and date, or creates it from that day's sales.
     *
     * This is deliberately idempotent: a link already sent to a customer must keep working, so an
     * existing receipt is returned untouched rather than regenerated with a new token.
     */
    @Transactional
    public Receipt getOrCreate(Long customerId, LocalDate date) {
        Optional<Receipt> existing = receiptRepository.findByCustomerIdAndReceiptDate(customerId, date);
        if (existing.isPresent()) {
            return existing.get();
        }

        List<Sale> sales = saleRepository.findByCustomerIdAndSaleDateOrderByIdAsc(customerId, date);
        if (sales.isEmpty()) {
            throw new IllegalArgumentException("No sales for that customer on that date.");
        }

        double subtotal = 0.0;
        for (Sale sale : sales) {
            subtotal += sale.getTotal() == null ? 0.0 : sale.getTotal();
        }

        Receipt receipt = new Receipt();
        receipt.setCustomerId(customerId);
        receipt.setReceiptDate(date);
        receipt.setReceiptNumber(nextReceiptNumber(date));
        receipt.setPublicToken(ReceiptTokens.newToken());
        receipt.setSubtotal(subtotal);
        receipt.setCreatedAt(LocalDateTime.now());
        Receipt saved = receiptRepository.save(receipt);

        for (Sale sale : sales) {
            ReceiptLine line = new ReceiptLine();
            line.setReceiptId(saved.getId());
            line.setSaleId(sale.getId());
            line.setProductName(sale.getProduct().getName());
            line.setUnit(sale.getProduct().getUnit().name());
            line.setQuantity(sale.getQuantity());
            line.setRate(sale.getRate());
            line.setLineTotal(sale.getTotal());
            receiptLineRepository.save(line);
        }

        return saved;
    }

    public List<ReceiptLine> lines(Receipt receipt) {
        return receiptLineRepository.findByReceiptIdOrderByIdAsc(receipt.getId());
    }

    public Receipt byToken(String token) {
        return receiptRepository.findByPublicToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found."));
    }

    public ReceiptResponse toResponse(Receipt receipt) {
        Customer customer = customerRepository.findById(receipt.getCustomerId()).orElse(null);
        List<ReceiptLineResponse> lineResponses = new ArrayList<>();
        for (ReceiptLine line : lines(receipt)) {
            lineResponses.add(new ReceiptLineResponse(
                    line.getProductName(), line.getUnit(), line.getQuantity(),
                    line.getRate(), line.getLineTotal()));
        }

        String whatsApp = null;
        if (customer != null) {
            whatsApp = PhoneNumbers.toE164India(customer.getPhone())
                    .map(number -> whatsAppLink(receipt, number, customer.getName()))
                    .orElse(null);
        }

        return new ReceiptResponse(
                receipt.getId(),
                receipt.getReceiptNumber(),
                receipt.getReceiptDate().toString(),
                customer == null ? "" : customer.getName(),
                receipt.getPublicToken(),
                receiptUrl(receipt),
                "/api/receipts/" + receipt.getPublicToken() + "/pdf",
                receipt.getSubtotal(),
                whatsApp,
                lineResponses);
    }

    /** The link that is shared with the buyer. Ends in .pdf so the SPA fallback never claims it. */
    public String receiptUrl(Receipt receipt) {
        return publicUrl + "/receipt/" + receipt.getPublicToken() + ".pdf";
    }

    public String whatsAppLink(Receipt receipt, String e164Number, String customerName) {
        String message = businessName + "\n"
                + "Receipt " + receipt.getReceiptNumber() + "\n"
                + (customerName == null || customerName.isBlank() ? "" : customerName + "\n")
                + "Date: " + receipt.getReceiptDate().format(DATE_FORMAT) + "\n"
                + "Total: " + Money.rupees(receipt.getSubtotal()) + "\n\n"
                + receiptUrl(receipt);

        // URLEncoder encodes a space as "+", which WhatsApp shows literally, so convert to %20.
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8).replace("+", "%20");
        return "https://wa.me/" + e164Number + "?text=" + encoded;
    }

    /** Appends one audit row. Never updates an existing one. */
    @Transactional
    public void recordSend(Receipt receipt, String channel, String recipient, String status, String error) {
        ReceiptSend send = new ReceiptSend();
        send.setReceiptId(receipt.getId());
        send.setChannel(channel);
        send.setRecipient(recipient);
        send.setStatus(status);
        send.setError(error);
        send.setCreatedAt(LocalDateTime.now());
        receiptSendRepository.save(send);
    }

    /**
     * Renders the receipt to PDF bytes. The PDF is never written to disk: Render's filesystem is
     * ephemeral, so it is rebuilt from the stored lines on every request.
     */
    public byte[] renderPdf(Receipt receipt) {
        Customer customer = customerRepository.findById(receipt.getCustomerId()).orElse(null);
        String xhtml = buildXhtml(receipt, customer, lines(receipt));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            registerFont(builder, "/fonts/NotoSans-Regular.ttf", "Noto Sans");
            registerFont(builder, "/fonts/NotoSans-Bold.ttf", "Noto Sans Bold");
            registerFont(builder, "/fonts/NotoSansDevanagari-Regular.ttf", "Noto Sans Devanagari");
            builder.withHtmlContent(xhtml, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not render the receipt PDF.", e);
        }
    }

    // The bundled fonts are what make the rupee sign and Devanagari text appear at all; the PDF
    // built-in fonts have neither. If one is missing the PDF still renders, with boxes for those
    // characters, so this warns loudly rather than failing the whole request.
    private void registerFont(PdfRendererBuilder builder, String resourcePath, String family) {
        if (getClass().getResource(resourcePath) == null) {
            log.warn("Receipt font missing from the classpath: {}. Some characters will not render.",
                    resourcePath);
            return;
        }
        builder.useFont(() -> {
            InputStream stream = getClass().getResourceAsStream(resourcePath);
            if (stream == null) {
                log.warn("Receipt font disappeared at render time: {}", resourcePath);
            }
            return stream;
        }, family);
    }

    private String nextReceiptNumber(LocalDate date) {
        long used = receiptRepository.countByReceiptDateBetween(
                ReceiptTokens.financialYearStart(date), ReceiptTokens.financialYearEnd(date));
        return ReceiptTokens.receiptNumber(date, used + 1);
    }

    /**
     * Builds the receipt as XHTML. Written with string concatenation to match BillService and to
     * avoid adding a template engine to the fixed tech stack. openhtmltopdf parses strictly, so
     * every tag is closed and every value is escaped.
     */
    private String buildXhtml(Receipt receipt, Customer customer, List<ReceiptLine> lines) {
        StringBuilder rows = new StringBuilder();
        for (ReceiptLine line : lines) {
            rows.append("<tr>")
                    .append("<td>").append(escape(line.getProductName())).append("</td>")
                    .append("<td class=\"num\">").append(Money.quantity(line.getQuantity()))
                    .append(" ").append(escape(line.getUnit())).append("</td>")
                    .append("<td class=\"num\">").append(Money.rupees(line.getRate())).append("</td>")
                    .append("<td class=\"num\">").append(Money.rupees(line.getLineTotal())).append("</td>")
                    .append("</tr>");
        }

        String customerBlock = customer == null ? "" :
                "<p class=\"party\"><strong>" + escape(customer.getName()) + "</strong>"
                        + (customer.getCity() == null || customer.getCity().isBlank()
                            ? "" : "<br />" + escape(customer.getCity()))
                        + (customer.getPhone() == null || customer.getPhone().isBlank()
                            ? "" : "<br />" + escape(customer.getPhone()))
                        + "</p>";

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                + "<head>"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
                + "<title>" + escape(receipt.getReceiptNumber()) + "</title>"
                + "<style>"
                + "@page { size: A4; margin: 18mm; }"
                + "body { font-family: 'Noto Sans', 'Noto Sans Devanagari', sans-serif;"
                + " font-size: 11pt; color: #1a1a1a; }"
                + "h1 { font-size: 18pt; margin: 0 0 2mm 0; }"
                + ".muted { color: #555555; font-size: 9pt; margin: 0; }"
                + ".meta { margin: 6mm 0 4mm 0; font-size: 10pt; }"
                + ".party { margin: 0 0 6mm 0; font-size: 10pt; line-height: 1.5; }"
                + "table { width: 100%; border-collapse: collapse; margin-top: 2mm; }"
                + "th, td { border: 0.5pt solid #bbbbbb; padding: 2.5mm 3mm; text-align: left; }"
                + "th { background-color: #f2f0ec; font-size: 9pt; text-transform: uppercase; }"
                + ".num { text-align: right; }"
                + "tfoot td { font-weight: bold; background-color: #f2f0ec; }"
                + "hr { border: 0; border-top: 0.5pt solid #bbbbbb; margin: 8mm 0 3mm 0; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<h1>" + escape(businessName) + "</h1>"
                + (businessPhone == null || businessPhone.isBlank()
                    ? "" : "<p class=\"muted\">" + escape(businessPhone) + "</p>")
                + "<p class=\"meta\"><strong>Receipt " + escape(receipt.getReceiptNumber())
                + "</strong><br />Date: " + receipt.getReceiptDate().format(DATE_FORMAT) + "</p>"
                + customerBlock
                + "<table>"
                + "<thead><tr><th>Item</th><th class=\"num\">Quantity</th>"
                + "<th class=\"num\">Rate</th><th class=\"num\">Amount</th></tr></thead>"
                + "<tbody>" + rows + "</tbody>"
                + "<tfoot><tr><td colspan=\"3\">Total</td>"
                + "<td class=\"num\">" + Money.rupees(receipt.getSubtotal()) + "</td></tr></tfoot>"
                + "</table>"
                + "<hr />"
                + "<p class=\"muted\">This is a computer-generated receipt.</p>"
                + "</body></html>";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
