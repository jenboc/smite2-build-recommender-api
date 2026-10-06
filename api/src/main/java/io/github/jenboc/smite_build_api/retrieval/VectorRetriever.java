package io.github.jenboc.smite_build_api.retrieval;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.jenboc.smite_build_api.indexing.IndexContainer;
import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.ollama.OllamaClient;

/**
 * Retrieves chunks using SimilaritySearch.cosineSimilarity and embedding vectors
 * @see SimilaritySearch
 */
@Service
public class VectorRetriever implements LimitedRetriever<List<Double>> {

    private final OllamaClient ollamaClient;
    private final IndexContainer indexContainer;

    public VectorRetriever(
            OllamaClient ollamaClient,
            IndexContainer indexContainer
    ) {
        this.ollamaClient = ollamaClient;
        this.indexContainer = indexContainer;
    }

    /**
     * Retrieve the top k similar chunks to a query
     * @param query the query to compare each chunk to
     * @param topK how many chunks to return
     * @returns the top k most similar chunks
     */
    public List<IndexedChunk> retrieve(List<Double> query, int topK) {
        // Don't waste time embedding if index is empty
        // We're going to return an empty list regardless.
        if (indexContainer.getChunks() == null || indexContainer.getChunks().isEmpty()) {
            return List.of();
        }

        return indexContainer.getChunks().stream()
            // Calculate similarity rating between query and each chunk
            .map(chunk -> Map.entry(chunk, SimilaritySearch.cosineSimilarity(chunk.embedding(), query)))
            // Sort by rating (the map value)
            // Descending since:
            // - cos(theta) = 1 => parallel => more similar
            // - cos(theta) = 0 => perpendicular
            // - cos(theta) = -1 => opposite
            .sorted(Map.Entry.<IndexedChunk, Double>comparingByValue().reversed())
            // Take the topK chunks
            .limit(topK)
            .map(Map.Entry::getKey)
            .toList();
    }

    public List<IndexedChunk> retrieveByString(String query, int topK) {
        if (indexContainer.getChunks() == null || indexContainer.getChunks().isEmpty()) {
            return List.of();
        }

        List<Double> vec = ollamaClient.embed(List.of(query)).get(0);
        return retrieve(vec, topK);
    }
}
