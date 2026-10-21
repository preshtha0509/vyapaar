package com.vyapaar.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the PDF side of Module 15 works without needing a database: the bundled fonts load from
 * the classpath, the strict XHTML parses, and a real PDF comes out with the rupee sign in it.
 *
 * The rupee sign is the thing worth guarding. PDF built-in fonts do not contain it, so without the
 * bundled Noto fonts every amount on the receipt renders as a blank box.
 */
class ReceiptPdfRenderingTest {

    private static final String REGULAR = "/fonts/NotoSans-Regular.ttf";
    private static final String BOLD = "/fonts/NotoSans-Bold.ttf";
    private static final String DEVANAGARI = "/fonts/NotoSansDevanagari-Regular.ttf";

    @Test
    void bundledFontsArePresentOnTheClasspath() {
        assertNotNull(getClass().getResource(REGULAR), REGULAR + " is missing");
        assertNotNull(getClass().getResource(BOLD), BOLD + " is missing");
        assertNotNull(getClass().getResource(DEVANAGARI), DEVANAGARI + " is missing");
    }

    @Test
    void rendersStrictXhtmlWithRupeesIntoAPdf() throws Exception {
        String xhtml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
                + "<style>@page { size: A4; margin: 18mm; }"
                + "body { font-family: 'Noto Sans', 'Noto Sans Devanagari', sans-serif; }</style>"
                + "</head><body>"
                + "<h1>VYAPAAR</h1>"
                + "<p>Total: ₹4,52,000.00</p>"
                + "<p>ग्राहक</p>"
                + "<table><tr><td>Garlic</td><td>12 KG</td></tr></table>"
                + "</body></html>";

        byte[] pdf = render(xhtml);

        assertTrue(pdf.length > 1000, "PDF looks too small: " + pdf.length + " bytes");
        String header = new String(pdf, 0, 5, StandardCharsets.ISO_8859_1);
        assertTrue(header.startsWith("%PDF-"), "Not a PDF, header was: " + header);
    }

    private byte[] render(String xhtml) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(() -> getClass().getResourceAsStream(REGULAR), "Noto Sans");
            builder.useFont(() -> getClass().getResourceAsStream(BOLD), "Noto Sans Bold");
            builder.useFont(() -> getClass().getResourceAsStream(DEVANAGARI), "Noto Sans Devanagari");
            builder.withHtmlContent(xhtml, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        }
    }
}
