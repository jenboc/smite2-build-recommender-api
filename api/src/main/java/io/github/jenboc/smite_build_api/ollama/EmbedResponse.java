package io.github.jenboc.smite_build_api.ollama;

import java.util.List;

/**
 * DTO for Ollama's response to an embed request
 */
public record EmbedResponse(String model, List<List<Double>> embeddings) {}
