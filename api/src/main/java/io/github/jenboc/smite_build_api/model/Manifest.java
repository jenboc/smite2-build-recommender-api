package io.github.jenboc.smite_build_api.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the scraped dataset's manifest
 */
@NoArgsConstructor
@AllArgsConstructor
public class Manifest {
    private @Getter @Setter String patch;
    private @Getter @Setter String pulledAt;
    private @Getter @Setter String source;
    private @Getter @Setter Map<String, Integer> counts;
    private @Getter @Setter List<Entry> items;
    private @Getter @Setter List<Entry> gods;

    /**
     * Represents a single entry in the manifest
     */
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entry {
        private @Getter @Setter String name;
        private @Getter @Setter String file;
        private @Getter @Setter String sha256;
    }
}
