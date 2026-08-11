package com.SistemSchool.service;

import org.springframework.stereotype.Service;

import com.SistemSchool.util.BIValidator;

@Service
public class BIValidationService {

    public boolean validar(String bi) {
        return BIValidator.isValid(bi);
    }

}