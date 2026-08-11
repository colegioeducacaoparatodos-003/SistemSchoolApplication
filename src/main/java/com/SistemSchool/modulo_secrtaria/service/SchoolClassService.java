package com.SistemSchool.modulo_secrtaria.service;

import com.SistemSchool.modulo_secrtaria.dto.SchoolClassDTO;
import com.SistemSchool.modulo_secrtaria.interfaces.SchoolClassTableProjection;
import com.SistemSchool.modulo_secrtaria.io.Classe;
import com.SistemSchool.modulo_secrtaria.io.SchoolClaassStatus;
import com.SistemSchool.modulo_secrtaria.io.ShiftType;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SchoolClassService {

    private final SchoolClassRepository repository;

    public SchoolClassService(SchoolClassRepository repository) {
        this.repository = repository;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public SchoolClass save(SchoolClass schoolClass) {
        if (repository.existsByClassCode(schoolClass.getClassCode())) {
            throw new RuntimeException("Código de turma já existe: " + schoolClass.getClassCode());
        }
        return repository.save(schoolClass);
    }

    public void update(SchoolClassDTO dto) {
        SchoolClass schoolClass = repository.findById(dto.getPkSchoolClass())
                .orElseThrow(() -> new RuntimeException("Turma não encontrada com id: " + dto.getPkSchoolClass()));

        schoolClass.setClassCode(dto.getClassCode());
        schoolClass.setClassName(dto.getClassName());
        schoolClass.setClasse(dto.getClasse());
        schoolClass.setTurno(dto.getTurno());
        schoolClass.setAnoLectivo(dto.getAnoLectivo());
        schoolClass.setCapacidade(dto.getCapacidade());
        schoolClass.setRoom(dto.getRoom());
        schoolClass.setStatus(dto.getStatus());
        schoolClass.setObs(dto.getObs());
        schoolClass.onUpdate();

        repository.save(schoolClass);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Turma não encontrada com id: " + id);
        }
        repository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<SchoolClassDTO> getAllSchoolClasses() {
        return repository.findAllSchoolClassesDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA (com filtros)
    // ─────────────────────────────────────────────────────────────

    public Page<SchoolClassDTO> findLazyWithFilters(int page, int size, Sort sort,
            String searchText, Classe classe, ShiftType turno,
            SchoolClaassStatus status, String anoLectivo) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAllWithFilters(searchText, classe, turno, status, anoLectivo, pageable);
    }

    // Método legado para compatibilidade
    public Page<SchoolClassDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SchoolClassTableProjection> projections = repository.findAllForTable(pageable);
        return projections.map(p -> new SchoolClassDTO(
                p.getPkSchoolClass(),
                p.getClassCode(),
                p.getClassName(),
                p.getClasse() != null ? Classe.valueOf(p.getClasse()) : null,
                p.getTurno() != null ? ShiftType.valueOf(p.getTurno()) : null,
                p.getAnoLectivo(),
                p.getCapacidade(),
                p.getRoom(),
                p.getStatus() != null ? SchoolClaassStatus.valueOf(p.getStatus()) : null,
                p.getObs(),
                p.getCreatedAt(),
                p.getUpdatedAt()));
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    public List<SchoolClass> getByClasse(Classe classe) {
        return repository.findByClasse(classe);
    }

    public List<SchoolClass> getByTurno(ShiftType turno) {
        return repository.findByTurno(turno);
    }

    public List<SchoolClass> getByStatus(SchoolClaassStatus status) {
        return repository.findByStatus(status);
    }

    public List<SchoolClass> getByAnoLectivo(String anoLectivo) {
        return repository.findByAnoLectivo(anoLectivo);
    }

    public SchoolClass getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada com id: " + id));
    }

    public SchoolClass findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada com id: " + id));
    }
}