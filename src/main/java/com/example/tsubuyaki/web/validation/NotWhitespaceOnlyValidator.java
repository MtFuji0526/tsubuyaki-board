package com.example.tsubuyaki.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotWhitespaceOnlyValidator implements ConstraintValidator<NotWhitespaceOnly, CharSequence> {

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return value.toString().codePoints().anyMatch(NotWhitespaceOnlyValidator::isNotWhitespace);
    }

    private static boolean isNotWhitespace(int codePoint) {
        return !Character.isWhitespace(codePoint) && !Character.isSpaceChar(codePoint);
    }
}
