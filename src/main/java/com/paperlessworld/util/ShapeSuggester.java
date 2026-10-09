package com.paperlessworld.util;

import com.paperlessworld.model.ThoughtType;

import java.util.Locale;

/**
 * Deterministic text pattern analyzer matching PRD Section 10.4.
 * Not pseudo-AI; simple, auditable rule set that suggests a ThoughtType.
 */
public final class ShapeSuggester {

    private ShapeSuggester() {}

    public static ThoughtType suggestType(String text) {
        if (text == null || text.isBlank()) {
            return ThoughtType.NOTE;
        }

        String trimmed = text.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);

        // 1. Trailing question mark suggests QUESTION
        if (trimmed.endsWith("?") || lower.startsWith("why ") || lower.startsWith("how to ") || lower.startsWith("what is ")) {
            return ThoughtType.QUESTION;
        }

        // 2. Verbs such as 'build', 'try', 'what if', 'explore' suggest IDEA
        if (lower.startsWith("what if") || lower.startsWith("try ") || 
            lower.startsWith("build ") || lower.startsWith("explore ") || 
            lower.startsWith("create ") || lower.contains("could we")) {
            return ThoughtType.IDEA;
        }

        // 3. Quotes, URLs, or bookmarks suggest REFERENCE
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("doi:") || (lower.startsWith("\"") && lower.endsWith("\""))) {
            return ThoughtType.REFERENCE;
        }

        // 4. Personal reflections suggest JOURNAL
        if (lower.startsWith("today i") || lower.startsWith("felt ") || lower.startsWith("reflection:")) {
            return ThoughtType.JOURNAL;
        }

        return ThoughtType.NOTE;
    }
}
