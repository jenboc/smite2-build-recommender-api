package io.github.jenboc.smite_build_api.indexing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.springframework.stereotype.Component;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Handles the I/O of IndexFile data
 */
@Component 
public class IndexStorage {
    
    private final JsonMapper jsonMapper;

    public IndexStorage(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * Attempt to load the index file
     * @returns the index file content
     */
    public Optional<IndexFile> loadIndex(Path indexPath) {
        if (!Files.exists(indexPath)) {
            return Optional.empty();
        }

        try (InputStream in = Files.newInputStream(indexPath)) {
            return Optional.of(jsonMapper.readValue(in, IndexFile.class));
        } catch (IOException | StreamReadException e) {
            System.err.printf("Could not read index as %s, treating as missing: %s\n",
                indexPath, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Attempt to write the index file
     */
    public void writeIndex(IndexFile index, Path indexPath) {
        try {
            Path parent = indexPath.getParent();

            // Create parent directories (if applicable)
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (OutputStream out = Files.newOutputStream(indexPath)) {
                jsonMapper.writeValue(out, index);
            }
        } catch (IOException e) {
            throw new IndexWriteException(
                "Failed to write index to: " + indexPath.toAbsolutePath(),
                e
            );
        }
    }
}
