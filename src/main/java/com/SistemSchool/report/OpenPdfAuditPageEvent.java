package com.SistemSchool.report;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OpenPdfAuditPageEvent extends PdfPageEventHelper {

    private static final Font FOOTER_FONT =
            FontFactory.getFont(FontFactory.HELVETICA, 6, Font.NORMAL, new Color(70, 70, 70));
    private static final float DEFAULT_FOOTER_TEXT_Y = 8f;
    private static final DateTimeFormatter ISSUED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String userLabel;
    private final String issuedAt = "Emitido em: " + LocalDateTime.now().format(ISSUED_AT_FORMAT);
    private final float footerTextY;

    public OpenPdfAuditPageEvent() {
        this(DEFAULT_FOOTER_TEXT_Y);
    }

    public OpenPdfAuditPageEvent(float footerTextY) {
        this(PdfAuditIdentity.getCurrentUserLabel(), footerTextY);
    }

    protected OpenPdfAuditPageEvent(String userLabel) {
        this(userLabel, DEFAULT_FOOTER_TEXT_Y);
    }

    protected OpenPdfAuditPageEvent(String userLabel, float footerTextY) {
        this.userLabel = userLabel;
        this.footerTextY = footerTextY;
    }

    protected String getUserLabel() {
        return userLabel;
    }

    protected String getIssuedAt() {
        return issuedAt;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT,
                new Phrase(userLabel, FOOTER_FONT), document.left(), footerTextY, 0);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                new Phrase(issuedAt, FOOTER_FONT), document.right(), footerTextY, 0);
    }
}
