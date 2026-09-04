package com.SistemSchool.modulo_pedagogico.io;

public enum SituationType {
    APROVADO("Aprovado"),
    REPROVADO("Reprovado"),
    EM_CURSO("Em Curso"),
    EM_EXAME("Em Exame");

    private final String descricao;

    SituationType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}