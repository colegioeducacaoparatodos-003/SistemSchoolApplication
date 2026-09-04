package com.SistemSchool.modulo_pedagogico.io;

public enum Trimester {
    PRIMEIRO("1º Trimestre"),
    SEGUNDO("2º Trimestre"),
    TERCEIRO("3º Trimestre");

    private final String descricao;

    Trimester(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}