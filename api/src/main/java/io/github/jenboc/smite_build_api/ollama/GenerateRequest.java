package io.github.jenboc.smite_build_api.ollama;

/**
 * DTO representing a request to Ollama to generate text using an LLM
 * @param model name of the model to use for generation
 * @param prompt the raw string of the LLM prompt
 * @param options generation options
 * @param stream should the response be streamed?
 */
public record GenerateRequest(
        String model,
        String prompt,
        GenerateOptions options,
        boolean stream
) {}
