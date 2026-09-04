package com.SistemSchool.modulo_secrtaria.service;

import com.SistemSchool.io.FileImage;
import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.modulo_secrtaria.interfaces.StudentTableProjection;
import com.SistemSchool.modulo_secrtaria.io.StudentStatus;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.StudentRepository;
import com.SistemSchool.util.BIValidator;

import jakarta.transaction.Transactional;

import org.primefaces.model.file.UploadedFile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.SistemSchool.io.Gender;

import java.time.LocalDate;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StudentService {

    /**
     * Pasta (dentro da aplicação web) onde as fotos dos alunos são guardadas.
     * IMPORTANTE: tem de coincidir com
     * {@code PdfReportService.STUDENT_PHOTO_BASE_PATH} ("/student_img/"),
     * pois é de lá que os relatórios em PDF (ficha de matrícula, cartão A6)
     * carregam a foto do aluno via {@code ExternalContext.getRealPath(...)}.
     */
    private static final String PASTA_FOTOS_ALUNOS = "student_img";

    /** Prefixo usado na geração automática do número interno do aluno (ex: ALU-2026-0001) */
    private static final String PREFIXO_NUMERO_ALUNO = "ALU-";

    private final StudentRepository repository;
    private final FileImage fileImage;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
        this.fileImage = new FileImage();
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Student save(Student student, UploadedFile foto) {
        validarObrigatorios(student);

        if (student.getSudentNumber() == null || student.getSudentNumber().isBlank()) {
            student.setSudentNumber(gerarNumeroAluno());
        }

        if (repository.existsBySudentNumber(student.getSudentNumber())) {
            throw new RuntimeException("Já existe um aluno com este número (" + student.getSudentNumber() + ").");
        }
        if (student.getBiNumber() != null && !student.getBiNumber().isBlank()
                && repository.existsByBiNumber(student.getBiNumber())) {
            throw new RuntimeException("Já existe um aluno com este número de BI.");
        }

        validarBI(student.getBiNumber());

        if (temFicheiro(foto)) {
            String nomeFicheiro = guardarFoto(foto, student.getSudentNumber());
            student.setUploadPhoto(nomeFicheiro);
        }

        return repository.save(student);
    }

    public void update(StudentDTO dto, UploadedFile novaFoto) {
        Student student = repository.findById(dto.getPkStudent())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com id: " + dto.getPkStudent()));

        if (dto.getSudentNumber() != null
                && repository.existsBySudentNumberAndPkStudentNot(dto.getSudentNumber(), student.getPkStudent())) {
            throw new RuntimeException("Já existe um aluno com este número (" + dto.getSudentNumber() + ").");
        }
        if (dto.getBiNumber() != null && !dto.getBiNumber().isBlank()
                && repository.existsByBiNumberAndPkStudentNot(dto.getBiNumber(), student.getPkStudent())) {
            throw new RuntimeException("Já existe um aluno com este número de BI.");
        }

        validarBI(dto.getBiNumber());

        student.setSudentNumber(dto.getSudentNumber());
        student.setFristName(dto.getFristName());
        student.setLastName(dto.getLastName());
        student.setFullName(dto.getFullName());
        student.setGender(dto.getGender());
        student.setBiNumber(dto.getBiNumber());
        student.setNascDate(dto.getNascDate());
        student.setBiExpiryData(dto.getBiExpiryData());
        student.setAddressStreet(dto.getAddressStreet());
        student.setAddressProvice(dto.getAddressProvice());
        student.setNameFather(dto.getNameFather());
        student.setNameMather(dto.getNameMather());
        student.setEmail(dto.getEmail());
        student.setPhone_1(dto.getPhone_1());
        student.setPhone_2(dto.getPhone_2());
        student.setStatus(dto.getStatus());
        student.setObs(dto.getObs());

        if (temFicheiro(novaFoto)) {
            removerFotoAtual(student.getUploadPhoto());
            String nomeFicheiro = guardarFoto(novaFoto, student.getSudentNumber());
            student.setUploadPhoto(nomeFicheiro);
        }

        repository.save(student);
    }

    public void delete(Long id) {
        try {
            Student student = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado com id: " + id));
            removerFotoAtual(student.getUploadPhoto());
            repository.delete(student);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Não é possível eliminar este aluno: existem registos associados (matrículas, notas, etc.).");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<StudentDTO> getAllStudents() {
        return repository.findAllStudentsDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA
    // ─────────────────────────────────────────────────────────────

    public Page<StudentDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StudentTableProjection> projections = repository.findAllForTable(pageable);

        return projections.map(p -> new StudentDTO(
                p.getPkStudent(),
                p.getSudentNumber(),
                p.getFristName(),
                p.getLastName(),
                p.getFullName(),
                p.getGender() != null ? com.SistemSchool.io.Gender.valueOf(p.getGender()) : null,
                p.getBiNumber(),
                p.getNascDate(),
                null,
                null,
                null,
                null,
                null,
                p.getEmail(),
                p.getPhone_1(),
                null,
                p.getUploadPhoto(),
                p.getStatus() != null ? StudentStatus.valueOf(p.getStatus()) : null,
                null,
                p.getCreatedAt(),
                p.getUpdatedAt()));
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    public List<Student> getByStatus(StudentStatus status) {
        return repository.findByStatus(status);
    }

    public List<Student> getByFullName(String fullName) {
        return repository.findByFullNameContainingIgnoreCase(fullName);
    }

    public Student getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com id: " + id));
    }

    public Student findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com id: " + id));
    }

    // ─────────────────────────────────────────────────────────────
    // GESTÃO DA FOTO (usa a classe FileImage)
    // ─────────────────────────────────────────────────────────────

    private boolean temFicheiro(UploadedFile foto) {
        return foto != null && foto.getContent() != null && foto.getContent().length > 0;
    }

    private String guardarFoto(UploadedFile foto, String sudentNumber) {
        try {
            String extensao = extrairExtensao(foto.getFileName());
            String base = (sudentNumber != null && !sudentNumber.isBlank()) ? sudentNumber : "aluno";
            String novoNome = base + "_" + System.currentTimeMillis() + extensao;

            fileImage.salvarArquivoSemMudarONome(foto, PASTA_FOTOS_ALUNOS, novoNome);

            return novoNome;
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível guardar a foto do aluno: " + e.getMessage(), e);
        }
    }

    private void removerFotoAtual(String nomeFicheiroAtual) {
        if (nomeFicheiroAtual == null || nomeFicheiroAtual.isBlank()) {
            return;
        }
        try {
            fileImage.eliminarFicheiro(nomeFicheiroAtual, PASTA_FOTOS_ALUNOS);
        } catch (SQLException e) {
            // Falha ao remover a foto antiga não deve impedir a operação principal;
            // fica apenas registada.
            System.out.println("Aviso: não foi possível eliminar a foto antiga (" + nomeFicheiroAtual + "): "
                    + e.getMessage());
        }
    }

    private String extrairExtensao(String nomeOriginal) {
        if (nomeOriginal == null || !nomeOriginal.contains(".")) {
            return "";
        }
        return nomeOriginal.substring(nomeOriginal.lastIndexOf('.'));
    }

    private void validarObrigatorios(Student student) {
        if (student.getFristName() == null || student.getFristName().isBlank()) {
            throw new RuntimeException("O primeiro nome do aluno é obrigatório.");
        }
        if (student.getLastName() == null || student.getLastName().isBlank()) {
            throw new RuntimeException("O último nome do aluno é obrigatório.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 1) VALIDAÇÃO DO BI (usa a classe utilitária BIValidator)
    // ─────────────────────────────────────────────────────────────

    /**
     * Valida o número de BI usando {@link BIValidator}. O BI é opcional no
     * cadastro, por isso só valida quando algum valor foi indicado.
     */
    public void validarBI(String biNumber) {
        if (biNumber == null || biNumber.isBlank()) {
            return;
        }
        if (!BIValidator.isValid(biNumber)) {
            throw new RuntimeException(
                    "Número de BI inválido. Formato esperado: 9 dígitos + 2 letras + 3 dígitos (ex: 123456789LA041).");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 2) PREENCHIMENTO AUTOMÁTICO DO NÚMERO DE ALUNO
    //    Formato: ALU-<ano>-<sequência com 4 dígitos>  (ex: ALU-2026-0001)
    //    A sequência reinicia a cada ano civil.
    // ─────────────────────────────────────────────────────────────

    /**
     * Gera o próximo número interno do aluno no formato "ALU-2026-0001",
     * garantindo unicidade mesmo que existam números atribuídos manualmente
     * fora da sequência. A sequência é reiniciada a cada ano: o cálculo
     * conta apenas os alunos cujo número já começa com "ALU-{anoAtual}-".
     */
    public String gerarNumeroAluno() {
        String ano = String.valueOf(Year.now().getValue());
        String prefixoAno = PREFIXO_NUMERO_ALUNO + ano + "-";

        long proximo = repository.countBySudentNumberStartingWith(prefixoAno) + 1;
        String numero;
        do {
            numero = prefixoAno + String.format("%04d", proximo);
            proximo++;
        } while (repository.existsBySudentNumber(numero));
        return numero;
    }

    // ── adiciona estes métodos à classe StudentService ──

public Page<StudentDTO> findLazyWithFilters(int page, int size, Sort sort,
        String searchText, StudentStatus status, Gender gender,
        LocalDate birthDateFrom, LocalDate birthDateTo, String studentName) {

    Pageable pageable = PageRequest.of(page, size, sort);
    Page<Student> entities = repository.findLazyWithFilters(
            searchText, status, gender, birthDateFrom, birthDateTo, studentName, pageable);

    return entities.map(this::toDto);
}

public long countWithFilters(String searchText, StudentStatus status, Gender gender,
        LocalDate birthDateFrom, LocalDate birthDateTo, String studentName) {
    return repository.countWithFilters(searchText, status, gender, birthDateFrom, birthDateTo, studentName);
}

private StudentDTO toDto(Student s) {
    return new StudentDTO(
            s.getPkStudent(),
            s.getSudentNumber(),
            s.getFristName(),
            s.getLastName(),
            s.getFullName(),
            s.getGender(),
            s.getBiNumber(),
            s.getNascDate(),
            s.getBiExpiryData(),
            s.getAddressStreet(),
            s.getAddressProvice(),
            s.getNameFather(),
            s.getNameMather(),
            s.getEmail(),
            s.getPhone_1(),
            s.getPhone_2(),
            s.getUploadPhoto(),
            s.getStatus(),
            s.getObs(),
            s.getCreatedAt(),
            s.getUpdatedAt()
    );
}
    // ─────────────────────────────────────────────────────────────
    // CONTAGENS PARA OS CARTÕES DE ESTATÍSTICA
    // ─────────────────────────────────────────────────────────────

    public long getTotalStudentsCount() {
        return repository.count();
    }

    public long getActiveStudentsCount() {
        return repository.countByStatus(StudentStatus.ACTIVE);
    }

    public long getGraduatedStudentsCount() {
        return repository.countByStatus(StudentStatus.GRADUATED);
    }

    public long getNewStudentsThisMonthCount() {
        LocalDateTime inicioDoMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return repository.countCreatedAfter(inicioDoMes);
    }
}