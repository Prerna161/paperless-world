package com.paperlessworld.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {

    @Test
    @DisplayName("verifyingCorrectPasswordSucceeds")
    void verifyingCorrectPasswordSucceeds() {
        char[] password = "vivaPassword2026!".toCharArray();
        String hash = PasswordHasher.hash(password);

        assertNotNull(hash);
        assertTrue(PasswordHasher.verify(password, hash));
    }

    @Test
    @DisplayName("verifyingWrongPasswordFails")
    void verifyingWrongPasswordFails() {
        char[] password = "vivaPassword2026!".toCharArray();
        String hash = PasswordHasher.hash(password);

        assertFalse(PasswordHasher.verify("wrongPassword".toCharArray(), hash));
        assertFalse(PasswordHasher.verify("".toCharArray(), hash));
    }

    @Test
    @DisplayName("twoHashesOfSamePasswordDifferDueToSalt")
    void twoHashesOfSamePasswordDifferDueToSalt() {
        char[] password = "reusedPasswordAcrossUsers".toCharArray();
        String hash1 = PasswordHasher.hash(password);
        String hash2 = PasswordHasher.hash(password);

        assertNotEquals(hash1, hash2, "Per-user 16-byte random salt must ensure hashes never match");
        assertTrue(PasswordHasher.verify(password, hash1));
        assertTrue(PasswordHasher.verify(password, hash2));
    }
}
