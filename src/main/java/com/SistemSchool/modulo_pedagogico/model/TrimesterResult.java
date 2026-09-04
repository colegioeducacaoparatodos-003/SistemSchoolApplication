package com.SistemSchool.modulo_pedagogico.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.SistemSchool.modulo_pedagogico.io.SituationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;

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
import jakarta.persistence.Table;
import java.beans.Transient;

@Entity
@Table(name = "trimester_result")
public class TrimesterResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkTrimesterResult;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolment_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_result_enrolment"))
    private Enrolment enrolment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "discipline_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_result_discipline"))
    private Discipline discipline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Trimester trimester;

    private Double mac;
    private Double npt;
    private Double mt;

    @Enumerated(EnumType.STRING)
    private SituationType situation;

    // AUDITORIA
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Transient
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public TrimesterResult() {}

    public TrimesterResult(Long pkTrimesterResult, Enrolment enrolment, Discipline discipline, Trimester trimester,
                           Double mac, Double npt, Double mt, SituationType situation,
                           String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkTrimesterResult = pkTrimesterResult;
        this.enrolment = enrolment;
        this.discipline = discipline;
        this.trimester = trimester;
        this.mac = mac;
        this.npt = npt;
        this.mt = mt;
        this.situation = situation;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPkTrimesterResult() { return pkTrimesterResult; }
    public void setPkTrimesterResult(Long pkTrimesterResult) { this.pkTrimesterResult = pkTrimesterResult; }

    public Enrolment getEnrolment() { return enrolment; }
    public void setEnrolment(Enrolment enrolment) { this.enrolment = enrolment; }

    public Discipline getDiscipline() { return discipline; }
    public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

    public Trimester getTrimester() { return trimester; }
    public void setTrimester(Trimester trimester) { this.trimester = trimester; }

    public Double getMac() { return mac; }
    public void setMac(Double mac) { this.mac = mac; }

    public Double getNpt() { return npt; }
    public void setNpt(Double npt) { this.npt = npt; }

    public Double getMt() { return mt; }
    public void setMt(Double mt) { this.mt = mt; }

    public SituationType getSituation() { return situation; }
    public void setSituation(SituationType situation) { this.situation = situation; }

    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public TrimesterResult pkTrimesterResult(Long pkTrimesterResult) { setPkTrimesterResult(pkTrimesterResult); return this; }
    public TrimesterResult enrolment(Enrolment enrolment) { setEnrolment(enrolment); return this; }
    public TrimesterResult discipline(Discipline discipline) { setDiscipline(discipline); return this; }
    public TrimesterResult trimester(Trimester trimester) { setTrimester(trimester); return this; }
    public TrimesterResult mac(Double mac) { setMac(mac); return this; }
    public TrimesterResult npt(Double npt) { setNpt(npt); return this; }
    public TrimesterResult mt(Double mt) { setMt(mt); return this; }
    public TrimesterResult situation(SituationType situation) { setSituation(situation); return this; }
    public TrimesterResult obs(String obs) { setObs(obs); return this; }
    public TrimesterResult createdAt(LocalDateTime createdAt) { setCreatedAt(createdAt); return this; }
    public TrimesterResult updatedAt(LocalDateTime updatedAt) { setUpdatedAt(updatedAt); return this; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrimesterResult)) return false;
        TrimesterResult that = (TrimesterResult) o;
        return Objects.equals(pkTrimesterResult, that.pkTrimesterResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkTrimesterResult);
    }
}