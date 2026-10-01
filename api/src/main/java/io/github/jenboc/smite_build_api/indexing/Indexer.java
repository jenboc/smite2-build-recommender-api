package io.github.jenboc.smite_build_api.indexing;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jenboc.smite_build_api.chunking.Chunk;
import io.github.jenboc.smite_build_api.chunking.GodChunker;
import io.github.jenboc.smite_build_api.chunking.ItemChunker;
import io.github.jenboc.smite_build_api.loading.DataLoader;
import io.github.jenboc.smite_build_api.ollama.OllamaClient;

/**
 * Responsible for indexing the chunks generated from the scraped data
 */
@Component
public class Indexer {

    private final DataLoader dataLoader;
    private final GodChunker godChunker;
    private final ItemChunker itemChunker;
    private final OllamaClient ollamaClient;

    public Indexer(
            DataLoader dataLoader,
            GodChunker godChunker,
            ItemChunker itemChunker,
            OllamaClient ollamaClient
    ) {
        this.dataLoader = dataLoader;
        this.godChunker = godChunker;
        this.itemChunker = itemChunker;
        this.ollamaClient = ollamaClient;
    }

    /**
     * Builds an index file for a given patch, using the scraped data
     * @param sourcePatch the game patch id
     * @returns the index file content
     */
    public IndexFile buildIndexFile(String sourcePatch) {
        List<IndexedChunk> chunks = new ArrayList<>();

        dataLoader.getAllGods().forEach(god ->
            chunks.addAll(embedAll(godChunker.chunk(god))));

        dataLoader.getAllItems().forEach(item ->
            chunks.addAll(embedAll(itemChunker.chunk(item))));
        
        return new IndexFile(
            sourcePatch,
            Instant.now().toString(),
            chunks
        );
    }

    private List<IndexedChunk> embedAll(List<Chunk> chunks) {
        // Avoid an empty API call
        if (chunks.isEmpty()) {
            return List.of();
        }

        // Call the API using the text fields of the chunks
        List<String> strings = chunks.stream().map(Chunk::text).toList();
        List<List<Double>> vectors = ollamaClient.embed(strings);

        // Ensure we received the correct number of vectors back
        // (1 vector per string)
        if (vectors.size() != strings.size()) {
            throw new IllegalStateException(
                "Embedding count mismatch: sent " + strings.size() + " strings "
                + "but only received " + vectors.size() + " vectors"
            );
        }

        // Convert chunks to indexedChunks using the returned vectors
        List<IndexedChunk> indexedChunks = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            indexedChunks.add(new IndexedChunk(
                chunks.get(i).text(),
                chunks.get(i).metadata(),
                vectors.get(i)
            ));
        }

        return indexedChunks;
    }
}
