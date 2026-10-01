package io.github.jenboc.smite_build_api.indexing;

import java.nio.file.Path;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.github.jenboc.smite_build_api.loading.DataLoader;

/**
 * Ensures that we have access to indexed chunks of the data at start up,
 * be it by loading the index or rebuilding it.
 */
@Component
// We don't want this to run during tests
@Profile("!test")
public class IndexStartupRunner implements ApplicationRunner {
    
    private final DataLoader dataLoader;
    private final Indexer indexer;
    private final IndexStorage indexStorage;
    private final Path indexDir;

    public IndexStartupRunner(
            DataLoader dataLoader,
            Indexer indexer,
            IndexStorage indexStorage,
            @Value("${index.path}") String indexDir
    ) {
        this.dataLoader = dataLoader;
        this.indexer = indexer;
        this.indexStorage = indexStorage;
        this.indexDir = Path.of(indexDir);
    }

    @Override
    public void run(ApplicationArguments args) {
        String currentPatch = dataLoader.getManifest().getPatch();
        Path filePath = indexDir.resolve("index.json");
        
        // Check if index rebuild is forced via command line
        if (args.containsOption("index.rebuild")) {
            rebuildIndex("forced via --index.rebuild", currentPatch, filePath);
            return;
        }

        // Check if we need to rebuild due to stale or missing file
        Optional<IndexFile> existingIndex = indexStorage.loadIndex(filePath);
        boolean isStale = existingIndex.isEmpty() || !existingIndex.get().sourcePatch().equals(currentPatch);

        if (!isStale) {
            System.out.printf("Index up to date (patch %s), skipping rebuild.\n",
                currentPatch);
            return;
        }

        rebuildIndex("stale or missing", currentPatch, filePath);
    }

    private void rebuildIndex(String reason, String currentPatch, Path filePath) {
        System.out.printf("Rebuilding index (%s)...\n", reason);

        IndexFile fresh = indexer.buildIndexFile(currentPatch);
        indexStorage.writeIndex(fresh, filePath);

        System.out.printf("Index rebuilt: %d chunks, patch %s\n",
            fresh.chunks().size(), currentPatch);

    }
}
