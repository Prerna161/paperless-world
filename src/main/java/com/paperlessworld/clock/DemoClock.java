package com.paperlessworld.clock;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe VirtualClock implementation used for live viva demos and time simulation.
 */
public class DemoClock implements VirtualClock {
    private static final DemoClock INSTANCE = new DemoClock();
    private final AtomicInteger offsetDays = new AtomicInteger(0);

    public DemoClock() {}

    public static DemoClock getInstance() {
        return INSTANCE;
    }

    @Override
    public Instant now() {
        return Instant.now().plus(offsetDays.get(), ChronoUnit.DAYS);
    }

    @Override
    public LocalDate today() {
        return LocalDate.ofInstant(now(), ZoneId.systemDefault());
    }

    @Override
    public int getOffsetDays() {
        return offsetDays.get();
    }

    @Override
    public void setOffsetDays(int days) {
        this.offsetDays.set(days);
    }

    @Override
    public void advanceDays(int days) {
        this.offsetDays.addAndGet(days);
    }

    @Override
    public void reset() {
        this.offsetDays.set(0);
    }

    /**
     * Compute virtual instant given an explicit user offset in days.
     */
    public static Instant calculateVirtualInstant(int offset) {
        return Instant.now().plus(offset, ChronoUnit.DAYS);
    }

    public static LocalDate calculateVirtualToday(int offset) {
        return LocalDate.ofInstant(calculateVirtualInstant(offset), ZoneId.systemDefault());
    }
}
