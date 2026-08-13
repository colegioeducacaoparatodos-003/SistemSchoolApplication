package com.SistemSchool.modulo_secrtaria.service;

import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_secrtaria.interfaces.StudentTableProjection;
import com.SistemSchool.modulo_secrtaria.io.StudentStatus;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.StudentRepository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository studentRepository) {
        this.repository = studentRepository;
    }

    public String generateNextStudentNumber() {
        int year = java.time.Year.now().getValue();
        String prefix = "ALU-" + year + "-";

        Optional<String> lastOpt = repository.findLastStudentNumberByPrefix(prefix + "%");

        int nextSeq = 1;
        if (lastOpt.isPresent()) {
            String seqPart = lastOpt.get().substring(prefix.length());
            try {
                nextSeq = Integer.parseInt(seqPart) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1; // fallback se o formato mudar
            }
        }
        return prefix + String.format("%04d", nextSeq);
    }
    // ---------------------------------------------------------------
    // CRUD PRINCIPAL
    // ---------------------------------------------------------------

    public Student save(Student student) {
        if (repository.existsBySudentNumber(student.getSudentNumber())) {
            throw new RuntimeException("Numero de aluno ja existe: " + student.getSudentNumber());
        }
        if (repository.existsByBiNumber(student.getBiNumber())) {
            throw new RuntimeException("Numero de BI ja registado: " + student.getBiNumber());
        }
        if (student.getEmail() != null && repository.existsByEmail(student.getEmail())) {
            throw new RuntimeException("Email ja registado: " + student.getEmail());
        }
        return repository.save(student);
    }

    public void update(StudentDTO dto) {
        Student student = repository.findById(dto.getPkStudent())
                .orElseThrow(() -> new RuntimeException("Aluno nao encontrado com id: " + dto.getPkStudent()));

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
        student.setUploadPhoto(dto.getUploadPhoto());
        student.setStatus(dto.getStatus());
        student.setObs(dto.getObs());

        repository.save(student);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Aluno nao encontrado com id: " + id);
        }
        repository.deleteById(id);
    }

    // ---------------------------------------------------------------
    // BUSCAR TODOS (lista completa com DTO)
    // ---------------------------------------------------------------

    public List<StudentDTO> getAllStudents() {
        return repository.findAllStudentsDTO();
    }

    // ---------------------------------------------------------------
    // LAZY LOADING PARA TABELA
    // ---------------------------------------------------------------

    public Page<StudentDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StudentTableProjection> projections = repository.findAllForTable(pageable);

        return projections.map(p -> new StudentDTO(
                p.getPkStudent(),
                p.getSudentNumber(),
                p.getFristName(),
                p.getLastName(),
                p.getFullName(),
                null,
                p.getBiNumber(),
                p.getNascDate(),
                p.getBiExpiryData(),
                p.getAddressStreet(),
                p.getAddressProvice(),
                p.getNameFather(),
                p.getNameMather(),
                p.getEmail(),
                p.getPhone_1(),
                p.getPhone_2(),
                p.getUploadPhoto(),
                null,
                p.getObs(),
                p.getCreatedAt(),
                p.getUpdatedAt()));
    }

    // ---------------------------------------------------------------
    // QUERIES UTILITARIAS
    // ---------------------------------------------------------------

    public long countAll() {
        return repository.count();
    }

    public long countByStatus(StudentStatus status) {
        return repository.countByStatus(status);
    }

    public List<Student> getByStatus(StudentStatus status) {
        return repository.findByStatus(status);
    }

    public List<Student> getByGender(Gender gender) {
        return repository.findByGender(gender);
    }

    public List<Student> getByProvincia(String provincia) {
        return repository.findByAddressProvice(provincia);
    }

    public Student getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno nao encontrado com id: " + id));
    }

    public Student findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudante nao encontrado com id: " + id));
    }
}