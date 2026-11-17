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
    private static final float MIN_PRINTABLE_FOOTER_Y = 24f;
    private static final float FOOTER_CONTENT_GAP = 14f;
    private static final DateTimeFormatter ISSUED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String userLabel;
    private final String issuedAt = "Emitido em: " + LocalDateTime.now().format(ISSUED_AT_FORMAT);
    private final float footerTextY;
    private final boolean useDocumentMargin;

    public PdfAuditPageEvent() {
        this(PdfAuditIdentity.getCurrentUserLabel());
    }

    protected PdfAuditPageEvent(float footerTextY) {
        this(PdfAuditIdentity.getCurrentUserLabel(), footerTextY, false);
    }

    protected PdfAuditPageEvent(String userLabel) {
        this(userLabel, 0, true);
    }

    protected PdfAuditPageEvent(String userLabel, float footerTextY) {
        this(userLabel, footerTextY, false);
    }

    private PdfAuditPageEvent(String userLabel, float footerTextY, boolean useDocumentMargin) {
        this.userLabel = userLabel;
        this.footerTextY = footerTextY;
        this.useDocumentMargin = useDocumentMargin;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        float textY = useDocumentMargin
                ? Math.max(MIN_PRINTABLE_FOOTER_Y, document.bottom() + FOOTER_CONTENT_GAP)
                : footerTextY;
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT,
                new Phrase(userLabel, FOOTER_FONT), document.left(), textY, 0);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                new Phrase(issuedAt, FOOTER_FONT), document.right(), textY, 0);
    }
}
