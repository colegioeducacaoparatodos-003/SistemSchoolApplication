package com.SistemSchool.modulo_Recursoa_Humano.io;

public enum QualificationLevel {

    ENSINO_MEDIO("Ensino Médio"),
    BACHARELATO("Bacharelato"),
    LICENCIATURA("Licenciatura"),
    POS_GRADUACAO("Pós-Graduação"),
    MESTRADO("Mestrado"),
    DOUTORAMENTO("Doutoramento");

    private final String descricao;

    QualificationLevel(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isENSINO_MEDIO() {
        return this == ENSINO_MEDIO;
    }

    public boolean isBACHARELATO() {
        return this == BACHARELATO;
    }

    public boolean isLICENCIATURA() {
        return this == LICENCIATURA;
    }

    public boolean isPOS_GRADUACAO() {
        return this == POS_GRADUACAO;
    }

    public boolean isMESTRADO() {
        return this == MESTRADO;
    }

    public boolean isDOUTORAMENTO() {
        return this == DOUTORAMENTO;
    }
}
