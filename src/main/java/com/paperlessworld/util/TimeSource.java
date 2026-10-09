package com.paperlessworld.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Time abstraction matching PRD Section 9 and 10.5.
 * Production uses the system clock; Demo mode applies an offset stored per user.
 */
public interface TimeSource {
    Instant now();
    LocalDate today();

    static TimeSource system() {
        return new TimeSource() {
            @Override
            public Instant now() { return Instant.now(); }
            @Override
            public LocalDate today() { return LocalDate.now(ZoneId.of("UTC")); }
        };
    }

    static TimeSource withOffsetDays(int offsetDays) {
        return new TimeSource() {
            @Override
            public Instant now() {
                return Instant.now().plus(offsetDays, ChronoUnit.DAYS);
            }
            @Override
            public LocalDate today() {
                return LocalDate.ofInstant(now(), ZoneId.of("UTC"));
            }
        };
    }
}
