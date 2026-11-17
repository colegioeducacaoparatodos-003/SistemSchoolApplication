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
    private static final float MIN_PRINTABLE_FOOTER_Y = 24f;
    private static final float FOOTER_CONTENT_GAP = 14f;
    private static final DateTimeFormatter ISSUED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String userLabel;
    private final String issuedAt = "Emitido em: " + LocalDateTime.now().format(ISSUED_AT_FORMAT);

    public OpenPdfAuditPageEvent() {
        this(PdfAuditIdentity.getCurrentUserLabel());
    }

    protected OpenPdfAuditPageEvent(String userLabel) {
        this.userLabel = userLabel;
    }

    protected String getUserLabel() {
        return userLabel;
    }

    protected String getIssuedAt() {
        return issuedAt;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        float textY = Math.max(MIN_PRINTABLE_FOOTER_Y, document.bottom() + FOOTER_CONTENT_GAP);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT,
                new Phrase(userLabel, FOOTER_FONT), document.left(), textY, 0);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                new Phrase(issuedAt, FOOTER_FONT), document.right(), textY, 0);
    }
}
