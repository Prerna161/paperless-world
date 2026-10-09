package com.paperlessworld.model;

/**
 * Functional category of a Thought.
 * Encapsulated within a single relational table.
 */
public enum ThoughtType {
    IDEA("Creative sparks, project concepts, potential experiments"),
    NOTE("Factual information, summaries, concepts"),
    QUESTION("Unresolved inquiries requiring investigation"),
    JOURNAL("Reflections and subjective logs"),
    REFERENCE("Documentation links, quotes, and static bookmarks");

    private final String description;

    ThoughtType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
