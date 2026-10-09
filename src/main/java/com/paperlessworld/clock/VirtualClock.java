package com.paperlessworld.clock;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Interface decoupling time retrieval across the application.
 * Enables live simulation of days/weeks passing during viva demonstrations.
 */
public interface VirtualClock {
    Instant now();
    LocalDate today();
    int getOffsetDays();
    void setOffsetDays(int offsetDays);
    void advanceDays(int days);
    void reset();
}
