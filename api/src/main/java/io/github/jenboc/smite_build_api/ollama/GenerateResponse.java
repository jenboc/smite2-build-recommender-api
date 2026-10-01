package io.github.jenboc.smite_build_api.ollama;

/**
 * DTO representing Ollama's response to a generation request
 */
public record GenerateResponse(String model, String response, boolean done) {}
