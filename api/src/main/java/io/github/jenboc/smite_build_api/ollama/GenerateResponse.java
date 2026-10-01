package io.github.jenboc.smite_build_api.ollama;

public record GenerateResponse(String model, String response, boolean done) {}
