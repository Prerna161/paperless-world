package com.paperlessworld.util;

import com.paperlessworld.exception.ValidationException;

public final class Validators {
    private Validators() {}

    public static long requireLong(String param) {
        if (param == null || param.isBlank()) {
            throw new ValidationException("Required numerical identifier is missing");
        }
        try {
            return Long.parseLong(param.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid number format: " + param);
        }
    }

    public static <E extends Enum<E>> E requireEnum(Class<E> enumClass, String param) {
        if (param == null || param.isBlank()) {
            throw new ValidationException("Required selection (" + enumClass.getSimpleName() + ") is missing");
        }
        try {
            return Enum.valueOf(enumClass, param.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid " + enumClass.getSimpleName() + " value: " + param);
        }
    }

    public static String requireNonBlank(String param, String fieldName) {
        if (param == null || param.isBlank()) {
            throw new ValidationException(fieldName + " cannot be blank");
        }
        return param.trim();
    }

    public static String requireMaxLength(String param, int maxLength, String fieldName) {
        if (param != null && param.length() > maxLength) {
            throw new ValidationException(fieldName + " exceeds maximum allowed length of " + maxLength + " characters");
        }
        return param;
    }

    public static int requireBoundedInt(String param, int min, int max, String fieldName) {
        long val = requireLong(param);
        if (val < min || val > max) {
            throw new ValidationException(fieldName + " must be between " + min + " and " + max);
        }
        return (int) val;
    }
}
