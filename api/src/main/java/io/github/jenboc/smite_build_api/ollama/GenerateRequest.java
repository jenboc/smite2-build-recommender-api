package io.github.jenboc.smite_build_api.ollama;

/**
 * DTO representing a request to Ollama to generate text using an LLM
 */
public record GenerateRequest(String model, String prompt, boolean stream) {}
