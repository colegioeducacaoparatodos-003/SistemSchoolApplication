package com.SistemSchool.modulo_secrtaria.io;


public enum DocumentType {

    CERTIDAO_NASCIMENTO("Certidão de Nascimento"),

    BILHETE_IDENTIDADE("Bilhete de Identidade"),

    FOTO("Fotografia"),

    CARTAO_VACINA("Cartão de Vacinação"),

    DECLARACAO_TRANSFERENCIA("Declaração de Transferência"),

    CERTIFICADO("Certificado"),

    OUTRO("Outro");

    private final String descricao;

    DocumentType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

}
