package com.paperlessworld.model;

import com.paperlessworld.exception.IllegalStateTransitionException;
import com.paperlessworld.exception.InvalidClosureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class ThoughtStateMachineTest {

    @Test
    @DisplayName("everyAllowedTransitionSucceeds")
    void everyAllowedTransitionSucceeds() {
        // INBOX -> OPEN
        assertTrue(ThoughtState.INBOX.canTransitionTo(ThoughtState.OPEN));
        assertDoesNotThrow(() -> ThoughtState.INBOX.requireTransitionTo(ThoughtState.OPEN));

        // INBOX -> CLOSED
        assertTrue(ThoughtState.INBOX.canTransitionTo(ThoughtState.CLOSED));
        assertDoesNotThrow(() -> ThoughtState.INBOX.requireTransitionTo(ThoughtState.CLOSED));

        // OPEN -> CLOSED
        assertTrue(ThoughtState.OPEN.canTransitionTo(ThoughtState.CLOSED));
        assertDoesNotThrow(() -> ThoughtState.OPEN.requireTransitionTo(ThoughtState.CLOSED));

        // CLOSED -> OPEN (reopening)
        assertTrue(ThoughtState.CLOSED.canTransitionTo(ThoughtState.OPEN));
        assertDoesNotThrow(() -> ThoughtState.CLOSED.requireTransitionTo(ThoughtState.OPEN));

        // Self-transitions
        for (ThoughtState state : ThoughtState.values()) {
            assertTrue(state.canTransitionTo(state));
            assertDoesNotThrow(() -> state.requireTransitionTo(state));
        }
    }

    @Test
    @DisplayName("everyDisallowedTransitionThrowsIllegalStateTransitionException")
    void everyDisallowedTransitionThrowsIllegalStateTransitionException() {
        // OPEN -> INBOX is forbidden
        assertFalse(ThoughtState.OPEN.canTransitionTo(ThoughtState.INBOX));
        assertThrows(IllegalStateTransitionException.class, 
                () -> ThoughtState.OPEN.requireTransitionTo(ThoughtState.INBOX));

        // CLOSED -> INBOX is forbidden
        assertFalse(ThoughtState.CLOSED.canTransitionTo(ThoughtState.INBOX));
        assertThrows(IllegalStateTransitionException.class, 
                () -> ThoughtState.CLOSED.requireTransitionTo(ThoughtState.INBOX));

        // Target null is forbidden
        assertFalse(ThoughtState.INBOX.canTransitionTo(null));
        assertThrows(IllegalStateTransitionException.class, 
                () -> ThoughtState.INBOX.requireTransitionTo(null));
    }

    @Test
    @DisplayName("closingWithoutReasonIsRejected")
    void closingWithoutReasonIsRejected() {
        Thought thought = new QuestionThought(1L, "tok-2", "Raft vs Paxos", "details", 4);
        thought.keep(Instant.now());

        assertThrows(InvalidClosureException.class, () -> thought.close(null, Instant.now()));
        assertEquals(ThoughtState.OPEN, thought.getState());
        assertNull(thought.getClosureReason());
    }

    @Test
    @DisplayName("validClosureSetsStateAndReasonAndTouchTime")
    void validClosureSetsStateAndReasonAndTouchTime() {
        Thought thought = new IdeaThought(1L, "tok-1", "Pool leak", "check HikariCP", 3);
        thought.keep(Instant.now());

        Instant touchTime = Instant.now();
        thought.close(ClosureReason.DONE, touchTime);

        assertEquals(ThoughtState.CLOSED, thought.getState());
        assertEquals(ClosureReason.DONE, thought.getClosureReason());
        assertEquals(touchTime, thought.getLastTouchedAt());
        assertEquals(touchTime, thought.getClosedAt());
    }

    @Test
    @DisplayName("reopeningClosedThoughtClearsClosureReasonAndSetsStateOpen")
    void reopeningClosedThoughtClearsClosureReasonAndSetsStateOpen() {
        Thought thought = new NoteThought(1L, "tok-3", "Classloader isolation", "notes", 3);
        thought.keep(Instant.now());
        thought.close(ClosureReason.LET_GO, Instant.now());

        assertEquals(ThoughtState.CLOSED, thought.getState());
        assertEquals(ClosureReason.LET_GO, thought.getClosureReason());

        Instant reopenTime = Instant.now();
        thought.reopen(reopenTime);

        assertEquals(ThoughtState.OPEN, thought.getState());
        assertNull(thought.getClosureReason());
        assertNull(thought.getClosedAt());
        assertEquals(reopenTime, thought.getLastTouchedAt());
    }
}
