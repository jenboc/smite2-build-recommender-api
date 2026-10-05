package io.github.jenboc.smite_build_api.decoding;

import org.springframework.stereotype.Service;

import io.github.jenboc.smite_build_api.ollama.OllamaClient;

/**
 * Responsible for the decoding of a raw string query
 */
@Service 
public class QueryDecoder {

    private final OllamaClient ollamaClient;

    public QueryDecoder(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    /**
     * Decode a raw query
     * @param query the raw query
     * @returns the decoded query
     */
    public DecodedQuery decode(String query) {
        return new DecodedQuery();
    }
}
