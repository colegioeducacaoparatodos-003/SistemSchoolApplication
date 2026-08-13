package com.SistemSchool.modulo_secrtaria.service;

import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_secrtaria.io.StudentStatus;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.StudentRepository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        List<StudentDTO> all = getAllStudents();
        List<StudentDTO> filtered = applyFilters(all, filters);

        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());

        List<StudentDTO> pageContent = filtered.subList(fromIndex, toIndex);
        return new PageImpl<>(pageContent, PageRequest.of(page, size, sort), filtered.size());
    }

    private List<StudentDTO> applyFilters(List<StudentDTO> source, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return source;
        }

        List<StudentDTO> filtered = new ArrayList<>();
        String globalTerm = normalize(filters.get("global"));

        for (StudentDTO dto : source) {
            boolean matches = true;

            if (!globalTerm.isEmpty()) {
                matches = matchesGlobal(dto, globalTerm);
            }

            if (matches) {
                for (Map.Entry<String, Object> entry : filters.entrySet()) {
                    String field = entry.getKey();
                    if ("global".equals(field)) {
                        continue;
                    }
                    String expected = normalize(entry.getValue());
                    if (expected.isEmpty()) {
                        continue;
                    }

                    boolean fieldMatches = switch (field) {
                        case "sudentNumber" -> contains(dto.getSudentNumber(), expected);
                        case "fullName" -> contains(dto.getFullName(), expected);
                        case "gender" -> contains(dto.getGender() != null ? dto.getGender().name() : null, expected);
                        case "status" -> contains(dto.getStatus() != null ? dto.getStatus().name() : null, expected);
                        case "addressProvice" -> contains(dto.getAddressProvice(), expected);
                        case "biNumber" -> contains(dto.getBiNumber(), expected);
                        case "email" -> contains(dto.getEmail(), expected);
                        case "phone_1" -> contains(dto.getPhone_1(), expected);
                        default -> true;
                    };

                    if (!fieldMatches) {
                        matches = false;
                        break;
                    }
                }
            }

            if (matches) {
                filtered.add(dto);
            }
        }

        return filtered;
    }

    private boolean matchesGlobal(StudentDTO dto, String globalTerm) {
        return contains(dto.getSudentNumber(), globalTerm)
                || contains(dto.getFullName(), globalTerm)
                || contains(dto.getBiNumber(), globalTerm)
                || contains(dto.getEmail(), globalTerm)
                || contains(dto.getPhone_1(), globalTerm)
                || contains(dto.getAddressProvice(), globalTerm)
                || contains(dto.getGender() != null ? dto.getGender().name() : null, globalTerm)
                || contains(dto.getStatus() != null ? dto.getStatus().name() : null, globalTerm);
    }

    private boolean contains(String value, String expected) {
        return value != null && value.toLowerCase().contains(expected.toLowerCase());
    }

    private String normalize(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    // ---------------------------------------------------------------
    // CONTAGEM
    // ---------------------------------------------------------------

    public long count() {
        return repository.count();
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