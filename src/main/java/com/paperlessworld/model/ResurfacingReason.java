package com.paperlessworld.model;

/**
 * Immutable record representing a firing resurfacing rule evaluation result.
 * Thread-safe value object according to PRD Section 10.5.
 */
public record ResurfacingReason(String ruleCode, String text, int daysNeglected) {
    public ResurfacingReason {
        if (ruleCode == null || text == null) {
            throw new IllegalArgumentException("ruleCode and text cannot be null");
        }
    }
}
