package com.SistemSchool.modulo_Recursoa_Humano.service;

import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_Recursoa_Humano.dto.TeacherDTO;
import com.SistemSchool.modulo_Recursoa_Humano.interfaces.TeacherTableProjection;
import com.SistemSchool.modulo_Recursoa_Humano.io.TeacherStatus;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;
import com.SistemSchool.modulo_Recursoa_Humano.repository.TeacherRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.time.Year;
import java.util.Optional;

@Service
@Transactional
public class TeacherService {

    private final TeacherRepository repository;
    private static final String TEACHER_NUMBER_PREFIX = "PROF-";

    public TeacherService(TeacherRepository teacherRepository) {
        this.repository = teacherRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // GERAÇÃO DE NÚMERO AUTOMÁTICO
    // ─────────────────────────────────────────────────────────────

    /**
     * Gera o próximo número de professor no formato PROF-<ano>-<sequencia>,
     * ex: PROF-2026-0001, PROF-2026-0002, ...
     * A sequência reinicia a cada ano.
     */
    public String generateTeacherNumber() {
        int currentYear = Year.now().getValue();
        String prefix = TEACHER_NUMBER_PREFIX + currentYear + "-";

        Optional<Teacher> lastTeacher = repository
                .findTopByTeacherNumberStartingWithOrderByTeacherNumberDesc(prefix);

        int nextSequence = 1;

        if (lastTeacher.isPresent()) {
            String lastNumber = lastTeacher.get().getTeacherNumber();
            String sequencePart = lastNumber.substring(prefix.length());
            try {
                nextSequence = Integer.parseInt(sequencePart) + 1;
            } catch (NumberFormatException e) {
                // Se por algum motivo o formato estiver corrompido, cai para 1
                nextSequence = 1;
            }
        }

        return prefix + String.format("%04d", nextSequence);
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Teacher save(Teacher teacher) {
        if (repository.existsByTeacherNumber(teacher.getTeacherNumber())) {
            throw new RuntimeException("Número de professor já existe: " + teacher.getTeacherNumber());
        }
        if (repository.existsByBiNumber(teacher.getBiNumber())) {
            throw new RuntimeException("Número de BI já registado: " + teacher.getBiNumber());
        }
        if (teacher.getEmail() != null && repository.existsByEmail(teacher.getEmail())) {
            throw new RuntimeException("Email já registado: " + teacher.getEmail());
        }
        return repository.save(teacher);
    }

    public void update(TeacherDTO dto) {
        Teacher teacher = repository.findById(dto.getPkTeacher())
                .orElseThrow(() -> new RuntimeException("Professor não encontrado com id: " + dto.getPkTeacher()));

        teacher.setTeacherNumber(dto.getTeacherNumber());
        teacher.setFristName(dto.getFristName());
        teacher.setLastName(dto.getLastName());
        teacher.setGender(dto.getGender());
        teacher.setQualificationLivel(dto.getQualificationLivel());
        teacher.setContractType(dto.getContractType());
        teacher.setStatus(dto.getStatus());
        teacher.setPhotoPhath(dto.getPhotoPhath());
        teacher.setBiNumber(dto.getBiNumber());
        teacher.setBiExpiryDate(dto.getBiExpiryDate());
        teacher.setAddressStreet(dto.getAddressStreet());
        teacher.setAddressProvice(dto.getAddressProvice());
        teacher.setBaseSalary(dto.getBaseSalary());
        teacher.setEmail(dto.getEmail());
        teacher.setPhone(dto.getPhone());
        teacher.setMobilePhone(dto.getMobilePhone());
        teacher.setObs(dto.getObs());

        repository.save(teacher);
    }

    public void delete(Long id) {
        try {
            Teacher teacher = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada"));
            repository.delete(teacher);
            repository.flush(); // força o erro aqui, dentro do try
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Não é possível eliminar o Professor.");
        }
    }
    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<TeacherDTO> getAllTeachers() {
        return repository.findAllTeachersDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA
    // ─────────────────────────────────────────────────────────────

    public Page<TeacherDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TeacherTableProjection> projections = repository.findAllForTable(pageable);

        return projections.map(p -> new TeacherDTO(
                p.getPkTeacher(),
                p.getTeacherNumber(),
                p.getFristName(),
                p.getLastName(),
                null,
                null,
                null,
                null,
                p.getPhotoPhath(),
                p.getBiNumber(),
                p.getBiExpiryDate(),
                p.getAddressStreet(),
                p.getAddressProvice(),
                p.getBaseSalary(),
                p.getEmail(),
                p.getPhone(),
                p.getMobilePhone(),
                p.getObs(),
                p.getCreatedAt(),
                p.getUpdatedAt()));
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    public long countAll() {
        return repository.count();
    }

    public long countByStatus(TeacherStatus status) {
        return repository.countByStatus(status);
    }

    public List<Teacher> getByStatus(TeacherStatus status) {
        return repository.findByStatus(status);
    }

    public List<Teacher> getByGender(Gender gender) {
        return repository.findByGender(gender);
    }

    public List<Teacher> getByProvincia(String provincia) {
        return repository.findByAddressProvice(provincia);
    }

    public Teacher getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado com id: " + id));
    }

    public Teacher findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado com id: " + id));
    }
}