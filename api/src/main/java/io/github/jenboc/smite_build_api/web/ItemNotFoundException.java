package io.github.jenboc.smite_build_api.web;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String name) {
        super("No item found with name: " + name);
    }
}
