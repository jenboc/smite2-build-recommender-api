package io.github.jenboc.smite_build_api.recommending;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.ollama.OllamaClient;
import io.github.jenboc.smite_build_api.retrieval.Retriever;


@ExtendWith(MockitoExtension.class)
class RecommenderTests {

    @Mock OllamaClient ollamaClient;
    @Mock RecommendationPromptBuilder promptBuilder;
    @Mock Retriever retriever;

    private Recommender recommender;

    @BeforeEach
    void setUp() {
        recommender = new Recommender(ollamaClient, promptBuilder, retriever);
    }

    private void setupMockCalls(
            String userQuery,
            List<IndexedChunk> retrievedChunks,
            String returnedPrompt,
            String generatedResponse
    ) {
        when(retriever.retrieve(eq(userQuery), anyInt()))
            .thenReturn(retrievedChunks);

        when(promptBuilder.buildPrompt(eq(userQuery), any()))
            .thenReturn(returnedPrompt);

        when(ollamaClient.generate(any()))
            .thenReturn(generatedResponse);
    }

    @Test
    void usesRetrievedChunksToBuildPrompts() {
        // Ensure that we're not just using the prompts given directly
        List<IndexedChunk> toRetrieve = List.of(
                new IndexedChunk("RETRIEVED", Map.of(), List.of(1.0))
        );

        setupMockCalls("query", toRetrieve, "prompt", "response");
        recommender.queryForRecommendation("query");

        verify(promptBuilder).buildPrompt("query", toRetrieve);
    }
 
    @Test
    void usesGeneratedPromptToGenerate() {
        // Ensure that we're actually making use of the prompt builder
        // rather than just passing the userQuery as is
        setupMockCalls("query", List.of(), "GENERATED_PROMPT", "response");
        recommender.queryForRecommendation("query");

        verify(ollamaClient).generate("GENERATED_PROMPT");
    }
   
    @Test
    void returnsGeneratedRecommendation() {
        // Ensure that we actually return the ollama result
        setupMockCalls("query", List.of(), "prompt", "GENERATED_RESPONSE");
        String res = recommender.queryForRecommendation("query");

        assertEquals(res, "GENERATED_RESPONSE");
    }

    @Test
    void propagatesRetrievalExceptions() {
        when(retriever.retrieve(eq("query"), anyInt()))
            .thenThrow(new RuntimeException("Retrieval Exception"));

        assertThrows(RuntimeException.class,
                () -> recommender.queryForRecommendation("query"),
                "Retrieval Exception");
    }

    @Test
    void propagatesOllamaExceptions() {
        when(retriever.retrieve(eq("query"), anyInt()))
            .thenReturn(List.of());

        when(promptBuilder.buildPrompt(eq("query"), any()))
            .thenReturn("prompt");

        when(ollamaClient.generate(any()))
            .thenThrow(new RuntimeException("Ollama Exception"));

        assertThrows(RuntimeException.class,
                () -> recommender.queryForRecommendation("query"),
                "Ollama Exception");
    }
}
