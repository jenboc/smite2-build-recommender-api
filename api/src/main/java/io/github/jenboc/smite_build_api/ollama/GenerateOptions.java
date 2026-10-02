package io.github.jenboc.smite_build_api.ollama;

/**
 * DTO representing generation options
 * @param num_ctx the number of tokens in the context window
 */
public record GenerateOptions(
        int num_ctx
) {}
