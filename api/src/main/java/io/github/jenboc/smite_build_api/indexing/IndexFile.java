package io.github.jenboc.smite_build_api.indexing;

import java.util.List;

/**
 * Represents the content of the index file (index.json)
 * @param sourcePatch which game patch the file was built for
 * @param builtAt the time when the file was built
 * @param chunks the chunks contained in the file
 */
public record IndexFile(String sourcePatch, String builtAt, List<IndexedChunk> chunks) {}
