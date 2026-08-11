package com.SistemSchool.modulo_secrtaria.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.SistemSchool.modulo_secrtaria.io.EstadoPagamento;
import com.SistemSchool.modulo_secrtaria.io.FormaPagamento;
import com.SistemSchool.modulo_secrtaria.io.MesReferencia;
import com.SistemSchool.modulo_secrtaria.model.Pagamento;

public class PagamentoDTO {

    private Long pkPagamento;

    private String numeroDocumento;

    // ===========================
    // Enrolment / Student
    // ===========================

    private Long enrolmentPk;

    private String enrolmentNumero;

    private String studentFullName;

    // ===========================
    // Fee
    // ===========================

    private Long feePk;

    private String feeDescricao; // TODO: confirmar nome real do campo em Fee

    // ===========================
    // CashBox
    // ===========================

    private Long cashBoxPk;

    private String cashBoxNumber;

    private BigDecimal valor;

    private BigDecimal multa;

    private BigDecimal total;

    private LocalDateTime dataEmissao;

    private LocalDateTime dataPagamento;

    private FormaPagamento formaPagamento;

    private EstadoPagamento estado;

    private MesReferencia mesReferencia;

    private String referencia;

    private String observacao;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public PagamentoDTO() {
    }

    public PagamentoDTO(
            Long pkPagamento,
            String numeroDocumento,

            Long enrolmentPk,
            String enrolmentNumero,
            String studentFullName,

            Long feePk,
            String feeDescricao,

            Long cashBoxPk,
            String cashBoxNumber,

            BigDecimal valor,
            BigDecimal multa,
            BigDecimal total,

            LocalDateTime dataEmissao,
            LocalDateTime dataPagamento,

            FormaPagamento formaPagamento,
            EstadoPagamento estado,
            MesReferencia mesReferencia,

            String referencia,
            String observacao,

            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.pkPagamento = pkPagamento;
        this.numeroDocumento = numeroDocumento;
        this.enrolmentPk = enrolmentPk;
        this.enrolmentNumero = enrolmentNumero;
        this.studentFullName = studentFullName;
        this.feePk = feePk;
        this.feeDescricao = feeDescricao;
        this.cashBoxPk = cashBoxPk;
        this.cashBoxNumber = cashBoxNumber;
        this.valor = valor;
        this.multa = multa;
        this.total = total;
        this.dataEmissao = dataEmissao;
        this.dataPagamento = dataPagamento;
        this.formaPagamento = formaPagamento;
        this.estado = estado;
        this.mesReferencia = mesReferencia;
        this.referencia = referencia;
        this.observacao = observacao;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // =================================================
    // ENTITY -> DTO
    // =================================================

    public static PagamentoDTO fromEntity(Pagamento pagamento) {

        Long enrolmentPk = null;
        String enrolmentNumero = null;
        String studentFullName = null;

        if (pagamento.getEnrolment() != null) {

            enrolmentPk = pagamento.getEnrolment().getPhEnrolment();
            enrolmentNumero = pagamento.getEnrolment().getEnrolmentNumer();

            if (pagamento.getEnrolment().getStudent() != null) {
                studentFullName = pagamento.getEnrolment().getStudent().getFullName();
            }
        }

        Long feePk = null;
        String feeDescricao = null;

        if (pagamento.getFee() != null) {
            feePk = pagamento.getFee().getPhFee(); // TODO: confirmar getter real
            // feeDescricao = pagamento.getFee().getDescricao(); // TODO: confirmar getter real
        }

        Long cashBoxPk = null;
        String cashBoxNumber = null;

        if (pagamento.getCashBox() != null) {
            cashBoxPk = pagamento.getCashBox().getPhCashBox();
            cashBoxNumber = pagamento.getCashBox().getCashBoxNumber();
        }

        return new PagamentoDTO(
                pagamento.getPkPagamento(),
                pagamento.getNumeroDocumento(),

                enrolmentPk,
                enrolmentNumero,
                studentFullName,

                feePk,
                feeDescricao,

                cashBoxPk,
                cashBoxNumber,

                pagamento.getValor(),
                pagamento.getMulta(),
                pagamento.getTotal(),

                pagamento.getDataEmissao(),
                pagamento.getDataPagamento(),

                pagamento.getFormaPagamento(),
                pagamento.getEstado(),
                pagamento.getMesReferencia(),

                pagamento.getReferencia(),
                pagamento.getObservacao(),

                pagamento.getCreatedAt(),
                pagamento.getUpdatedAt()
        );
    }

    public Long getPkPagamento() {
        return pkPagamento;
    }

    public void setPkPagamento(Long pkPagamento) {
        this.pkPagamento = pkPagamento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public Long getEnrolmentPk() {
        return enrolmentPk;
    }

    public void setEnrolmentPk(Long enrolmentPk) {
        this.enrolmentPk = enrolmentPk;
    }

    public String getEnrolmentNumero() {
        return enrolmentNumero;
    }

    public void setEnrolmentNumero(String enrolmentNumero) {
        this.enrolmentNumero = enrolmentNumero;
    }

    public String getStudentFullName() {
        return studentFullName;
    }

    public void setStudentFullName(String studentFullName) {
        this.studentFullName = studentFullName;
    }

    public Long getFeePk() {
        return feePk;
    }

    public void setFeePk(Long feePk) {
        this.feePk = feePk;
    }

    public String getFeeDescricao() {
        return feeDescricao;
    }

    public void setFeeDescricao(String feeDescricao) {
        this.feeDescricao = feeDescricao;
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

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getMulta() {
        return multa;
    }

    public void setMulta(BigDecimal multa) {
        this.multa = multa;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public EstadoPagamento getEstado() {
        return estado;
    }

    public void setEstado(EstadoPagamento estado) {
        this.estado = estado;
    }

    public MesReferencia getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(MesReferencia mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
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