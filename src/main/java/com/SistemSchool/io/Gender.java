package com.SistemSchool.io;

public enum Gender  {
    MALE("Masculino"),
    FEMALE("Feminino"),
    OTHER("Outro"),
    PREFER_NOT_TO_SAY("Prefere não dizer");

    private final String descricao;

    Gender(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}