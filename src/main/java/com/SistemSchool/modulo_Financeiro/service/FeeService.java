package com.SistemSchool.modulo_Financeiro.service;

import com.SistemSchool.modulo_Financeiro.dto.FeeDTO;
import com.SistemSchool.modulo_Financeiro.interfaces.FeeTableProjection;
import com.SistemSchool.modulo_Financeiro.io.FeeStatus;
import com.SistemSchool.modulo_Financeiro.io.FeeType;
import com.SistemSchool.modulo_Financeiro.model.Fee;
import com.SistemSchool.modulo_Financeiro.repository.FeeRepository;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class FeeService {

    // Formato: PRO-{ano}-{sequência com 4 dígitos}, ex: PRO-2026-0001
    private static final String FEE_CODE_PREFIX = "PRO";

    private final FeeRepository repository;
    private final SchoolClassRepository schoolClassRepository;

    public FeeService(FeeRepository repository,
            SchoolClassRepository schoolClassRepository) {
        this.repository = repository;
        this.schoolClassRepository = schoolClassRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────
    public Fee save(Fee fee) {
        if (repository.existsByFeeCode(fee.getFeeCode())) {
            throw new RuntimeException("Código de propina já existe: " + fee.getFeeCode());
        }
        if (fee.getSchoolClass() == null || fee.getSchoolClass().getPkSchoolClass() == null) {
            throw new RuntimeException("É necessário indicar a turma para a taxa.");
        }
        if (fee.getFeeType() == null) {
            throw new RuntimeException("É necessário indicar o tipo de taxa.");
        }

        Long schoolClassPk = fee.getSchoolClass().getPkSchoolClass();
        SchoolClass schoolClass = schoolClassRepository.findById(schoolClassPk)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada com id: " + schoolClassPk));

        if (repository.existsBySchoolClass_PkSchoolClassAndFeeTypeAndSchoolYear(
                schoolClassPk, fee.getFeeType(), fee.getSchoolYear())) {
            throw new RuntimeException(
                    "Já existe uma taxa do tipo " + fee.getFeeType() + " para esta turma neste ano letivo.");
        }

        fee.setSchoolClass(schoolClass);
        return repository.save(fee);
    }

    public void update(FeeDTO dto) {
        Fee fee = repository.findById(dto.getPhFee())
                .orElseThrow(() -> new RuntimeException("Propina não encontrada com id: " + dto.getPhFee()));

        if (dto.getSchoolClassPk() != null
                && (fee.getSchoolClass() == null
                        || !dto.getSchoolClassPk().equals(fee.getSchoolClass().getPkSchoolClass()))) {
            SchoolClass schoolClass = schoolClassRepository.findById(dto.getSchoolClassPk())
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada com id: " + dto.getSchoolClassPk()));
            fee.setSchoolClass(schoolClass);
        }

        fee.setFeeCode(dto.getFeeCode());
        fee.setDescription(dto.getDescription());
        fee.setSchoolYear(dto.getSchoolYear());
        fee.setAmount(dto.getAmount());
        fee.setStartDate(dto.getStartDate());
        fee.setEndDate(dto.getEndDate());
        fee.setStatus(dto.getStatus());
        fee.setObs(dto.getObs());
        fee.setUpdatedAt(LocalDateTime.now());

        repository.save(fee);
    }

    /**
     * Elimina uma propina.
     */
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Propina não encontrada com id: " + id);
        }
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException(
                    "Não é possível eliminar esta propina porque já existem registros associados a ela (ex: faturas). "
                            + "Elimine ou reatribua os registros vinculados antes de tentar novamente.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GERAÇÃO AUTOMÁTICA DO FEE CODE
    // ─────────────────────────────────────────────────────────────

    /**
     * Gera o próximo feeCode disponível para o ano letivo informado, no
     * formato PRO-{ano}-{sequência com 4 dígitos}, ex: PRO-2026-0001.
     * Se schoolYear vier nulo, usa o ano corrente como fallback.
     */
    @Transactional(readOnly = true)
    public String generateNextFeeCode(Integer schoolYear) {
        int year = schoolYear != null ? schoolYear : java.time.Year.now().getValue();
        String prefix = FEE_CODE_PREFIX + "-" + year + "-";

        Optional<String> lastOpt = repository.findLastFeeCodeByPrefix(prefix + "%");

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

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FeeDTO> getAllFees() {
        return repository.findAllFeesDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA
    // ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<FeeDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<FeeTableProjection> projections;
        if (filters == null || filters.isEmpty()) {
            projections = repository.findAllForTable(pageable);
        } else {
            projections = repository.findAllForTable(pageable);
        }

        return projections.map(p -> new FeeDTO(
                p.getPhFee(),
                p.getFeeCode(),
                p.getDescription(),
                p.getFeeType() != null ? FeeType.valueOf(p.getFeeType()) : null,
                p.getSchoolClassPk(),
                p.getSchoolClassName(),
                p.getSchoolYear(),
                p.getAmount(),
                p.getStartDate(),
                p.getEndDate(),
                p.getStatus() != null ? FeeStatus.valueOf(p.getStatus()) : null,
                p.getObs(),
                p.getCreatedAt(),
                p.getUpdatedAt()));
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Fee> getByStatus(FeeStatus status) {
        return repository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Fee> getBySchoolYear(Integer schoolYear) {
        return repository.findBySchoolYear(schoolYear);
    }

    @Transactional(readOnly = true)
    public List<Fee> getByEndDate(LocalDateTime endDate) {
        return repository.findByEndDate(endDate);
    }

    @Transactional(readOnly = true)
    public List<Fee> getBySchoolClass(Long schoolClassPk) {
        return repository.findBySchoolClass_PkSchoolClass(schoolClassPk);
    }

    @Transactional(readOnly = true)
    public Fee getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Propina não encontrada com id: " + id));
    }
}