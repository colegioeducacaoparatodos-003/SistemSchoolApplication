package com.SistemSchool.modulo_pedagogico.io;

public enum EvaluationType {
    CONTINUA("Avaliação Contínua"),
    PROVA_TRIMESTRAL("Prova Trimestral");

    private final String descricao;

    EvaluationType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    // Alias para compatibilidade com a view (EL chama getDescription())
    public String getDescription() {
        return descricao;
    }
}