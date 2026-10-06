package io.github.jenboc.smite_build_api.retrieval;

import java.util.List;

import io.github.jenboc.smite_build_api.indexing.IndexedChunk;

/**
 * Represents the API of an object which facilitates chunk retrieval using
 * some object as reference.
 */
public interface Retriever<T> {

    /**
     * Retrieve relevant chunks
     * @param query the object which determines what is relevant
     */
    public List<IndexedChunk> retrieve(T query);
}
