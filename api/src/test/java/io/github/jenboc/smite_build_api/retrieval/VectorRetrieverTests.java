package io.github.jenboc.smite_build_api.retrieval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jenboc.smite_build_api.indexing.IndexContainer;
import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.ollama.OllamaClient;

@ExtendWith(MockitoExtension.class)
class VectorRetrieverTests {

    @Mock private OllamaClient ollamaClient;
    @Mock private IndexContainer indexContainer;

    private VectorRetriever retriever;

    @BeforeEach
    void setUp() {
        retriever = new VectorRetriever(ollamaClient, indexContainer);
    }

    private IndexedChunk chunk(String text, List<Double> vector) {
        return new IndexedChunk(text, Map.of("type", "test"), vector);
    }

    @Test
    void returnsChunksInDescendingSimilarity() {
        List<Double> queryVector = List.of(1.0, 0.0);

        IndexedChunk exact = chunk("exact", queryVector);
        IndexedChunk partial = chunk("partial", List.of(0.7, 0.7));
        IndexedChunk opposite = chunk("opposite", List.of(-1.0, 0.0));

        when(indexContainer.getChunks()).thenReturn(List.of(partial, opposite, exact));

        List<IndexedChunk> res = retriever.retrieve(queryVector, 3);
        assertEquals(List.of(exact, partial, opposite), res);
    }

    @Test
    void respectsTopKLimit() {
        List<Double> queryVector = List.of(1.0, 0.0);

        IndexedChunk exact = chunk("exact", queryVector);
        IndexedChunk partial = chunk("partial", List.of(0.7, 0.7));
        IndexedChunk opposite = chunk("opposite", List.of(-1.0, 0.0));

        when(indexContainer.getChunks()).thenReturn(List.of(partial, opposite, exact));

        List<IndexedChunk> res = retriever.retrieve(queryVector, 2);
        assertEquals(List.of(exact, partial), res);
    }

    @Test
    void returnsAllIfKIsLargerThanLength() {
        List<Double> queryVector = List.of(1.0, 0.0);

        IndexedChunk exact = chunk("exact", queryVector);
        IndexedChunk partial = chunk("partial", List.of(0.7, 0.7));
        IndexedChunk opposite = chunk("opposite", List.of(-1.0, 0.0));

        when(indexContainer.getChunks()).thenReturn(List.of(partial, opposite, exact));

        List<IndexedChunk> res = retriever.retrieve(queryVector, 5000);
        assertEquals(List.of(exact, partial, opposite), res);
    }

    @Test
    void returnsEmptyListWhenIndexIsEmptyWithNoEmbedCall() {
        when(indexContainer.getChunks()).thenReturn(List.of());

        List<IndexedChunk> res = retriever.retrieve(List.of(1.0), 10);
        assertTrue(res.isEmpty());
    }

    @Test
    void embedsTheQueryExactlyOnceIfIndexNonEmpty() {
        List<Double> queryVector = List.of(1.0, 0.0);

        IndexedChunk exact = chunk("exact", queryVector);
        IndexedChunk partial = chunk("partial", List.of(0.7, 0.7));
        IndexedChunk opposite = chunk("opposite", List.of(-1.0, 0.0));

        when(indexContainer.getChunks()).thenReturn(List.of(partial, opposite, exact));

        retriever.retrieve(queryVector, 10);
    }
}
