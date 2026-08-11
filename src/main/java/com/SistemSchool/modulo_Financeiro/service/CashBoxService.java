package com.SistemSchool.modulo_Financeiro.service;

import com.SistemSchool.modulo_Financeiro.dto.CashBoxDTO;
import com.SistemSchool.modulo_Financeiro.interfaces.CashBoxTableProjection;
import com.SistemSchool.modulo_Financeiro.io.CashBoxStatus;
import com.SistemSchool.modulo_Financeiro.model.CashBox;
import com.SistemSchool.modulo_Financeiro.repository.CashBoxRepository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Transactional
public class CashBoxService {

    private static final Logger LOGGER = Logger.getLogger(CashBoxService.class.getName());

    private final CashBoxRepository repository;

    public CashBoxService(CashBoxRepository repository) {
        this.repository = repository;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public CashBox save(CashBox cashBox) {
        if (cashBox.getCashBoxNumber() == null || cashBox.getCashBoxNumber().isBlank()) {
            cashBox.setCashBoxNumber(generateNextCashBoxNumber());
        }

        if (repository.existsByCashBoxNumber(cashBox.getCashBoxNumber())) {
            throw new RuntimeException("Número de caixa já existe: " + cashBox.getCashBoxNumber());
        }

        if (repository.existsByStatus(CashBoxStatus.OPEN)) {
            throw new RuntimeException("Já existe um caixa aberto. Feche-o antes de abrir um novo.");
        }

        return repository.save(cashBox);
    }

    public void update(CashBoxDTO dto) {
        CashBox cashBox = repository.findById(dto.getPhCashBox())
                .orElseThrow(() -> new RuntimeException("Caixa não encontrado com id: " + dto.getPhCashBox()));

        cashBox.setCashBoxNumber(dto.getCashBoxNumber());
        cashBox.setOpeningDate(dto.getOpeningDate());
        cashBox.setClosingDate(dto.getClosingDate());
        cashBox.setOpeningBalance(dto.getOpeningBalance());
        cashBox.setClosingBalance(dto.getClosingBalance());
        cashBox.setOperator(dto.getOperator());
        cashBox.setStatus(dto.getStatus());
        cashBox.setObservation(dto.getObservation());

        repository.save(cashBox);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Caixa não encontrado com id: " + id);
        }
        repository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────
    // GERAÇÃO AUTOMÁTICA DE NÚMERO
    // ─────────────────────────────────────────────────────────────

    private String generateNextCashBoxNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "CX-" + year + "-";

        String lastNumber = repository.findLastCashBoxNumberOfCurrentYear();

        int nextSequence = 1;
        if (lastNumber != null && !lastNumber.isBlank()) {
            try {
                String seqPart = lastNumber.substring(lastNumber.lastIndexOf('-') + 1);
                nextSequence = Integer.parseInt(seqPart) + 1;
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                LOGGER.log(Level.WARNING, "Erro ao parsear último número de caixa: {0}", lastNumber);
            }
        }

        return prefix + String.format("%03d", nextSequence);
    }

    // ─────────────────────────────────────────────────────────────
    // MOVIMENTAÇÃO DE SALDO
    // ─────────────────────────────────────────────────────────────

    public CashBox creditarValor(Long cashBoxPk, BigDecimal valor) {
        if (cashBoxPk == null) {
            throw new RuntimeException("É necessário indicar o caixa para creditar o valor.");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("O valor a creditar deve ser maior que zero.");
        }

        CashBox cashBox = repository.findById(cashBoxPk)
                .orElseThrow(() -> new RuntimeException("Caixa não encontrado com id: " + cashBoxPk));

        if (cashBox.getStatus() != CashBoxStatus.OPEN) {
            throw new RuntimeException(
                    "Não é possível registar entradas num caixa fechado (" + cashBox.getCashBoxNumber() + ").");
        }

        BigDecimal atual = cashBox.getTotalIncome() != null ? cashBox.getTotalIncome() : BigDecimal.ZERO;
        cashBox.setTotalIncome(atual.add(valor));

        return repository.save(cashBox);
    }

    public CashBox debitarValor(Long cashBoxPk, BigDecimal valor) {
        if (cashBoxPk == null || valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        CashBox cashBox = repository.findById(cashBoxPk).orElse(null);
        if (cashBox == null) {
            LOGGER.log(Level.WARNING, "Tentativa de estornar valor de um caixa inexistente: {0}", cashBoxPk);
            return null;
        }

        BigDecimal atual = cashBox.getTotalIncome() != null ? cashBox.getTotalIncome() : BigDecimal.ZERO;
        cashBox.setTotalIncome(atual.subtract(valor));

        return repository.save(cashBox);
    }

    // ─────────────────────────────────────────────────────────────
    // FECHAMENTO DE CAIXA
    // ─────────────────────────────────────────────────────────────

    public void closeCashBox(Long id, BigDecimal closingBalance, String observation) {
        CashBox cashBox = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caixa não encontrado com id: " + id));

        if (cashBox.getStatus() == CashBoxStatus.CLOSED) {
            throw new RuntimeException("Este caixa já está fechado.");
        }

        cashBox.setStatus(CashBoxStatus.CLOSED);
        cashBox.setClosingDate(LocalDate.now());
        cashBox.setClosingBalance(closingBalance);

        if (observation != null && !observation.isBlank()) {
            cashBox.setObservation(observation);
        }

        repository.save(cashBox);
    }

    // ─────────────────────────────────────────────────────────────
    // SALDO ATUAL (para pré-preencher fecho)
    // ─────────────────────────────────────────────────────────────

    public BigDecimal getCurrentBalance(Long cashBoxId) {
        if (cashBoxId == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal balance = repository.getCurrentBalanceById(cashBoxId);
        return balance != null ? balance : BigDecimal.ZERO;
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS
    // ─────────────────────────────────────────────────────────────

    public List<CashBoxDTO> getAllCashBoxes() {
        return repository.findAllCashBoxesDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA (com filtros)
    // ─────────────────────────────────────────────────────────────

    public Page<CashBoxDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        Pageable pageable = PageRequest.of(page, size, sort);

        String cashBoxNumber = getFilterString(filters, "cashBoxNumber");
        String operator = getFilterString(filters, "operator");
        String status = getFilterString(filters, "status");
        LocalDate startDate = getFilterDate(filters, "startDate");
        LocalDate endDate = getFilterDate(filters, "endDate");

        Page<CashBoxTableProjection> projections = repository.findAllForTable(
                cashBoxNumber, operator, status, startDate, endDate, pageable);

        return projections.map(p -> {
            CashBoxDTO dto = new CashBoxDTO();
            dto.setPhCashBox(p.getPhCashBox());
            dto.setCashBoxNumber(p.getCashBoxNumber());
            dto.setOperator(p.getOperator());
            dto.setOpeningBalance(p.getOpeningBalance());
            dto.setTotalIncome(p.getTotalIncome());
            dto.setTotalExpense(p.getTotalExpense());
            dto.setCurrentBalance(p.getCurrentBalance());
            dto.setStatus(p.getStatus() != null ? CashBoxStatus.valueOf(p.getStatus()) : null);
            dto.setOpeningDate(p.getOpeningDate());
            dto.setClosingDate(p.getClosingDate());
            dto.setCreatedAt(p.getCreatedAt());
            dto.setUpdatedAt(p.getUpdatedAt());
            return dto;
        });
    }

    private String getFilterString(Map<String, Object> filters, String key) {
        if (filters == null || !filters.containsKey(key)) {
            return null;
        }
        Object value = filters.get(key);
        if (value instanceof String s) {
            return s.isBlank() ? null : s;
        }
        return value != null ? value.toString() : null;
    }

    private LocalDate getFilterDate(Map<String, Object> filters, String key) {
        if (filters == null || !filters.containsKey(key)) {
            return null;
        }
        Object value = filters.get(key);
        if (value instanceof LocalDate d) {
            return d;
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    public List<CashBox> getByStatus(CashBoxStatus status) {
        return repository.findByStatus(status);
    }

    public List<CashBox> getByOperator(String operator) {
        return repository.findByOperator(operator);
    }

    public List<CashBox> getByOpeningDate(LocalDate openingDate) {
        return repository.findByOpeningDate(openingDate);
    }

    public List<CashBox> getByOpeningDateBetween(LocalDate startDate, LocalDate endDate) {
        return repository.findByOpeningDateBetween(startDate, endDate);
    }

    public CashBox getOpenCashBox() {
        return repository.findFirstByStatusOrderByOpeningDateDesc(CashBoxStatus.OPEN);
    }

    public boolean hasOpenCashBox() {
        return repository.existsByStatus(CashBoxStatus.OPEN);
    }

    public BigDecimal getTotalOpeningBalance() {
        BigDecimal total = repository.getTotalOpeningBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    public CashBox getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caixa não encontrado com id: " + id));
    }

    public CashBox findById(Long id) {
        return getById(id);
    }
}