package com.SistemSchool.modulo_pedagogico.report;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.SistemSchool.modulo_pedagogico.dto.BoletimDTO;
import com.SistemSchool.modulo_pedagogico.dto.DisciplineGradeDTO;
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

@Component
public class BoletimPdfGenerator {

    private static final Font FONT_TITULO = new Font(Font.HELVETICA, 13, Font.BOLD);
    private static final Font FONT_SUBTITULO = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font FONT_LABEL = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font FONT_TEXTO = new Font(Font.HELVETICA, 11, Font.NORMAL);

    private static final String[] TRIMESTRES = { "", "1º", "2º", "3º" };

    public byte[] gerar(BoletimDTO dto, String schoolName) {

        Document document = new Document(PageSize.A4, 60, 60, 40, 40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            adicionarCabecalho(document, dto, schoolName);
            adicionarDadosDoAluno(document, dto);
            adicionarTabelaDeNotas(document, dto);
            adicionarAssinaturas(document);

            document.close();

        } catch (DocumentException e) {
            throw new IllegalStateException("Erro ao gerar o Boletim em PDF", e);
        }

        return baos.toByteArray();
    }

    private void adicionarCabecalho(Document document, BoletimDTO dto, String schoolName) throws DocumentException {

        document.add(centralizado("REPÚBLICA DE ANGOLA", FONT_SUBTITULO));
        document.add(centralizado("MINISTÉRIO DA EDUCAÇÃO", FONT_SUBTITULO));
        document.add(centralizado(schoolName.toUpperCase(Locale.ROOT), FONT_SUBTITULO));
        document.add(new Paragraph(" "));

        String trimestreExtenso = dto.getTrimester() != null && dto.getTrimester() >= 1 && dto.getTrimester() <= 3
                ? TRIMESTRES[dto.getTrimester()]
                : "";

        document.add(centralizado(
                "BOLETIM DE NOTA DO " + trimestreExtenso + " TRIMESTRE/" + valorOuVazio(dto.getAcademicYear()),
                FONT_TITULO));
        document.add(new Paragraph(" "));
    }

    private void adicionarDadosDoAluno(Document document, BoletimDTO dto) throws DocumentException {

        document.add(linha("NOME: ", dto.getStudentName()));
        document.add(new Paragraph(" "));

        Paragraph classeENumero = new Paragraph();
        classeENumero.add(new Phrase("CLASSE: ", FONT_LABEL));
        classeENumero.add(new Phrase(valorOuVazio(dto.getSchoolClassName()) + "          ", FONT_TEXTO));
        classeENumero.add(new Phrase("Nº ", FONT_LABEL));
        classeENumero.add(new Phrase(valorOuVazio(dto.getStudentNumber()), FONT_TEXTO));
        document.add(classeENumero);
        document.add(new Paragraph(" "));

        Paragraph anoEPeriodo = new Paragraph();
        anoEPeriodo.add(new Phrase("ANO LECTIVO: ", FONT_LABEL));
        anoEPeriodo.add(new Phrase(valorOuVazio(dto.getAcademicYear()) + "          ", FONT_TEXTO));
        anoEPeriodo.add(new Phrase("PERÍODO: ", FONT_LABEL));
        anoEPeriodo.add(new Phrase(valorOuVazio(dto.getPeriod()), FONT_TEXTO));
        document.add(anoEPeriodo);
        document.add(new Paragraph(" "));
    }

    private void adicionarTabelaDeNotas(Document document, BoletimDTO dto) throws DocumentException {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 2.2f, 1f });

        table.addCell(cabecalho("DISCIPLINA"));
        table.addCell(cabecalho("VALORES"));

        if (dto.getDisciplineGrades() != null) {
            for (DisciplineGradeDTO nota : dto.getDisciplineGrades()) {
                table.addCell(celula(nota.getDisciplineName(), Element.ALIGN_LEFT));
                table.addCell(celula(formatarNota(nota.getFinalGrade()), Element.ALIGN_CENTER));
            }
        }

        table.addCell(cabecalho("Comportamento"));
        table.addCell(celula(valorOuVazio(dto.getBehavior()), Element.ALIGN_CENTER));

        table.addCell(cabecalho("Observação"));
        table.addCell(celula(valorOuVazio(dto.getObservation()), Element.ALIGN_CENTER));

        document.add(table);
    }

    private void adicionarAssinaturas(Document document) throws DocumentException {

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        PdfPTable assinaturas = new PdfPTable(2);
        assinaturas.setWidthPercentage(100);

        assinaturas.addCell(assinaturaCell("O PROFESSOR"));
        assinaturas.addCell(assinaturaCell("O DIRECTOR"));

        document.add(assinaturas);
    }

    private PdfPCell assinaturaCell(String label) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph titulo = new Paragraph(label, FONT_LABEL);
        titulo.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(titulo);
        cell.addElement(new Paragraph(" "));

        Paragraph linha = new Paragraph("_______________________________");
        linha.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(linha);

        return cell;
    }

    private Paragraph centralizado(String texto, Font font) {
        Paragraph p = new Paragraph(texto, font);
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    private Paragraph linha(String label, String valor) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(label, FONT_LABEL));
        p.add(new Phrase(valorOuVazio(valor), FONT_TEXTO));
        return p;
    }

    private PdfPCell cabecalho(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_LABEL));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private PdfPCell celula(String texto, int alinhamento) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_TEXTO));
        cell.setPadding(6);
        cell.setHorizontalAlignment(alinhamento);
        return cell;
    }

    private String formatarNota(Double nota) {
        return nota == null ? "" : String.format(Locale.forLanguageTag("pt-AO"), "%.1f", nota);
    }

    private String valorOuVazio(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}