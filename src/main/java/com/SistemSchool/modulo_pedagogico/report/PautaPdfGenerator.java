package com.SistemSchool.modulo_pedagogico.report;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.SistemSchool.modulo_pedagogico.dto.AlunoNotaDTO;
import com.SistemSchool.modulo_pedagogico.dto.PautaDTO;
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
public class PautaPdfGenerator {

    private static final Font FONT_TITULO = new Font(Font.HELVETICA, 13, Font.BOLD);
    private static final Font FONT_SUBTITULO = new Font(Font.HELVETICA, 11, Font.NORMAL);
    private static final Font FONT_CABECALHO = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font FONT_TEXTO = new Font(Font.HELVETICA, 10, Font.NORMAL);

    private static final String[] TRIMESTRES = { "", "1º", "2º", "3º" };

    public byte[] gerar(PautaDTO dto, String schoolName) {

        Document document = new Document(PageSize.A4.rotate(), 40, 40, 50, 40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            adicionarCabecalho(document, dto, schoolName);
            adicionarTabela(document, dto);

            document.close();

        } catch (DocumentException e) {
            throw new IllegalStateException("Erro ao gerar a Pauta em PDF", e);
        }

        return baos.toByteArray();
    }

    private void adicionarCabecalho(Document document, PautaDTO dto, String schoolName) throws DocumentException {

        Paragraph escola = new Paragraph(schoolName.toUpperCase(Locale.ROOT), FONT_SUBTITULO);
        escola.setAlignment(Element.ALIGN_CENTER);
        document.add(escola);

        String trimestreExtenso = dto.getTrimester() != null && dto.getTrimester() >= 1 && dto.getTrimester() <= 3
                ? TRIMESTRES[dto.getTrimester()]
                : "";

        Paragraph titulo = new Paragraph(
                "PAUTA DE NOTAS — " + valorOuVazio(dto.getDisciplineName()).toUpperCase(Locale.ROOT)
                        + " — " + trimestreExtenso + " TRIMESTRE/" + valorOuVazio(dto.getAcademicYear()),
                FONT_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph turma = new Paragraph("Turma: " + valorOuVazio(dto.getSchoolClassName()), FONT_SUBTITULO);
        document.add(turma);

        document.add(new Paragraph(" "));
    }

    private void adicionarTabela(Document document, PautaDTO dto) throws DocumentException {

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 0.6f, 3f, 1f, 1.2f });

        table.addCell(cabecalho("Nº"));
        table.addCell(cabecalho("Aluno"));
        table.addCell(cabecalho("Nota"));
        table.addCell(cabecalho("Situação"));

        if (dto.getStudentGrades() != null) {
            for (AlunoNotaDTO aluno : dto.getStudentGrades()) {
                table.addCell(celula(String.valueOf(aluno.getNumber()), Element.ALIGN_CENTER));
                table.addCell(celula(aluno.getStudentName(), Element.ALIGN_LEFT));
                table.addCell(celula(formatarNota(aluno.getFinalGrade()), Element.ALIGN_CENTER));
                table.addCell(celula(valorOuVazio(aluno.getSituation()), Element.ALIGN_CENTER));
            }
        }

        document.add(table);
    }

    private PdfPCell cabecalho(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_CABECALHO));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell celula(String texto, int alinhamento) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_TEXTO));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alinhamento);
        return cell;
    }

    private String formatarNota(Double nota) {
        return nota == null ? "-" : String.format(Locale.forLanguageTag("pt-AO"), "%.1f", nota);
    }

    private String valorOuVazio(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}