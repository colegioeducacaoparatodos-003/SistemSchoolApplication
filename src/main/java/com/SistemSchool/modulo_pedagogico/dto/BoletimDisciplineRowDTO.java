package com.SistemSchool.modulo_pedagogico.dto;

import java.io.Serializable;

public class BoletimDisciplineRowDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String disciplineName;
    private Double mac;
    private Double npp;
    private Double npt;
    private Double mt;  // Média Trimestral

    public BoletimDisciplineRowDTO() {}

    public String getDisciplineName() { return disciplineName; }
    public void setDisciplineName(String disciplineName) { this.disciplineName = disciplineName; }

    public Double getMac() { return mac; }
    public void setMac(Double mac) { this.mac = mac; }

    public Double getNpp() { return npp; }
    public void setNpp(Double npp) { this.npp = npp; }

    public Double getNpt() { return npt; }
    public void setNpt(Double npt) { this.npt = npt; }

    public Double getMt() { return mt; }
    public void setMt(Double mt) { this.mt = mt; }

    public String getMtFormatted() {
        return mt != null ? String.format("%.1f", mt) : "-";
    }
}
