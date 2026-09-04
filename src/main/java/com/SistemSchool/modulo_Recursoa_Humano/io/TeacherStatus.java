package com.SistemSchool.modulo_Recursoa_Humano.io;

public enum TeacherStatus {

    ACTIVE("Ativo"),
    INACTIVE("Inativo"),
    ON_LEAVE("De Licença"),
    SUSPENDED("Suspenso"),
    TERMINATED("Desvinculado"),
    RETIRED("Reformado");

    private final String descricao;

    TeacherStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isACTIVE() {
        return this == ACTIVE;
    }

    public boolean isINACTIVE() {
        return this == INACTIVE;
    }

    public boolean isON_LEAVE() {
        return this == ON_LEAVE;
    }

    public boolean isSUSPENDED() {
        return this == SUSPENDED;
    }

    public boolean isTERMINATED() {
        return this == TERMINATED;
    }

    public boolean isRETIRED() {
        return this == RETIRED;
    }
}
