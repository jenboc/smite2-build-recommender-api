package io.github.jenboc.smite_build_api.recommending;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.jenboc.smite_build_api.indexing.IndexedChunk;

class RecommendationPromptBuilderTests {

    private final RecommendationPromptBuilder builder = new RecommendationPromptBuilder();

    private final List<IndexedChunk> testChunks = List.of(
            new IndexedChunk(
                "Chunk 1",
                Map.of(),
                List.of(1.0)
            ),
            new IndexedChunk(
                "Chunk 2",
                Map.of(),
                List.of(2.0)
            )
    );

    @Test
    void promptContainsPreamble() {
        String prompt = builder.buildPrompt("query", testChunks);
        assertTrue(prompt.contains(builder.getPreamble()),
                "got: " + prompt);
    }

    @Test
    void promptContainsInstructions() {
        String prompt = builder.buildPrompt("query", testChunks);
        assertTrue(prompt.contains(builder.getInstructions()),
                "got: " + prompt);
    }

    @Test
    void promptContainsUserQuery() {
        String prompt = builder.buildPrompt("THIS IS A USER QUERY", testChunks);
        assertTrue(prompt.contains("User request:\nTHIS IS A USER QUERY"),
                "got: " + prompt);
    }

    @Test
    void promptContainsRetrievedContext() {
        String prompt = builder.buildPrompt("query", testChunks);
        assertTrue(prompt.contains("Retrieved context:\nChunk 1\n\nChunk 2"),
                "got: " + prompt);
    }

    @Test
    void doesNotThrowOnEmptyContext() {
        assertDoesNotThrow(() -> builder.buildPrompt("query", List.of()));
    }
    
    @Test
    void doesNotThrowOnNullContext() {
        assertDoesNotThrow(() -> builder.buildPrompt("query", null));
    }

    @Test
    void doesNotThrowOnNullQuery() {
        assertDoesNotThrow(() -> builder.buildPrompt(null, testChunks));
    }

    @Test
    void doesNotThrowOnEmptyQuery() {
        assertDoesNotThrow(() -> builder.buildPrompt("", testChunks));
    }

}
