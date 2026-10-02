package io.github.jenboc.smite_build_api.web;

public class GodNotFoundException extends RuntimeException {
    public GodNotFoundException(String name) {
        super("No god found with name: " + name);
    }
}
