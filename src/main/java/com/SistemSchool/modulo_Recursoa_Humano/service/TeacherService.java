package com.SistemSchool.modulo_Recursoa_Humano.service;

import java.io.IOException;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;

import org.primefaces.model.file.UploadedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.SistemSchool.io.Assistant;
import com.SistemSchool.io.FileImage;

import jakarta.servlet.ServletContext;
import com.SistemSchool.modulo_Recursoa_Humano.dto.TeacherDTO;
import com.SistemSchool.modulo_Recursoa_Humano.interfaces.TeacherTableProjection;
import com.SistemSchool.modulo_Recursoa_Humano.io.ContractType;
import com.SistemSchool.modulo_Recursoa_Humano.io.QualificationLevel;
import com.SistemSchool.modulo_Recursoa_Humano.io.TeacherStatus;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;
import com.SistemSchool.modulo_Recursoa_Humano.repository.TeacherRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class TeacherService {

    private static final String TEACHER_IMG_FOLDER = "teacher_img";
    private static final String TEACHER_PREFIX = "PROF";
    private static final int SEQUENCE_LENGTH = 5;

    private final TeacherRepository repository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.repository = teacherRepository;
    }

    // ---------------------
    // CRUD
    // ---------------------

    /**
     * Garante que a pasta de imagens do professor existe.
     * Se não existir, cria a pasta.
     * @throws RuntimeException se não conseguir criar a pasta
     */
    private void ensureTeacherImgFolderExists() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                String webRoot = facesContext.getExternalContext().getRealPath("/");
                Path folderPath = Paths.get(webRoot, TEACHER_IMG_FOLDER);
                
                if (!Files.exists(folderPath)) {
                    Files.createDirectories(folderPath);
                    System.out.println("Pasta " + TEACHER_IMG_FOLDER + " foi criada com sucesso em: " + folderPath);
                }
            }
        } catch (IOException e) {
            String errorMsg = "Erro ao criar pasta para imagens de professores: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        }
    }

    public Teacher save(Teacher teacher, UploadedFile photo) throws IOException {

        teacher.setTeacherNumber(generateTeacherNumber());

        String newNameFile = "default.png";
        if (photo != null && photo.getSize() > 0) {
            try {
                // Garante que a pasta existe
                ensureTeacherImgFolderExists();
                
                FileImage fileImage = new FileImage();
                Assistant assistant = new Assistant();
                newNameFile = "0" + assistant.novoNome(photo.getContentType());
                fileImage.salvarArquivo(photo, TEACHER_IMG_FOLDER, newNameFile);
                System.out.println("Foto do professor salva com sucesso: " + newNameFile);
            } catch (IOException e) {
                String errorMsg = "Erro ao salvar imagem do professor: " + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();
                // Continua com default.png se houver erro
                newNameFile = "default.png";
                throw new IOException(errorMsg, e);
            } catch (Exception e) {
                String errorMsg = "Erro inesperado ao processar imagem do professor: " + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();
                newNameFile = "default.png";
                throw new IOException(errorMsg, e);
            }
        }
        teacher.setPhotoPhath(newNameFile);

        teacher.setCreatedAt(LocalDateTime.now());
        teacher.setUpdatedAt(LocalDateTime.now());

        try {
            Teacher savedTeacher = repository.save(teacher);
            System.out.println("Professor registado com sucesso: " + teacher.getTeacherNumber());
            return savedTeacher;
        } catch (Exception e) {
            String errorMsg = "Erro ao salvar professor na base de dados: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            throw new IOException(errorMsg, e);
        }
    }

    public Teacher findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));
    }

    public void update(TeacherDTO dto) {
        Teacher teacher = repository.findById(dto.getPkTeacher())
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));

        teacher.setFristName(dto.getFristName());
        teacher.setLastName(dto.getLastName());
        teacher.setGender(dto.getGender());
        teacher.setQualificationLivel(dto.getQualificationLivel());
        teacher.setContractType(dto.getContractType());
        teacher.setStatus(dto.getStatus());
        teacher.setBiNumber(dto.getBiNumber());
        teacher.setBiExpiryDate(dto.getBiExpiryDate());
        teacher.setAddressStreet(dto.getAddressStreet());
        teacher.setAddressProvice(dto.getAddressProvice());
        teacher.setBaseSalary(dto.getBaseSalary());
        teacher.setEmail(dto.getEmail());
        teacher.setPhone(dto.getPhone());
        teacher.setMobilePhone(dto.getMobilePhone());
        teacher.setObs(dto.getObs());
        teacher.setUpdatedAt(LocalDateTime.now());

        repository.save(teacher);
    }

    public void updatePhoto(Long pkTeacher, UploadedFile photo) throws IOException {
        Teacher teacher = repository.findById(pkTeacher)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));

        if (photo != null && photo.getSize() > 0) {
            try {
                // Garante que a pasta existe
                ensureTeacherImgFolderExists();
                
                FileImage fileImage = new FileImage();
                Assistant assistant = new Assistant();
                String newNameFile = "0" + assistant.novoNome(photo.getContentType());
                fileImage.salvarArquivo(photo, TEACHER_IMG_FOLDER, newNameFile);

                teacher.setPhotoPhath(newNameFile);
                teacher.setUpdatedAt(LocalDateTime.now());
                repository.save(teacher);
                System.out.println("Foto do professor atualizada com sucesso: " + newNameFile);
            } catch (IOException e) {
                String errorMsg = "Erro ao atualizar imagem do professor: " + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();
                throw new IOException(errorMsg, e);
            } catch (Exception e) {
                String errorMsg = "Erro inesperado ao atualizar imagem do professor: " + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();
                throw new IOException(errorMsg, e);
            }
        }
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    // ---------------------
    // MÉTODO PARA LAZY LOADING (com filtros)
    // ---------------------

    public Page<TeacherDTO> findLazy(int page, int size, Sort sort,
            String teacherNumber, String name, String status) {

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TeacherTableProjection> projections = repository.findAllForTable(
                pageable, blankToNull(teacherNumber), blankToNull(name), blankToNull(status));

        return projections.map(p -> new TeacherDTO(
                p.getPkTeacher(),
                p.getTeacherNumber(),
                p.getFristName(),
                p.getLastName(),
                p.getQualificationLivel() != null ? QualificationLevel.valueOf(p.getQualificationLivel()) : null,
                p.getContractType() != null ? ContractType.valueOf(p.getContractType()) : null,
                p.getStatus() != null ? TeacherStatus.valueOf(p.getStatus()) : null,
                p.getPhotoPhath(),
                p.getEmail(),
                p.getPhone(),
                p.getCreatedAt()));
    }

    public long countFiltered(String teacherNumber, String name, String status) {
        return repository.countFiltered(blankToNull(teacherNumber), blankToNull(name), blankToNull(status));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    // ---------------------
    // ESTATÍSTICAS DO CABEÇALHO
    // ---------------------

    public long countTotal() {
        return repository.count();
    }

    public long countActive() {
        return repository.countByStatus(TeacherStatus.ACTIVE);
    }

    public long countNewThisMonth() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        return repository.countByCreatedAtBetween(start, end);
    }

    public BigDecimal sumBaseSalary() {
        BigDecimal total = repository.sumBaseSalary();
        return total != null ? total : BigDecimal.ZERO;
    }

    // ---------------------
    // GERAÇÃO AUTOMÁTICA DO teacherNumber (PROF-ANO-SEQUENCIA)
    // ---------------------

    /**
     * Gera o próximo número de professor no formato PROF-ANO-SEQUENCIA
     * (ex: PROF-2026-00001).
     *
     * A sequência é reiniciada a cada ano. O bloqueio pessimista (FOR UPDATE)
     * aplicado na consulta do repositório evita que duas gravações
     * concorrentes gerem o mesmo número.
     */
    public String generateTeacherNumber() {

        int currentYear = Year.now().getValue();
        String prefix = TEACHER_PREFIX + "-" + currentYear + "-";

        String lastNumber = repository.findLastTeacherNumberForUpdate(prefix);

        int nextSequence = 1;
        if (lastNumber != null) {
            String sequencePart = lastNumber.substring(prefix.length());
            try {
                nextSequence = Integer.parseInt(sequencePart) + 1;
            } catch (NumberFormatException e) {
                nextSequence = 1;
            }
        }

        String sequenceFormatted = String.format("%0" + SEQUENCE_LENGTH + "d", nextSequence);
        return prefix + sequenceFormatted;
    }

    // ---------------------
    // EXPORTAÇÃO PDF DA LISTA
    // ---------------------

    /**
     * Gera um PDF simples com a lista de professores (respeitando os
     * mesmos filtros usados na tabela) e envia-o diretamente na resposta
     * HTTP corrente, terminando o ciclo de vida do JSF.
     */
    public void exportTeacherListPdf(String teacherNumber, String name, String status) throws IOException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"lista_professores.pdf\"");

        Document document = new Document(PageSize.A4.rotate(), 24, 24, 30, 30);

        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Lista de Professores", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(14);
            document.add(title);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 2f, 3f, 2.5f, 2.5f, 2f, 2.5f });

            Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, java.awt.Color.WHITE);
            for (String header : new String[] { "Nº Professor", "Nome", "Qualificação", "Vínculo", "Estado", "Email" }) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
                cell.setBackgroundColor(new java.awt.Color(20, 20, 20));
                cell.setPadding(6);
                table.addCell(cell);
            }

            Font bodyFont = new Font(Font.HELVETICA, 8);
            Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
            Page<TeacherDTO> page = findLazy(0, Integer.MAX_VALUE, sort, teacherNumber, name, status);

            for (TeacherDTO t : page.getContent()) {
                table.addCell(new Paragraph(t.getTeacherNumber(), bodyFont));
                table.addCell(new Paragraph(t.getFristName() + " " + t.getLastName(), bodyFont));
                table.addCell(new Paragraph(
                        t.getQualificationLivel() != null ? t.getQualificationLivel().getDescricao() : "-", bodyFont));
                table.addCell(new Paragraph(
                        t.getContractType() != null ? t.getContractType().getDescricao() : "-", bodyFont));
                table.addCell(new Paragraph(
                        t.getStatus() != null ? t.getStatus().getDescricao() : "-", bodyFont));
                table.addCell(new Paragraph(t.getEmail() != null ? t.getEmail() : "-", bodyFont));
            }

            document.add(table);

        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar o PDF da lista de professores", e);
        } finally {
            document.close();
        }

        facesContext.responseComplete();
    }
}