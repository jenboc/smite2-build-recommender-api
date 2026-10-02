package io.github.jenboc.smite_build_api.indexing;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Receives a list of indexed chunks (most likely from the IndexStartupRunner)
 * and exposes it as a Spring Boot Service.
 * @see IndexStartupRunner
 */
@Service
public class IndexContainer {

    private List<IndexedChunk> chunks = List.of();

    /**
     * Set the stored list of indexed chunks
     * @param chunks the new list of indexed chunks
     */
    public void setChunks(List<IndexedChunk> chunks) {
        this.chunks = chunks;
    }

    /**
     * Get the stored list of indexed chunks
     * @returns the stored list of indexed chunks
     */
    public List<IndexedChunk> getChunks() {
        return chunks;
    }
}
