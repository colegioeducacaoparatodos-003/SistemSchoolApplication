package com.SistemSchool.report;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfAuditPageEvent extends PdfPageEventHelper {

    private static final Font FOOTER_FONT =
            FontFactory.getFont(FontFactory.HELVETICA, 6, Font.NORMAL, new BaseColor(70, 70, 70));
    private static final float DEFAULT_FOOTER_TEXT_Y = 8f;
    private static final DateTimeFormatter ISSUED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String userLabel;
    private final String issuedAt = "Emitido em: " + LocalDateTime.now().format(ISSUED_AT_FORMAT);
    private final float footerTextY;

    public PdfAuditPageEvent() {
        this(DEFAULT_FOOTER_TEXT_Y);
    }

    protected PdfAuditPageEvent(float footerTextY) {
        this(PdfAuditIdentity.getCurrentUserLabel(), footerTextY);
    }

    protected PdfAuditPageEvent(String userLabel) {
        this(userLabel, DEFAULT_FOOTER_TEXT_Y);
    }

    protected PdfAuditPageEvent(String userLabel, float footerTextY) {
        this.userLabel = userLabel;
        this.footerTextY = footerTextY;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT,
                new Phrase(userLabel, FOOTER_FONT), document.left(), footerTextY, 0);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                new Phrase(issuedAt, FOOTER_FONT), document.right(), footerTextY, 0);
    }
}
