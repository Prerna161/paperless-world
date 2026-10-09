package com.paperlessworld.util;

import com.paperlessworld.model.ThoughtType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShapeSuggesterTest {

    @Test
    @DisplayName("ShapeSuggester identifies QUESTION from trailing question mark")
    void testQuestionDetection() {
        assertEquals(ThoughtType.QUESTION, ShapeSuggester.suggestType("How does connection pooling handle eviction?"));
    }

    @Test
    @DisplayName("ShapeSuggester identifies IDEA from creative verbs like 'what if' and 'explore'")
    void testIdeaDetection() {
        assertEquals(ThoughtType.IDEA, ShapeSuggester.suggestType("Explore WebAssembly for embedded indexing"));
        assertEquals(ThoughtType.IDEA, ShapeSuggester.suggestType("What if we ran SQLite on client workers"));
    }

    @Test
    @DisplayName("ShapeSuggester defaults to NOTE for general statements")
    void testNoteDefault() {
        assertEquals(ThoughtType.NOTE, ShapeSuggester.suggestType("Tomcat 10.1 classloader notes"));
    }
}
