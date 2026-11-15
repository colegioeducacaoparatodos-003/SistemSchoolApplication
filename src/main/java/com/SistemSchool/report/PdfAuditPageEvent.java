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
            FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL, new BaseColor(70, 70, 70));
    private static final DateTimeFormatter ISSUED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String userLabel;
    private final String issuedAt = "Emitido em: " + LocalDateTime.now().format(ISSUED_AT_FORMAT);

    public PdfAuditPageEvent() {
        this(PdfAuditIdentity.getCurrentUserLabel());
    }

    protected PdfAuditPageEvent(String userLabel) {
        this.userLabel = userLabel;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT,
                new Phrase(userLabel, FOOTER_FONT), document.left(), 8, 0);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                new Phrase(issuedAt, FOOTER_FONT), document.right(), 8, 0);
    }
}
