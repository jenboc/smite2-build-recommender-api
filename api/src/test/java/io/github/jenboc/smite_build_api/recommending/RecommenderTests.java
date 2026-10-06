package io.github.jenboc.smite_build_api.recommending;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jenboc.smite_build_api.decoding.DecodedQuery;
import io.github.jenboc.smite_build_api.decoding.QueryDecoder;
import io.github.jenboc.smite_build_api.decoding.QueryType;
import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.ollama.OllamaClient;


@ExtendWith(MockitoExtension.class)
class RecommenderTests {

    @Mock QueryDecoder queryDecoder;
    @Mock ContextGatherer gatherer;
    @Mock OllamaClient ollamaClient;

    private Recommender recommender;

    @BeforeEach
    void setUp() {
        recommender = new Recommender(queryDecoder, gatherer, ollamaClient);
    }

    private void setupMockCalls(
            String userQuery,
            List<IndexedChunk> retrievedChunks,
            String generatedResponse
    ) {
        when(queryDecoder.decode(any()))
            .thenReturn(new DecodedQuery(
                QueryType.BUILD_RECOMMENDATION,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                userQuery
            ));

        when(gatherer.gather(any()))
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
    void propagatesGathererExceptions() {
        when(gatherer.gather(any()))
            .thenThrow(new RuntimeException("Gathering Exception"));

        assertThrows(RuntimeException.class,
                () -> recommender.queryForRecommendation("query"),
                "Retrieval Exception");
    }

    @Test
    void propagatesOllamaExceptions() {
        when(gatherer.gather(any()))
            .thenReturn(List.of());

        lenient().when(ollamaClient.generate(any()))
            .thenThrow(new RuntimeException("Ollama Exception"));

        assertThrows(RuntimeException.class,
                () -> recommender.queryForRecommendation("query"),
                "Ollama Exception");
    }
}
