package io.github.jenboc.smite_build_api.ollama;

import java.util.List;

public record EmbedResponse(String model, List<List<Double>> embeddings) {}
