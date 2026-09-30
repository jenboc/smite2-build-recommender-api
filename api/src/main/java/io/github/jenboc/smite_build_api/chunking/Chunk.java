package io.github.jenboc.smite_build_api.chunking;

import java.util.Map;

public record Chunk(String text, Map<String, String> metadata) {
}
