package com.SistemSchool.modulo_secrtaria.service;

import com.SistemSchool.modulo_secrtaria.dto.PagamentoDTO;
import com.SistemSchool.modulo_secrtaria.model.Pagamento;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class PdfGeneratorService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC);

    // Fontes usadas apenas no relatório de lista
    private static final Font LIST_TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font LIST_SUB_FONT = new Font(Font.HELVETICA, 9, Font.ITALIC);
    private static final Font LIST_HEADER_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font LIST_CELL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Font LIST_TOTAL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);

    private static final Color HEADER_BG = new Color(20, 20, 20);
    private static final Color ROW_ALT_BG = new Color(245, 245, 245);

    // ─────────────────────────────────────────────────────────────
    // PAGAMENTO — comprovativo individual
    // ─────────────────────────────────────────────────────────────

    public byte[] generatePagamentoPdf(Pagamento pagamento) {
        Document document = new Document(PageSize.A5);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addTitle(document, "COMPROVATIVO DE PAGAMENTO", pagamento.getNumeroDocumento());

            addField(document, "Aluno:",
                    pagamento.getEnrolment() != null && pagamento.getEnrolment().getStudent() != null
                            ? pagamento.getEnrolment().getStudent().getFullName()
                            : "-");

            addField(document, "Matrícula:",
                    pagamento.getEnrolment() != null ? pagamento.getEnrolment().getEnrolmentNumer() : "-");

            addField(document, "Propina:",
                    pagamento.getFee() != null ? String.valueOf(pagamento.getFee().getPhFee()) : "-");

            addField(document, "Mês de Referência:",
                    pagamento.getMesReferencia() != null ? pagamento.getMesReferencia().toString() : "-");

            addField(document, "Caixa:",
                    pagamento.getCashBox() != null ? pagamento.getCashBox().getCashBoxNumber() : "-");

            addField(document, "Forma de Pagamento:",
                    pagamento.getFormaPagamento() != null ? pagamento.getFormaPagamento().toString() : "-");

            addField(document, "Valor:", formatMoney(pagamento.getValor()));
            addField(document, "Multa:", formatMoney(pagamento.getMulta()));
            addField(document, "Total:", formatMoney(pagamento.getTotal()));

            addField(document, "Data de Emissão:",
                    pagamento.getDataEmissao() != null ? pagamento.getDataEmissao().format(DATETIME_FMT) : "-");

            addField(document, "Data do Pagamento:",
                    pagamento.getDataPagamento() != null ? pagamento.getDataPagamento().format(DATETIME_FMT) : "-");

            addField(document, "Estado:",
                    pagamento.getEstado() != null ? pagamento.getEstado().toString() : "-");

            addField(document, "Referência:",
                    pagamento.getReferencia() != null ? pagamento.getReferencia() : "-");

            if (pagamento.getObservacao() != null && !pagamento.getObservacao().isBlank()) {
                addField(document, "Observação:", pagamento.getObservacao());
            }

            addFooter(document);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar o PDF do pagamento: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────
    // LISTA DE PAGAMENTOS — relatório tabular
    // ─────────────────────────────────────────────────────────────

    public byte[] generatePagamentosListPdf(List<PagamentoDTO> pagamentos, String titulo) {
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 20);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Paragraph tituloParagraph = new Paragraph(titulo != null ? titulo : "Lista de Pagamentos", LIST_TITLE_FONT);
            tituloParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(tituloParagraph);

            Paragraph geradoEm = new Paragraph(
                    "Gerado em: " + LocalDateTime.now().format(DATETIME_FMT), LIST_SUB_FONT);
            geradoEm.setAlignment(Element.ALIGN_CENTER);
            geradoEm.setSpacingAfter(15);
            document.add(geradoEm);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 2.2f, 2.8f, 1.6f, 1.6f, 1.6f, 1.6f, 1.3f, 1.6f });

            String[] headers = { "Nº Documento", "Aluno", "Valor Pago", "Total",
                    "Método", "Data Pagamento", "Estado", "Referência" };
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, LIST_HEADER_FONT));
                cell.setBackgroundColor(HEADER_BG);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            boolean alt = false;
            for (PagamentoDTO p : pagamentos) {
                Color bg = alt ? ROW_ALT_BG : Color.WHITE;
                alt = !alt;

                addCell(table, p.getNumeroDocumento(), bg);
                addCell(table, p.getStudentFullName(), bg);
                addCell(table, formatMoeda(p.getTotal()), bg);
                addCell(table, p.getFormaPagamento() != null ? p.getFormaPagamento().getDescricao() : "-", bg);
                addCell(table, p.getDataPagamento() != null ? p.getDataPagamento().format(DATETIME_FMT) : "-", bg);
                addCell(table, p.getEstado() != null ? p.getEstado().getDescricao() : "-", bg);
                addCell(table, p.getReferencia() != null ? p.getReferencia() : "-", bg);
            }

            document.add(table);

            BigDecimal totalGeral = pagamentos.stream()
                    .map(PagamentoDTO::getTotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Paragraph totalParagraph = new Paragraph(
                    "\nTotal de registos: " + pagamentos.size() +
                            "   |   Valor total pago: " + formatMoeda(totalGeral),
                    LIST_TOTAL_FONT);
            totalParagraph.setSpacingBefore(10);
            document.add(totalParagraph);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF da lista de pagamentos: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private String formatMoney(BigDecimal value) {
        return value != null ? String.format("Kz %,.2f", value) : "-";
    }

    private String formatMoeda(BigDecimal valor) {
        return valor != null ? String.format("%,.2f Kz", valor) : "0,00 Kz";
    }

    private void addTitle(Document document, String title, String number) throws DocumentException {
        Paragraph t = new Paragraph(title, TITLE_FONT);
        t.setAlignment(Element.ALIGN_CENTER);
        document.add(t);

        Paragraph n = new Paragraph(number != null ? number : "-", LABEL_FONT);
        n.setAlignment(Element.ALIGN_CENTER);
        n.setSpacingAfter(15);
        document.add(n);
    }

    private void addField(Document document, String label, String value) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", LABEL_FONT));
        p.add(new Chunk(value != null ? value : "-", VALUE_FONT));
        p.setSpacingAfter(6);
        document.add(p);
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph(
                "Documento gerado eletronicamente pelo sistema de gestão escolar.", SMALL_FONT);
        footer.setSpacingBefore(30);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addCell(PdfPTable table, String value, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "-", LIST_CELL_FONT));
        cell.setPadding(5);
        cell.setBackgroundColor(bg);
        table.addCell(cell);
    }
}