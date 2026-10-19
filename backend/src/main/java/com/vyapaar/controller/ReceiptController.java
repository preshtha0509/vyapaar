package com.vyapaar.controller;

import com.vyapaar.dto.ReceiptCreateRequest;
import com.vyapaar.dto.ReceiptResponse;
import com.vyapaar.dto.ReceiptSendRequest;
import com.vyapaar.entity.Receipt;
import com.vyapaar.service.ReceiptService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

/**
 * There is no class-level request mapping on purpose: the owner-facing endpoints live under /api,
 * while the buyer's link is /receipt/{token}.pdf. The .pdf ending matters, because
 * SpaFallbackController claims paths without a dot and would otherwise forward it to index.html.
 */
@RestController
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    /** Creates the receipt for a customer and date, or returns the one that already exists. */
    @PostMapping("/api/receipts")
    public ReceiptResponse create(@RequestBody ReceiptCreateRequest request) {
        if (request.customerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required.");
        }
        LocalDate date = request.date() == null ? LocalDate.now() : request.date();
        try {
            return receiptService.toResponse(receiptService.getOrCreate(request.customerId(), date));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /** Owner-facing preview of the PDF. */
    @GetMapping("/api/receipts/{token}/pdf")
    public ResponseEntity<byte[]> pdfForOwner(@PathVariable String token) {
        return pdf(token);
    }

    /** The buyer's public link. Anyone holding the token can read this one receipt. */
    @GetMapping("/receipt/{token}.pdf")
    public ResponseEntity<byte[]> publicPdf(@PathVariable String token) {
        return pdf(token);
    }

    /** Appends an audit row recording that the owner sent this receipt. */
    @PostMapping("/api/receipts/{token}/sends")
    public ResponseEntity<Void> recordSend(@PathVariable String token,
                                           @RequestBody ReceiptSendRequest request) {
        Receipt receipt = find(token);
        String channel = request.channel() == null ? "UNKNOWN" : request.channel();
        receiptService.recordSend(receipt, channel, request.recipient(), "OPENED", null);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<byte[]> pdf(String token) {
        Receipt receipt = find(token);
        byte[] bytes = receiptService.renderPdf(receipt);
        String filename = receipt.getReceiptNumber().replace("/", "-") + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                // The link is unlisted, so keep it out of search engines.
                .header("X-Robots-Tag", "noindex, nofollow")
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=0, no-store")
                .body(bytes);
    }

    // A bad token and an unknown token are both plain 404s, so nothing can be learned by probing.
    private Receipt find(String token) {
        try {
            return receiptService.byToken(token);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt not found.");
        }
    }
}
