package com.SistemSchool.io;

public enum Perfil {

    ADMIN("Administrador"),
    SECRETARY("Secretário/a"),
    FINANCIAL("Financeiro"),
    PEDAGOGICAL("Director Pedagógico");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
