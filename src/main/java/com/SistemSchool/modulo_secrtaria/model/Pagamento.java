package com.SistemSchool.modulo_secrtaria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.SistemSchool.modulo_Financeiro.model.CashBox;
import com.SistemSchool.modulo_Financeiro.model.Fee;
import com.SistemSchool.modulo_secrtaria.io.EstadoPagamento;
import com.SistemSchool.modulo_secrtaria.io.FormaPagamento;
import com.SistemSchool.modulo_secrtaria.io.MesReferencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkPagamento;

    // Número do documento deve ser alinhatorio
    @Column(nullable = false, unique = true)
    private String numeroDocumento;

    // Matrícula do aluno e o Nome do estudante
    /** Relacionamentos */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolment_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_pagamento_enrolment"))
    private Enrolment enrolment;

    // Configuração da propina
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_pagamento_fee"))
    private Fee fee;

    // Caixa onde foi recebido
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_box_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_pagamento_cash_box"))
    private CashBox cashBox;

    // Valor da propina
    private BigDecimal valor;

    // Multa aplicada
    private BigDecimal multa;

    // Total da cobrança
    private BigDecimal total;

    // Data de emissão
    private LocalDateTime dataEmissao;

    // Data do pagamento
    private LocalDateTime dataPagamento;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    private EstadoPagamento estado;

    @Enumerated(EnumType.STRING)
    private MesReferencia mesReferencia;

    private String referencia;

    private String observacao;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();

        this.updatedAt = LocalDateTime.now();

        if (this.estado == null) {

            this.estado = EstadoPagamento.PENDENTE;

        }

        if (this.dataEmissao == null) {

            this.dataEmissao = LocalDateTime.now();

        }

    }

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();

    }


    public Pagamento() {
    }

    public Pagamento(Long pkPagamento, String numeroDocumento, Enrolment enrolment, 
        Fee fee, CashBox cashBox, BigDecimal valor, 
        BigDecimal multa, BigDecimal total, LocalDateTime dataEmissao, LocalDateTime dataPagamento, 
        FormaPagamento formaPagamento, EstadoPagamento estado, MesReferencia mesReferencia, 
        String referencia, String observacao, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkPagamento = pkPagamento;
        this.numeroDocumento = numeroDocumento;
        this.enrolment = enrolment;
        this.fee = fee;
        this.cashBox = cashBox;
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

    public Long getPkPagamento() {
        return this.pkPagamento;
    }

    public void setPkPagamento(Long pkPagamento) {
        this.pkPagamento = pkPagamento;
    }

    public String getNumeroDocumento() {
        return this.numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public Enrolment getEnrolment() {
        return this.enrolment;
    }

    public void setEnrolment(Enrolment enrolment) {
        this.enrolment = enrolment;
    }

    public Fee getFee() {
        return this.fee;
    }

    public void setFee(Fee fee) {
        this.fee = fee;
    }

    public CashBox getCashBox() {
        return this.cashBox;
    }

    public void setCashBox(CashBox cashBox) {
        this.cashBox = cashBox;
    }

    public BigDecimal getValor() {
        return this.valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getMulta() {
        return this.multa;
    }

    public void setMulta(BigDecimal multa) {
        this.multa = multa;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getDataEmissao() {
        return this.dataEmissao;
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }


    public LocalDateTime getDataPagamento() {
        return this.dataPagamento;
    }

    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public FormaPagamento getFormaPagamento() {
        return this.formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public EstadoPagamento getEstado() {
        return this.estado;
    }

    public void setEstado(EstadoPagamento estado) {
        this.estado = estado;
    }

    public MesReferencia getMesReferencia() {
        return this.mesReferencia;
    }

    public void setMesReferencia(MesReferencia mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public String getReferencia() {
        return this.referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Pagamento pkPagamento(Long pkPagamento) {
        setPkPagamento(pkPagamento);
        return this;
    }

    public Pagamento numeroDocumento(String numeroDocumento) {
        setNumeroDocumento(numeroDocumento);
        return this;
    }

    public Pagamento enrolment(Enrolment enrolment) {
        setEnrolment(enrolment);
        return this;
    }

    public Pagamento fee(Fee fee) {
        setFee(fee);
        return this;
    }

    public Pagamento cashBox(CashBox cashBox) {
        setCashBox(cashBox);
        return this;
    }

    public Pagamento valor(BigDecimal valor) {
        setValor(valor);
        return this;
    }

    public Pagamento multa(BigDecimal multa) {
        setMulta(multa);
        return this;
    }

    public Pagamento total(BigDecimal total) {
        setTotal(total);
        return this;
    }

    public Pagamento dataEmissao(LocalDateTime dataEmissao) {
        setDataEmissao(dataEmissao);
        return this;
    }

    public Pagamento dataPagamento(LocalDateTime dataPagamento) {
        setDataPagamento(dataPagamento);
        return this;
    }

    public Pagamento formaPagamento(FormaPagamento formaPagamento) {
        setFormaPagamento(formaPagamento);
        return this;
    }

    public Pagamento estado(EstadoPagamento estado) {
        setEstado(estado);
        return this;
    }

    public Pagamento mesReferencia(MesReferencia mesReferencia) {
        setMesReferencia(mesReferencia);
        return this;
    }

    public Pagamento referencia(String referencia) {
        setReferencia(referencia);
        return this;
    }

    public Pagamento observacao(String observacao) {
        setObservacao(observacao);
        return this;
    }

    public Pagamento createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public Pagamento updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Pagamento)) {
            return false;
        }
        Pagamento pagamento = (Pagamento) o;
        return Objects.equals(pkPagamento, pagamento.pkPagamento) && Objects.equals(numeroDocumento, pagamento.numeroDocumento) && Objects.equals(enrolment, pagamento.enrolment) && Objects.equals(fee, pagamento.fee) && Objects.equals(cashBox, pagamento.cashBox) && Objects.equals(valor, pagamento.valor) && Objects.equals(multa, pagamento.multa) && Objects.equals(total, pagamento.total) && Objects.equals(dataEmissao, pagamento.dataEmissao) && Objects.equals(dataPagamento, pagamento.dataPagamento) && Objects.equals(formaPagamento, pagamento.formaPagamento) && Objects.equals(estado, pagamento.estado) && Objects.equals(mesReferencia, pagamento.mesReferencia) && Objects.equals(referencia, pagamento.referencia) && Objects.equals(observacao, pagamento.observacao) && Objects.equals(createdAt, pagamento.createdAt) && Objects.equals(updatedAt, pagamento.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkPagamento, numeroDocumento, enrolment, 
            fee, cashBox, valor, multa, total, dataEmissao,
            dataPagamento, formaPagamento, estado, 
            mesReferencia, referencia, observacao, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "{" +
            " pkPagamento='" + getPkPagamento() + "'" +
            ", numeroDocumento='" + getNumeroDocumento() + "'" +
            ", enrolment='" + getEnrolment() + "'" +
            ", fee='" + getFee() + "'" +
            ", cashBox='" + getCashBox() + "'" +
            ", valor='" + getValor() + "'" +
            ", multa='" + getMulta() + "'" +
            ", total='" + getTotal() + "'" +
            ", dataEmissao='" + getDataEmissao() + "'" +
            ", dataPagamento='" + getDataPagamento() + "'" +
            ", formaPagamento='" + getFormaPagamento() + "'" +
            ", estado='" + getEstado() + "'" +
            ", mesReferencia='" + getMesReferencia() + "'" +
            ", referencia='" + getReferencia() + "'" +
            ", observacao='" + getObservacao() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }

}