package io.github.jenboc.smite_build_api.retrieval;

import java.util.List;

import io.github.jenboc.smite_build_api.indexing.IndexedChunk;

public interface LimitedRetriever<T> extends Retriever<T> {
    /**
     * Retrieve the most relevant chunks to a query
     * @param query the query determining how "relevant" a chunk is
     * @param limit how many chunks to return
     * @returns the relevant chunks
     */
    public List<IndexedChunk> retrieve(T query, int limit);

    /**
     * Retrieve 5 chunks using the query
     * @param query the query determining how "relevant" a chunk is
     * @returns the 5 most relevant chunks to the query
     */
    @Override
    public default List<IndexedChunk> retrieve(T query) {
        return retrieve(query, 10);
    }
}
