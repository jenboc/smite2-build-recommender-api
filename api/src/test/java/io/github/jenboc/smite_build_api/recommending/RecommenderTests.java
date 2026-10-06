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
import io.github.jenboc.smite_build_api.retrieval.VectorRetriever;


@ExtendWith(MockitoExtension.class)
class RecommenderTests {

    @Mock OllamaClient ollamaClient;
    @Mock VectorRetriever retriever;

    private Recommender recommender;

    @BeforeEach
    void setUp() {
        recommender = new Recommender(ollamaClient, retriever);
    }

    private void setupMockCalls(
            String userQuery,
            List<IndexedChunk> retrievedChunks,
            String generatedResponse
    ) {
        when(ollamaClient.embed(any()))
            .thenReturn(List.of(List.of(1.0)));

        when(retriever.retrieve(any(), anyInt()))
            .thenReturn(retrievedChunks);

        when(ollamaClient.generate(any()))
            .thenReturn(generatedResponse);
    }

    @Test
    void returnsGeneratedRecommendation() {
        // Ensure that we actually return the ollama result
        setupMockCalls("query", List.of(), "GENERATED_RESPONSE");
        String res = recommender.queryForRecommendation("query");

        assertEquals(res, "GENERATED_RESPONSE");
    }

    @Test
    void propagatesRetrievalExceptions() {
        when(ollamaClient.embed(any()))
            .thenReturn(List.of(List.of(1.0)));

        when(retriever.retrieve(any(), anyInt()))
            .thenThrow(new RuntimeException("Retrieval Exception"));

        assertThrows(RuntimeException.class,
                () -> recommender.queryForRecommendation("query"),
                "Retrieval Exception");
    }

    @Test
    void propagatesOllamaExceptions() {
        when(ollamaClient.embed(any()))
            .thenReturn(List.of(List.of(1.0)));

        when(retriever.retrieve(any(), anyInt()))
            .thenReturn(List.of());

        when(ollamaClient.generate(any()))
            .thenThrow(new RuntimeException("Ollama Exception"));

        assertThrows(RuntimeException.class,
                () -> recommender.queryForRecommendation("query"),
                "Ollama Exception");
    }
}
