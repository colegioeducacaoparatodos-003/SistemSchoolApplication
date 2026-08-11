package com.SistemSchool.modulo_dashboard_charts.dto;

import com.SistemSchool.io.Perfil;

public class ProfileCountDTO {

    private final Perfil perfil;
    private final Long total;

    public ProfileCountDTO(Perfil perfil, Long total) {
        this.perfil = perfil;
        this.total = total;
    }

    public Perfil getPerfil() { return perfil; }
    public Long getTotal() { return total; }
}