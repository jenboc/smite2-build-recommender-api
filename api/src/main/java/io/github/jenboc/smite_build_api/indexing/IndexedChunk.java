package io.github.jenboc.smite_build_api.indexing;

import java.util.List;
import java.util.Map;

/**
 * Represents an index chunk
 * @param text the chunk's text
 * @param metadata the chunk's metadata
 * @param embedding the chunk text's embedding vector
 */
public record IndexedChunk(String text, Map<String, String> metadata, List<Double> embedding) {}
