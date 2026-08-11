package com.SistemSchool.modulo_secrtaria.io;

public enum SchoolClaassStatus {

    ACTIVE,
    INACTIVE,
    CANCELED,
    DECEASED;

    public boolean isACTIVE(){
        return this == ACTIVE;
    }

        public boolean isINACTIVE(){
        return this == INACTIVE;
    }

        public boolean isCANCELED(){
        return this == CANCELED;
    }

        public boolean isDECEASED(){
        return this == DECEASED;
    }
}
