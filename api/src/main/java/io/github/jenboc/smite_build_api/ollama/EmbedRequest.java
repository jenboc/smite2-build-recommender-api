package io.github.jenboc.smite_build_api.ollama;

import java.util.List;

/**
 * The DTO for a request to Ollama for the embedding of strings
 */
public record EmbedRequest(String model, List<String> input) {}
