package io.github.jenboc.smite_build_api.indexing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tools.jackson.databind.json.JsonMapper;

class IndexStorageTests {

    // Normal JsonMapper can be used for IndexFile I/O operations
    private final IndexStorage indexStorage = new IndexStorage(JsonMapper.builder().build());

    @Test
    void correctWriteLoadRoundTrip(@TempDir Path tempDir) {
        Path indexPath = tempDir.resolve("index.json");
        IndexFile original = new IndexFile(
                "patch",
                "2026-10-02T00:00:00Z",
                List.of(
                    new IndexedChunk("text", Map.of("type", "item"), List.of(1.0))
                )
        );

        indexStorage.writeIndex(original, indexPath);
        Optional<IndexFile> loaded = indexStorage.loadIndex(indexPath);

        assertTrue(loaded.isPresent());
        assertEquals(original, loaded.get());
    }

    @Test
    void loadReturnsEmptyWhenFileDoesNotExist(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("fake.json");
        assertEquals(Optional.empty(), indexStorage.loadIndex(missing));
    }

    @Test
    void loadReturnsEmptyAndDoesNotThrowWhenFileIsCorrupt(@TempDir Path tempDir) throws IOException {
        Path corrupt = tempDir.resolve("corrupt.json");
        Files.writeString(corrupt, "{ corrupt json ]");

        assertEquals(Optional.empty(), indexStorage.loadIndex(corrupt));
    }

    @Test
    void writeCreatesParentDirectoriesIfMissing(@TempDir Path tempDir) {
        Path nested = tempDir.resolve("subdir1/subdir2/subdir3/index.json");
        IndexFile index = new IndexFile(
                "patch",
                "2026-10-02T00:00:00Z",
                List.of(
                    new IndexedChunk("text", Map.of("type", "item"), List.of(1.0))
                )
        );

        indexStorage.writeIndex(index, nested);
        assertTrue(Files.exists(nested));
    }
}
