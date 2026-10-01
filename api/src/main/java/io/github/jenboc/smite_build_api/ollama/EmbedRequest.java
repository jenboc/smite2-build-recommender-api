package io.github.jenboc.smite_build_api.ollama;

import java.util.List;

public record EmbedRequest(String model, List<String> input) {}
