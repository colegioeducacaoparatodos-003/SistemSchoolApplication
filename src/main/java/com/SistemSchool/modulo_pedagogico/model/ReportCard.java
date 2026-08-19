package com.SistemSchool.modulo_pedagogico.model;

import java.time.LocalDateTime;

import com.SistemSchool.modulo_pedagogico.io.ReportCardStatus;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;

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
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

/**
 * Guarda, por matrícula e trimestre, os dados do Boletim que NÃO são
 * calculados a partir de Grade/Evaluation: comportamento, observação,
 * período e estado de emissão. As notas por disciplina continuam a ser
 * calculadas em tempo real via GradeRepository.calcularMediaFinal
 * (ver BoletimService). Ano lectivo vem de SchoolClass.anoLectivo,
 * não é duplicado aqui.
 */
@Entity
@Table(name = "report_card", uniqueConstraints = {
        @UniqueConstraint(name = "uq_report_card_enrolment_trimester", columnNames = { "enrolment_pk", "trimester" })
})
public class ReportCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkReportCard;

    /** Relacionamentos */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolment_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_reportcard_enrolment"))
    private Enrolment enrolment;

    private Integer trimester;

    // Período (ex.: "1º Período")
    private String period;

    // Comportamento (ex.: "Bom", "Muito Bom")
    private String behavior;

    private String observation;

    @Enumerated(EnumType.STRING)
    private ReportCardStatus status;

    // =======================================
    // Auditoria
    // =======================================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = ReportCardStatus.DRAFT;
        }

    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public ReportCard() {
    }

    public ReportCard(Long pkReportCard, Enrolment enrolment, Integer trimester,
                      String period, String behavior, String observation,
                      ReportCardStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.pkReportCard = pkReportCard;
        this.enrolment = enrolment;
        this.trimester = trimester;
        this.period = period;
        this.behavior = behavior;
        this.observation = observation;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

    }

    public Long getPkReportCard() {
        return this.pkReportCard;
    }

    public void setPkReportCard(Long pkReportCard) {
        this.pkReportCard = pkReportCard;
    }

    public Enrolment getEnrolment() {
        return this.enrolment;
    }

    public void setEnrolment(Enrolment enrolment) {
        this.enrolment = enrolment;
    }

    public Integer getTrimester() {
        return this.trimester;
    }

    public void setTrimester(Integer trimester) {
        this.trimester = trimester;
    }

    public String getPeriod() {
        return this.period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getBehavior() {
        return this.behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }

    public String getObservation() {
        return this.observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public ReportCardStatus getStatus() {
        return this.status;
    }

    public void setStatus(ReportCardStatus status) {
        this.status = status;
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

    public ReportCard pkReportCard(Long pkReportCard) {
        setPkReportCard(pkReportCard);
        return this;
    }

    public ReportCard enrolment(Enrolment enrolment) {
        setEnrolment(enrolment);
        return this;
    }

    public ReportCard trimester(Integer trimester) {
        setTrimester(trimester);
        return this;
    }

    public ReportCard period(String period) {
        setPeriod(period);
        return this;
    }

    public ReportCard behavior(String behavior) {
        setBehavior(behavior);
        return this;
    }

    public ReportCard observation(String observation) {
        setObservation(observation);
        return this;
    }

    public ReportCard status(ReportCardStatus status) {
        setStatus(status);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ReportCard)) {
            return false;
        }
        ReportCard reportCard = (ReportCard) o;
        return Objects.equals(pkReportCard, reportCard.pkReportCard) && Objects.equals(enrolment, reportCard.enrolment) && Objects.equals(trimester, reportCard.trimester) && Objects.equals(period, reportCard.period) && Objects.equals(behavior, reportCard.behavior) && Objects.equals(observation, reportCard.observation) && Objects.equals(status, reportCard.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkReportCard, enrolment, trimester, period, behavior, observation, status);
    }

    @Override
    public String toString() {
        return "{" +
            " pkReportCard='" + getPkReportCard() + "'" +
            ", enrolment='" + getEnrolment() + "'" +
            ", trimester='" + getTrimester() + "'" +
            ", period='" + getPeriod() + "'" +
            ", behavior='" + getBehavior() + "'" +
            ", observation='" + getObservation() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }

}