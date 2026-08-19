package com.SistemSchool.modulo_secrtaria.service;

import com.SistemSchool.modulo_secrtaria.dto.PagamentoDTO;
import com.SistemSchool.modulo_secrtaria.io.MesReferencia;
import com.SistemSchool.modulo_secrtaria.model.Pagamento;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.faces.context.FacesContext;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class PdfGeneratorService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────────────────────────────────────
    // IDENTIDADE DO COLÉGIO / LAYOUT DO RECIBO
    // ─────────────────────────────────────────────────────────────

    private static final String SCHOOL_NAME = "COMPLEXO ESCOLAR PRIVADO EDUCAÇÃO PARA TODOS";
    private static final String SCHOOL_PHONES = "924 259 557 / 953 087 821";
    private static final String LOGO_PATH = "/resources/imgs/logo.jpg";

    private static final float HEADER_HEIGHT = 120f;
    private static final float FOOTER_HEIGHT = 46f;

    // Paleta do cabeçalho: vinho -> vermelho -> dourado -> preto (gradiente)
    private static final Color GRAD_WINE = new Color(78, 17, 32);
    private static final Color GRAD_RED = new Color(150, 30, 34);
    private static final Color GRAD_GOLD = new Color(198, 140, 40);
    private static final Color GRAD_BLACK = new Color(24, 20, 18);

    // Cor escura usada nos textos/acentos do corpo do documento
    private static final Color HEADER_NAVY = new Color(58, 22, 26);
    private static final Color ACCENT_GOLD = new Color(214, 153, 45);

    private static final Font FATURA_TITLE_FONT = new Font(Font.HELVETICA, 17, Font.BOLD, Color.WHITE);
    private static final Font FATURA_NUMBER_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(255, 232, 200));
    private static final Font FATURA_CLIENT_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, HEADER_NAVY);
    private static final Font FATURA_CLIENT_SUB_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
    private static final Font FATURA_TABLE_HEADER_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, HEADER_NAVY);
    private static final Font FATURA_TABLE_CELL_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
    private static final Font FATURA_TOTAL_LABEL_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, HEADER_NAVY);
    private static final Font FATURA_TOTAL_VALUE_FONT = new Font(Font.HELVETICA, 13, Font.BOLD, HEADER_NAVY);
    private static final Font FATURA_DETAIL_LABEL_FONT = new Font(Font.HELVETICA, 8, Font.BOLD, Color.DARK_GRAY);
    private static final Font FATURA_DETAIL_VALUE_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
    private static final Font FATURA_FOOTER_TITLE_FONT = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font FATURA_FOOTER_SUB_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.WHITE);

    // Fontes usadas apenas no relatório de lista
    private static final Font LIST_TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font LIST_SUB_FONT = new Font(Font.HELVETICA, 9, Font.ITALIC);
    private static final Font LIST_HEADER_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font LIST_CELL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Font LIST_TOTAL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);

    private static final Color HEADER_BG = new Color(20, 20, 20);
    private static final Color ROW_ALT_BG = new Color(245, 245, 245);

    // ─────────────────────────────────────────────────────────────
    // PAGAMENTO — comprovativo individual (estilo "recibo")
    // ─────────────────────────────────────────────────────────────

    public byte[] generatePagamentoPdf(Pagamento pagamento) {
        Document document = new Document(PageSize.A5, 36, 36, HEADER_HEIGHT + 15, FOOTER_HEIGHT + 15);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            PdfContentByte cb = writer.getDirectContent();
            drawFaturaHeader(cb, document.getPageSize(), pagamento.getNumeroDocumento());
            drawFaturaFooter(cb, document.getPageSize());

            String alunoNome = pagamento.getEnrolment() != null && pagamento.getEnrolment().getStudent() != null
                    ? pagamento.getEnrolment().getStudent().getFullName()
                    : "-";
            String matricula = pagamento.getEnrolment() != null
                    ? pagamento.getEnrolment().getEnrolmentNumer()
                    : "-";

            Paragraph alunoParagraph = new Paragraph(alunoNome.toUpperCase(), FATURA_CLIENT_FONT);
            alunoParagraph.setSpacingAfter(2);
            document.add(alunoParagraph);

            Paragraph matriculaParagraph = new Paragraph("Matrícula: " + matricula, FATURA_CLIENT_SUB_FONT);
            matriculaParagraph.setSpacingAfter(16);
            document.add(matriculaParagraph);

            document.add(buildFaturaItemsTable(pagamento));

            Paragraph totalParagraph = new Paragraph();
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            totalParagraph.setSpacingBefore(10);
            totalParagraph.add(new Chunk("TOTAL   ", FATURA_TOTAL_LABEL_FONT));
            totalParagraph.add(new Chunk(formatMoeda(pagamento.getTotal()), FATURA_TOTAL_VALUE_FONT));
            document.add(totalParagraph);

            document.add(buildFaturaDetailsBlock(pagamento));

            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar o PDF do pagamento: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void drawFaturaHeader(PdfContentByte cb, Rectangle pageSize, String numeroDocumento) {
        float pageWidth = pageSize.getWidth();
        float pageHeight = pageSize.getHeight();
        float headerY = pageHeight - HEADER_HEIGHT;

        drawHeaderGradient(cb, 0, headerY, pageWidth, HEADER_HEIGHT);

        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("RECIBO DE PAGAMENTO", FATURA_TITLE_FONT), 36, pageHeight - 48, 0);

        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase(numeroDocumento != null ? numeroDocumento : "-", FATURA_NUMBER_FONT),
                36, pageHeight - 68, 0);

        try {
            Image logo = loadLogo();
            if (logo != null) {
                float maxWidth = 80f;
                float maxHeight = 55f;
                logo.scaleToFit(maxWidth, maxHeight);
                float x = pageWidth - 36 - logo.getScaledWidth();
                float y = pageHeight - (HEADER_HEIGHT / 2) - (logo.getScaledHeight() / 2);
                logo.setAbsolutePosition(x, y);
                cb.addImage(logo);
            }
        } catch (Exception e) {
            // Se o logotipo não puder ser carregado, o recibo é gerado sem ele
        }
    }

    /**
     * Preenche a área do cabeçalho com um gradiente horizontal composto por
     * vinho -> vermelho -> dourado -> preto, criado através de faixas
     * verticais com cor interpolada (o OpenPDF não oferece um gradiente
     * multi-stop nativo simples de configurar).
     */
    private void drawHeaderGradient(PdfContentByte cb, float x, float y, float width, float height) {
        Color[] stops = { GRAD_WINE, GRAD_RED, GRAD_GOLD, GRAD_BLACK };
        int steps = 90;
        float stepWidth = width / steps;

        cb.saveState();
        for (int i = 0; i < steps; i++) {
            float t = (float) i / (steps - 1);
            Color color = multiColorLerp(stops, t);
            cb.setColorFill(color);
            cb.rectangle(x + (i * stepWidth), y, stepWidth + 1f, height);
            cb.fill();
        }
        cb.restoreState();
    }

    private Color multiColorLerp(Color[] colors, float t) {
        int n = colors.length - 1;
        float scaled = Math.max(0f, Math.min(1f, t)) * n;
        int idx = (int) Math.floor(scaled);
        if (idx >= n) {
            idx = n - 1;
        }
        float localT = scaled - idx;
        Color c1 = colors[idx];
        Color c2 = colors[idx + 1];
        int r = Math.round(c1.getRed() + (c2.getRed() - c1.getRed()) * localT);
        int g = Math.round(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * localT);
        int b = Math.round(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * localT);
        return new Color(r, g, b);
    }

    private void drawFaturaFooter(PdfContentByte cb, Rectangle pageSize) {
        float pageWidth = pageSize.getWidth();

        cb.saveState();
        cb.setColorFill(ACCENT_GOLD);
        cb.rectangle(0, 0, pageWidth, FOOTER_HEIGHT);
        cb.fill();
        cb.restoreState();

        float logoX = 10f;
        float logoMaxSize = FOOTER_HEIGHT - 12f;
        float textStartX = logoX;

        try {
            Image logo = loadLogo();
            if (logo != null) {
                logo.scaleToFit(logoMaxSize, logoMaxSize);
                float y = (FOOTER_HEIGHT - logo.getScaledHeight()) / 2f;
                logo.setAbsolutePosition(logoX, y);
                cb.addImage(logo);
                textStartX = logoX + logo.getScaledWidth() + 8f;
            }
        } catch (Exception e) {
            // Se o logotipo não puder ser carregado, o texto ocupa o espaço todo
        }

        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase(SCHOOL_NAME, FATURA_FOOTER_TITLE_FONT), textStartX, FOOTER_HEIGHT / 2f + 2, 0);

        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase("Contacto: " + SCHOOL_PHONES, FATURA_FOOTER_SUB_FONT),
                pageWidth - 10, FOOTER_HEIGHT / 2f - 8, 0);
    }

    private Image loadLogo() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext == null) {
                return null;
            }
            try (InputStream is = facesContext.getExternalContext().getResourceAsStream(LOGO_PATH)) {
                if (is == null) {
                    return null;
                }
                return Image.getInstance(is.readAllBytes());
            }
        } catch (Exception e) {
            return null;
        }
    }

    private PdfPTable buildFaturaItemsTable(Pagamento pagamento) {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[] { 3.2f, 1.2f, 1.6f, 1.6f });
        } catch (DocumentException ignored) {
            // larguras válidas, nunca deve ocorrer
        }

        String[] headers = { "DESCRIÇÃO", "QTD", "PREÇO", "TAXA" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FATURA_TABLE_HEADER_FONT));
            cell.setBorder(Rectangle.BOTTOM);
            cell.setBorderWidth(1.2f);
            cell.setBorderColor(ACCENT_GOLD);
            cell.setPaddingBottom(6);
            cell.setHorizontalAlignment("DESCRIÇÃO".equals(h) ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            table.addCell(cell);
        }

        String descricaoPropina = describeMesReferencia(pagamento.getMesReferencia());
        addFaturaRow(table, descricaoPropina, "01",
                formatMoeda(pagamento.getValor()), formatMoeda(pagamento.getValor()));

        if (pagamento.getMulta() != null && pagamento.getMulta().signum() > 0) {
            addFaturaRow(table, "Multa Por Atraso", "01",
                    formatMoeda(pagamento.getMulta()), formatMoeda(pagamento.getMulta()));
        }

        return table;
    }

    /**
     * Mostra apenas o tipo digitado (ex.: "Propina" ou "Matrícula"), sem
     * prefixo fixo — deixa de forçar "Propina - X" para os casos em que X
     * já é o próprio tipo de pagamento.
     */
    private String describeMesReferencia(MesReferencia mes) {
        return mes != null ? mes.toString() : "Pagamento";
    }

    private void addFaturaRow(PdfPTable table, String descricao, String qtd, String preco, String taxa) {
        PdfPCell descCell = new PdfPCell(new Phrase(descricao, FATURA_TABLE_CELL_FONT));
        descCell.setBorder(Rectangle.NO_BORDER);
        descCell.setPaddingTop(8);
        descCell.setPaddingBottom(8);
        table.addCell(descCell);

        table.addCell(faturaCell(qtd, Element.ALIGN_RIGHT));
        table.addCell(faturaCell(preco, Element.ALIGN_RIGHT));
        table.addCell(faturaCell(taxa, Element.ALIGN_RIGHT));
    }

    private PdfPCell faturaCell(String value, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(value, FATURA_TABLE_CELL_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);
        cell.setPaddingTop(8);
        cell.setPaddingBottom(8);
        return cell;
    }

    private PdfPTable buildFaturaDetailsBlock(Pagamento pagamento) {
        PdfPTable details = new PdfPTable(2);
        details.setWidthPercentage(100);
        details.setSpacingBefore(20);
        try {
            details.setWidths(new float[] { 1f, 1f });
        } catch (DocumentException ignored) {
            // larguras válidas, nunca deve ocorrer
        }

        addDetailRow(details, "Forma de Pagamento:",
                pagamento.getFormaPagamento() != null ? pagamento.getFormaPagamento().toString() : "-");
        addDetailRow(details, "Caixa:",
                pagamento.getCashBox() != null ? pagamento.getCashBox().getCashBoxNumber() : "-");
        addDetailRow(details, "Data de Emissão:",
                pagamento.getDataEmissao() != null ? pagamento.getDataEmissao().format(DATETIME_FMT) : "-");
        addDetailRow(details, "Data do Pagamento:",
                pagamento.getDataPagamento() != null ? pagamento.getDataPagamento().format(DATETIME_FMT) : "-");
        addDetailRow(details, "Estado:",
                pagamento.getEstado() != null ? pagamento.getEstado().toString() : "-");
        addDetailRow(details, "Referência:",
                pagamento.getReferencia() != null ? pagamento.getReferencia() : "-");

        if (pagamento.getObservacao() != null && !pagamento.getObservacao().isBlank()) {
            PdfPCell obsLabel = new PdfPCell(new Phrase("Observação:", FATURA_DETAIL_LABEL_FONT));
            obsLabel.setBorder(Rectangle.NO_BORDER);
            obsLabel.setPaddingTop(4);
            details.addCell(obsLabel);

            PdfPCell obsValue = new PdfPCell(new Phrase(pagamento.getObservacao(), FATURA_DETAIL_VALUE_FONT));
            obsValue.setBorder(Rectangle.NO_BORDER);
            obsValue.setPaddingTop(4);
            details.addCell(obsValue);
        }

        return details;
    }

    private void addDetailRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FATURA_DETAIL_LABEL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingTop(3);
        labelCell.setPaddingBottom(3);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FATURA_DETAIL_VALUE_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingTop(3);
        valueCell.setPaddingBottom(3);
        table.addCell(valueCell);
    }

    // ─────────────────────────────────────────────────────────────
    // LISTA DE PAGAMENTOS — relatório tabular (inalterado)
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

    private String formatMoeda(BigDecimal valor) {
        return valor != null ? String.format("%,.2f Kz", valor) : "0,00 Kz";
    }

    private void addCell(PdfPTable table, String value, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "-", LIST_CELL_FONT));
        cell.setPadding(5);
        cell.setBackgroundColor(bg);
        table.addCell(cell);
    }
}