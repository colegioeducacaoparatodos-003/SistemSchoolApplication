package com.SistemSchool.report;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cabeçalho e rodapé comuns a todos os relatórios PDF do SistemSchool.
 */
public class ReportPageEvent extends PdfPageEventHelper {

    private static final BaseColor COLOR_BLACK  = new BaseColor(20, 20, 20);
    private static final BaseColor COLOR_RED    = new BaseColor(200, 16, 46);
    private static final BaseColor COLOR_YELLOW = new BaseColor(245, 180, 0);

    private static final Font FONT_TITLE    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.WHITE);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_YELLOW);
    private static final Font FONT_FOOTER   = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);

    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String documentTitle;

    public ReportPageEvent(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte canvas = writer.getDirectContent();
        float pageWidth  = document.getPageSize().getWidth();
        float pageHeight = document.getPageSize().getHeight();

        // Barra de cabeçalho preta
        canvas.saveState();
        canvas.setColorFill(COLOR_BLACK);
        canvas.rectangle(0, pageHeight - 60, pageWidth, 60);
        canvas.fill();

        // Faixa amarela no topo
        canvas.setColorFill(COLOR_YELLOW);
        canvas.rectangle(0, pageHeight - 3, pageWidth, 3);
        canvas.fill();
        canvas.restoreState();

        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                new Phrase("SistemSchool", FONT_TITLE), 40, pageHeight - 32, 0);

        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                new Phrase(documentTitle, FONT_SUBTITLE), 40, pageHeight - 48, 0);

        ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                new Phrase("Gerado em " + LocalDateTime.now().format(TIMESTAMP_FMT), FONT_SUBTITLE),
                pageWidth - 40, pageHeight - 48, 0);

        // Linha vermelha no rodapé
        canvas.saveState();
        canvas.setColorFill(COLOR_RED);
        canvas.rectangle(0, 0, pageWidth, 2);
        canvas.fill();
        canvas.restoreState();

        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("Página " + writer.getPageNumber(), FONT_FOOTER),
                pageWidth / 2, 25, 0);
    }
}