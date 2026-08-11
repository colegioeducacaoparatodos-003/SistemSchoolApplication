package com.SistemSchool.modulo_Recursoa_Humano.io;

public enum QualificationLevel {

    SECONDARY,
    BACHELOR,
    POST_GRADUATION,
    MASTER,
    DOCTORATE,
    POST_DOCTORATE;

    // Métodos utilitários (mantidos, mas opcionais)
    public boolean isSECONDARY() { return this == SECONDARY; }
    public boolean isBACHELOR() { return this == BACHELOR; }
    public boolean isPOST_GRADUATION() { return this == POST_GRADUATION; }
    public boolean isMASTER() { return this == MASTER; }
    public boolean isDOCTORATE() { return this == DOCTORATE; }
    public boolean isPOST_DOCTORATE() { return this == POST_DOCTORATE; }

    
}