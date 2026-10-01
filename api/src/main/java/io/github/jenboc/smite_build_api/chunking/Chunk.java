package io.github.jenboc.smite_build_api.chunking;

import java.util.Map;

/**
 * Represents a single chunk.
 *
 * <p>A chunk is a piece of text, alongside some metadata, which describes
 * an object</p>
 */
public record Chunk(String text, Map<String, String> metadata) {
}
