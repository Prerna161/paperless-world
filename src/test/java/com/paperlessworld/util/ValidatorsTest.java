package com.paperlessworld.util;

import com.paperlessworld.exception.ValidationException;
import com.paperlessworld.model.ClosureReason;
import com.paperlessworld.model.ThoughtType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorsTest {

    @Test
    @DisplayName("nullOrBlankIdThrowsValidationException")
    void nullOrBlankIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> Validators.requireLong(null));
        assertThrows(ValidationException.class, () -> Validators.requireLong("   "));
    }

    @Test
    @DisplayName("nonNumericIdThrowsValidationException")
    void nonNumericIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> Validators.requireLong("abc"));
        assertThrows(ValidationException.class, () -> Validators.requireLong("12.34"));
        assertThrows(ValidationException.class, () -> Validators.requireLong("123-hack"));
    }

    @Test
    @DisplayName("unknownEnumValueThrowsValidationException")
    void unknownEnumValueThrowsValidationException() {
        assertThrows(ValidationException.class, () -> Validators.requireEnum(ClosureReason.class, "NOT_A_REASON"));
        assertThrows(ValidationException.class, () -> Validators.requireEnum(ThoughtType.class, "RANDOM"));
        assertThrows(ValidationException.class, () -> Validators.requireEnum(ClosureReason.class, null));
    }

    @Test
    @DisplayName("overLengthStringThrowsValidationException")
    void overLengthStringThrowsValidationException() {
        String longTitle = "A".repeat(201);
        assertThrows(ValidationException.class, () -> Validators.requireMaxLength(longTitle, 200, "Title"));

        String exactTitle = "A".repeat(200);
        assertEquals(exactTitle, Validators.requireMaxLength(exactTitle, 200, "Title"));
    }

    @Test
    @DisplayName("validInputsPassValidation")
    void validInputsPassValidation() {
        assertEquals(42L, Validators.requireLong("42"));
        assertEquals(100L, Validators.requireLong("  100  "));
        assertEquals(ClosureReason.DONE, Validators.requireEnum(ClosureReason.class, "done"));
        assertEquals(ClosureReason.LET_GO, Validators.requireEnum(ClosureReason.class, "LET_GO"));
        assertEquals("clean", Validators.requireNonBlank("  clean  ", "Title"));
        assertEquals(3, Validators.requireBoundedInt("3", 1, 5, "Importance"));
    }
}
