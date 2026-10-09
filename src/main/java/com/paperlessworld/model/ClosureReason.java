package com.paperlessworld.model;

/**
 * Mandatory reason required whenever an Open Loop is transitioned to CLOSED.
 * Prevents notes from silently disappearing into vague archive or trash bins.
 */
public enum ClosureReason {
    DONE("Acted upon and completed"),
    LET_GO("Deliberately abandoned or dismissed"),
    RECORDED("Filed away as reference; no further action required");

    private final String description;

    ClosureReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
