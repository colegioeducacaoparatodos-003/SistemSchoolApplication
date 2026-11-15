package com.SistemSchool.report;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cabeçalho e rodapé comuns a todos os relatórios PDF do SistemSchool.
 */
public class ReportPageEvent extends PdfAuditPageEvent {

    private static final BaseColor COLOR_BLACK  = new BaseColor(20, 20, 20);
    private static final BaseColor COLOR_RED    = new BaseColor(200, 16, 46);
    private static final BaseColor COLOR_YELLOW = new BaseColor(245, 180, 0);

    private static final Font FONT_TITLE    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.WHITE);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_YELLOW);
    private static final Font FONT_FOOTER   = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);

    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String documentTitle;
        private final String brandName;
        private final Image logo;
        private final float footerTextY;
        private final float footerRuleY;

    public ReportPageEvent(String documentTitle) {
                this(documentTitle, "COMPLEXO ESCOLA PRIVADO EDUCAÇÃO PARA TODOS", null, 25, 0);
        }

        public ReportPageEvent(String documentTitle, String brandName, Image logo) {
                this(documentTitle, brandName, logo, 25, 0);
        }

        public ReportPageEvent(String documentTitle, String brandName, Image logo,
                float footerTextY, float footerRuleY) {
                    this(documentTitle, brandName, logo, footerTextY, footerRuleY, 8f);
            }

            public ReportPageEvent(String documentTitle, String brandName, Image logo,
                    float footerTextY, float footerRuleY, float auditFooterTextY) {
            super(auditFooterTextY);
        this.documentTitle = documentTitle;
                this.brandName = brandName;
                this.logo = logo;
                this.footerTextY = footerTextY;
                this.footerRuleY = footerRuleY;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        super.onEndPage(writer, document);
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

                float brandTextX = 40;
                if (logo != null) {
                        try {
                                logo.scaleToFit(32, 32);
                                logo.setAbsolutePosition(40, pageHeight - 48);
                                canvas.addImage(logo);
                                brandTextX = 80;
                        } catch (Exception ignored) {
                        }
                }

                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                                new Phrase(brandName, FONT_TITLE), brandTextX, pageHeight - 32, 0);

        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                new Phrase(documentTitle, FONT_SUBTITLE), 40, pageHeight - 48, 0);

        ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                new Phrase("Gerado em " + LocalDateTime.now().format(TIMESTAMP_FMT), FONT_SUBTITLE),
                pageWidth - 40, pageHeight - 48, 0);

        // Linha vermelha no rodapé
        canvas.saveState();
        canvas.setColorFill(COLOR_RED);
        canvas.rectangle(0, footerRuleY, pageWidth, 2);
        canvas.fill();
        canvas.restoreState();

        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase("Página " + writer.getPageNumber(), FONT_FOOTER),
                pageWidth / 2, footerTextY, 0);
    }
}