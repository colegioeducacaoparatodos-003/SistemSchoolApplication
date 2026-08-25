package com.SistemSchool.modulo_Financeiro.service;

import com.SistemSchool.modulo_Financeiro.dto.FinancialMovementDTO;
import com.SistemSchool.modulo_Financeiro.interfaces.FinancialMovementTableProjection;
import com.SistemSchool.modulo_Financeiro.io.MovementStatus;
import com.SistemSchool.modulo_Financeiro.io.MovementType;
import com.SistemSchool.modulo_Financeiro.model.CashBox;
import com.SistemSchool.modulo_Financeiro.model.FinancialMovement;
import com.SistemSchool.modulo_Financeiro.repository.CashBoxRepository;
import com.SistemSchool.modulo_Financeiro.repository.FinancialMovementRepository;
import com.SistemSchool.modulo_secrtaria.model.Pagamento;
import com.SistemSchool.modulo_secrtaria.repository.PagamentoRepository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FinancialMovementService {

    private static final int MAX_TENTATIVAS_NUMERO_MOVIMENTO = 20;

    private final FinancialMovementRepository repository;
    private final CashBoxRepository cashBoxRepository;
    private final PagamentoRepository pagamentoRepository;

    public FinancialMovementService(FinancialMovementRepository repository,
            CashBoxRepository cashBoxRepository,
            PagamentoRepository pagamentoRepository) {
        this.repository = repository;
        this.cashBoxRepository = cashBoxRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    /**
     * Ponto único de entrada para criar movimentos financeiros — seja um
     * lançamento manual feito pelo FinancialMovementController, seja um
     * movimento gerado automaticamente pelo PagamentoService ao confirmar um
     * pagamento. Qualquer que seja a origem, o saldo do CashBox (totalIncome/
     * totalExpense) é sempre atualizado aqui, garantindo que a coluna
     * persistida em CashBox nunca fique dessincronizada da soma real dos
     * movimentos.
     */
    public FinancialMovement save(FinancialMovement movement) {
        if (repository.existsByMovementNumber(movement.getMovementNumber())) {
            throw new RuntimeException("Número de movimento já existe: " + movement.getMovementNumber());
        }
        if (movement.getCashBox() == null || movement.getCashBox().getPhCashBox() == null) {
            throw new RuntimeException("É necessário indicar o caixa para o movimento.");
        }

        Long cashBoxPk = movement.getCashBox().getPhCashBox();
        CashBox cashBox = cashBoxRepository.findById(cashBoxPk)
                .orElseThrow(() -> new RuntimeException("Caixa não encontrado com id: " + cashBoxPk));
        movement.setCashBox(cashBox);

        if (movement.getPagamento() != null && movement.getPagamento().getPkPagamento() != null) {
            Long pagamentoPk = movement.getPagamento().getPkPagamento();
            Pagamento pagamento = pagamentoRepository.findById(pagamentoPk)
                    .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com id: " + pagamentoPk));
            movement.setPagamento(pagamento);
        } else {
            movement.setPagamento(null);
        }

        if (movement.getMovementDate() == null) {
            movement.setMovementDate(LocalDateTime.now());
        }
        if (movement.getStatus() == null) {
            movement.setStatus(MovementStatus.ACTIVE);
        }
        movement.setCreatedAt(LocalDateTime.now());
        movement.setUpdatedAt(LocalDateTime.now());

        FinancialMovement saved = repository.save(movement);

        applyToCashBoxBalance(cashBox, saved, +1);

        return saved;
    }

    public void update(FinancialMovementDTO dto) {
        FinancialMovement movement = repository.findById(dto.getPhMovement())
                .orElseThrow(() -> new RuntimeException("Movimento não encontrado com id: " + dto.getPhMovement()));

        // Estorna o efeito do valor/tipo/estado antigos no saldo do caixa
        // antigo antes de aplicar as alterações, para não duplicar nem
        // perder valores no saldo.
        CashBox oldCashBox = movement.getCashBox();
        applyToCashBoxBalance(oldCashBox, movement, -1);

        if (dto.getCashBoxPk() != null
                && !dto.getCashBoxPk().equals(movement.getCashBox().getPhCashBox())) {
            CashBox cashBox = cashBoxRepository.findById(dto.getCashBoxPk())
                    .orElseThrow(() -> new RuntimeException(
                            "Caixa não encontrado com id: " + dto.getCashBoxPk()));
            movement.setCashBox(cashBox);
        }

        if (dto.getPagamentoPk() != null) {
            if (movement.getPagamento() == null
                    || !dto.getPagamentoPk().equals(movement.getPagamento().getPkPagamento())) {
                Pagamento pagamento = pagamentoRepository.findById(dto.getPagamentoPk())
                        .orElseThrow(() -> new RuntimeException(
                                "Pagamento não encontrado com id: " + dto.getPagamentoPk()));
                movement.setPagamento(pagamento);
            }
        } else {
            movement.setPagamento(null);
        }

        movement.setMovementNumber(dto.getMovementNumber());
        movement.setDescription(dto.getDescription());
        movement.setAmount(dto.getAmount());
        movement.setType(dto.getType());
        movement.setCategory(dto.getCategory());
        movement.setStatus(dto.getStatus());
        movement.setResponsible(dto.getResponsible());
        movement.setObservation(dto.getObservation());
        movement.setMovementDate(dto.getMovementDate());
        movement.setUpdatedAt(LocalDateTime.now());

        FinancialMovement saved = repository.save(movement);

        // Aplica o efeito dos novos valores/tipo/estado no caixa (novo ou
        // mesmo) atual.
        applyToCashBoxBalance(saved.getCashBox(), saved, +1);
    }

    public void delete(Long id) {
        FinancialMovement movement = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimento não encontrado com id: " + id));

        // Estorna o efeito do movimento no saldo do caixa antes de eliminar,
        // para que o saldo continue correto após a remoção.
        applyToCashBoxBalance(movement.getCashBox(), movement, -1);

        repository.deleteById(id);
    }

    /**
     * Aplica (ou reverte, quando sign = -1) o efeito de um movimento no
     * saldo do caixa. Movimentos que não estão ACTIVE não afetam o saldo —
     * um movimento CANCELLED, por exemplo, não deve nunca ter sido somado.
     */
    private void applyToCashBoxBalance(CashBox cashBox, FinancialMovement movement, int sign) {
        if (cashBox == null || movement.getStatus() != MovementStatus.ACTIVE) {
            return;
        }

        BigDecimal amount = movement.getAmount() != null ? movement.getAmount() : BigDecimal.ZERO;
        BigDecimal signedAmount = sign < 0 ? amount.negate() : amount;

        if (movement.getType() == MovementType.INCOME) {
            BigDecimal current = cashBox.getTotalIncome() != null ? cashBox.getTotalIncome() : BigDecimal.ZERO;
            cashBox.setTotalIncome(current.add(signedAmount));
        } else if (movement.getType() == MovementType.EXPENSE) {
            BigDecimal current = cashBox.getTotalExpense() != null ? cashBox.getTotalExpense() : BigDecimal.ZERO;
            cashBox.setTotalExpense(current.add(signedAmount));
        }

        cashBoxRepository.save(cashBox);
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<FinancialMovementDTO> getAllFinancialMovements() {
        return repository.findAllFinancialMovementsDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA
    // ─────────────────────────────────────────────────────────────

    public Page<FinancialMovementTableProjection> findLazy(int page, int size, Sort sort,
            Map<String, Object> filters) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAllForTable(pageable);
    }

    public List<FinancialMovementTableProjection> getAllForTable() {
        return repository.findAllForTable(Pageable.unpaged()).getContent();
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    public List<FinancialMovement> getByCashBox(Long cashBoxPk) {
        return repository.findByCashBox_PhCashBox(cashBoxPk);
    }

    public List<FinancialMovement> getByPagamento(Long pagamentoPk) {
        return repository.findByPagamento_PkPagamento(pagamentoPk);
    }

    public List<FinancialMovement> getByType(MovementType type) {
        return repository.findByType(type);
    }

    public List<FinancialMovement> getByStatus(MovementStatus status) {
        return repository.findByStatus(status);
    }

    public List<FinancialMovement> getByCategory(String category) {
        return repository.findByCategory(category);
    }

    public List<FinancialMovement> getByPeriod(LocalDateTime start, LocalDateTime end) {
        return repository.findByMovementDateBetween(start, end);
    }

    public FinancialMovement getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimento não encontrado com id: " + id));
    }

    public FinancialMovement findById(Long id) {
        return getById(id);
    }

    public long count() {
        return repository.count();
    }

    public boolean existsByMovementNumber(String movementNumber) {
        return repository.existsByMovementNumber(movementNumber);
    }

    // ─────────────────────────────────────────────────────────────
    // GERAÇÃO DE NÚMEROS SEQUENCIAIS
    // ─────────────────────────────────────────────────────────────

    /**
     * Gera o próximo número de movimento no formato MOV-<ano>-<sequência>.
     *
     * IMPORTANTE: usa a MAIOR sequência já usada no ano (via
     * findMaxSequenceForYear), e NÃO repository.count(). Usar count() estava
     * a causar duplicados (ex: "MOV-2026-00094" já existente) sempre que um
     * movimento era eliminado, porque a contagem total de linhas baixa mas a
     * sequência de números já emitidos não retrocede. Isso, por sua vez,
     * fazia com que a gravação do movimento falhasse a meio da transação de
     * confirmação de pagamento, provocando rollback do pagamento também.
     *
     * Este método é a fonte única de geração de números de movimento —
     * usado tanto pelo PagamentoService (movimento automático ao confirmar
     * um pagamento) como por qualquer outro ponto que precise de um número
     * novo. Não escreve nada na base de dados; é apenas cálculo em memória
     * antes do save().
     */
    public String generateMovementNumber() {
        int year = Year.now().getValue();
        long nextSeq = repository.findMaxSequenceForYear(year) + 1;

        String numero;
        int tentativas = 0;
        do {
            numero = String.format("MOV-%d-%05d", year, nextSeq);
            if (!repository.existsByMovementNumber(numero)) {
                break;
            }
            nextSeq++;
            tentativas++;
        } while (tentativas < MAX_TENTATIVAS_NUMERO_MOVIMENTO);

        if (tentativas >= MAX_TENTATIVAS_NUMERO_MOVIMENTO) {
            throw new RuntimeException(
                    "Não foi possível gerar um número de movimento único após " + MAX_TENTATIVAS_NUMERO_MOVIMENTO
                            + " tentativas. Verifique a sequência de movimentos do ano " + year + ".");
        }

        return numero;
    }

    // ─────────────────────────────────────────────────────────────
    // RELATÓRIOS FINANCEIROS
    // ─────────────────────────────────────────────────────────────

    public BigDecimal getTotalIncome() {
        return repository.getTotalIncome();
    }

    public BigDecimal getTotalExpense() {
        return repository.getTotalExpense();
    }

    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }
}