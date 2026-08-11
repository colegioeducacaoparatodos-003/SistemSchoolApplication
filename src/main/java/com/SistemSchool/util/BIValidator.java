package com.SistemSchool.util;

import java.util.regex.Pattern;

public final class BIValidator {

    private BIValidator() {
    }

    // 9 dígitos + 2 letras + 3 dígitos
    private static final Pattern BI_PATTERN =
            Pattern.compile("^\\d{9}[A-Z]{2}\\d{3}$");

    public static boolean isValid(String bi) {

        if (bi == null || bi.isBlank()) {
            return false;
        }

        bi = bi.trim().toUpperCase();

        return BI_PATTERN.matcher(bi).matches();
    }

}