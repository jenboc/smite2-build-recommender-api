package io.github.jenboc.smite_build_api.ollama;

public record GenerateRequest(String model, String prompt, boolean stream) {}
