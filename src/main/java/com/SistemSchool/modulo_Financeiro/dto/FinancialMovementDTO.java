package com.SistemSchool.modulo_Financeiro.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.SistemSchool.modulo_Financeiro.io.MovementType;
import com.SistemSchool.modulo_Financeiro.io.MovementStatus;
import com.SistemSchool.modulo_Financeiro.model.FinancialMovement;

public class FinancialMovementDTO {

    private Long phMovement;

    private String movementNumber;

    // ==========================
    // Caixa
    // ==========================

    private Long cashBoxPk;

    private String cashBoxNumber;

    // ==========================
    // Pagamento relacionado
    // ==========================

    private Long pagamentoPk;

    private String pagamentoNumeroDocumento;

    private String description;

    private BigDecimal amount;

    private MovementType type;

    private MovementStatus status;

    private String category;

    private String responsible;

    private String observation;

    private LocalDateTime movementDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public FinancialMovementDTO() {

    }

    /**
     * IMPORTANTE: a ordem dos parâmetros deste construtor tem de bater
     * EXATAMENTE (posição e tipo) com a ordem usada na constructor
     * expression "SELECT new ...FinancialMovementDTO(...)" definida em
     * FinancialMovementRepository.findAllFinancialMovementsDTO().
     * Se alterar um dos dois lados, altere o outro também.
     */
    public FinancialMovementDTO(
            Long phMovement,
            String movementNumber,

            Long cashBoxPk,
            String cashBoxNumber,

            Long pagamentoPk,
            String pagamentoNumeroDocumento,

            String description,

            BigDecimal amount,

            MovementType type,

            String category,

            MovementStatus status,

            String responsible,

            String observation,

            LocalDateTime movementDate,

            LocalDateTime createdAt,

            LocalDateTime updatedAt) {

        this.phMovement = phMovement;

        this.movementNumber = movementNumber;

        this.cashBoxPk = cashBoxPk;

        this.cashBoxNumber = cashBoxNumber;

        this.pagamentoPk = pagamentoPk;

        this.pagamentoNumeroDocumento = pagamentoNumeroDocumento;

        this.description = description;

        this.amount = amount;

        this.type = type;

        this.category = category;

        this.status = status;

        this.responsible = responsible;

        this.observation = observation;

        this.movementDate = movementDate;

        this.createdAt = createdAt;

        this.updatedAt = updatedAt;

    }

    // =====================================================
    // ENTITY -> DTO
    // =====================================================

    public static FinancialMovementDTO fromEntity(
            FinancialMovement movement) {

        Long cashBoxPk = null;

        String cashBoxNumber = null;

        Long pagamentoPk = null;

        String pagamentoNumeroDocumento = null;

        if (movement.getCashBox() != null) {

            cashBoxPk = movement.getCashBox()
                    .getPhCashBox();

            cashBoxNumber = movement.getCashBox()
                    .getCashBoxNumber();

        }

        if (movement.getPagamento() != null) {

            pagamentoPk = movement.getPagamento()
                    .getPkPagamento();

            pagamentoNumeroDocumento = movement.getPagamento()
                    .getNumeroDocumento();

        }

        return new FinancialMovementDTO(

                movement.getPhMovement(),

                movement.getMovementNumber(),

                cashBoxPk,

                cashBoxNumber,

                pagamentoPk,

                pagamentoNumeroDocumento,

                movement.getDescription(),

                movement.getAmount(),

                movement.getType(),

                movement.getCategory(),

                movement.getStatus(),

                movement.getResponsible(),

                movement.getObservation(),

                movement.getMovementDate(),

                movement.getCreatedAt(),

                movement.getUpdatedAt()

        );

    }

    public Long getPhMovement() {
        return phMovement;
    }

    public void setPhMovement(Long phMovement) {
        this.phMovement = phMovement;
    }

    public String getMovementNumber() {
        return movementNumber;
    }

    public void setMovementNumber(String movementNumber) {
        this.movementNumber = movementNumber;
    }

    public Long getCashBoxPk() {
        return cashBoxPk;
    }

    public void setCashBoxPk(Long cashBoxPk) {
        this.cashBoxPk = cashBoxPk;
    }

    public String getCashBoxNumber() {
        return cashBoxNumber;
    }

    public void setCashBoxNumber(String cashBoxNumber) {
        this.cashBoxNumber = cashBoxNumber;
    }

    public Long getPagamentoPk() {
        return pagamentoPk;
    }

    public void setPagamentoPk(Long pagamentoPk) {
        this.pagamentoPk = pagamentoPk;
    }

    public String getPagamentoNumeroDocumento() {
        return pagamentoNumeroDocumento;
    }

    public void setPagamentoNumeroDocumento(String pagamentoNumeroDocumento) {
        this.pagamentoNumeroDocumento = pagamentoNumeroDocumento;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public MovementStatus getStatus() {
        return status;
    }

    public void setStatus(MovementStatus status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}