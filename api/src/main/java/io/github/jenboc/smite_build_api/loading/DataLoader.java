package io.github.jenboc.smite_build_api.loading;

import java.nio.file.Path;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tools.jackson.databind.json.JsonMapper;

import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.Item;
import io.github.jenboc.smite_build_api.model.Manifest;


/**
 * Spring Boot Service which loads scraped God and Item .json files.
 */
@Service 
public class DataLoader {

    private final JsonMapper jsonMapper;
    private final Path dataDir;

    // Internal state that is populated by loadAll()
    private Manifest manifest;
    private Map<String, God> gods = new HashMap<>();
    private Map<String, Item> items = new HashMap<>();

    public DataLoader(JsonMapper jsonMapper, @Value("${data.path}") String dataPath) {
        this.jsonMapper = jsonMapper;
        this.dataDir = Path.of(dataPath);
    }

    @PostConstruct
    void loadAll() {
        this.manifest = loadManifest();
        this.gods = loadEntries(manifest.getGods(), God.class);
        this.items = loadEntries(manifest.getItems(), Item.class);

        System.out.printf("Loaded %d gods and %d items from %s\n",
            gods.size(), items.size(), dataDir.toAbsolutePath());
    }

    private Manifest loadManifest() {
        Path manifestPath = dataDir.resolve("manifest.json");

        try (InputStream in = Files.newInputStream(manifestPath)) {
            return jsonMapper.readValue(in, Manifest.class);
        } catch (IOException e) {
            throw new DataLoadException("Failed to read/parse manifest.json", e);
        }
    }

    private <T> Map<String, T> loadEntries(List<Manifest.Entry> entries, Class<T> type) {
        Map<String, T> result = new HashMap<>();

        for (Manifest.Entry entry : entries) {
            Path filePath = dataDir.resolve(entry.getFile());

            try (InputStream in = Files.newInputStream(filePath)) {
                T value = jsonMapper.readValue(in, type);
                result.put(entry.getName().toLowerCase(), value);
            } catch (IOException e) {
                throw new DataLoadException("Failed to load file: " + filePath.toAbsolutePath());
            }
        }
        
        return result;
    }

    /**
     * Get a list of all loaded gods
     */
    public List<God> getAllGods() {
        return List.copyOf(gods.values());
    }
    
    /**
     * Lookup a god by name
     * @param name the god's name (case insensitive)
     */
    public Optional<God> getGodByName(String name) {
        return Optional.ofNullable(gods.get(name.toLowerCase()));
    }

    /**
     * Get a list of all loaded items
     */
    public List<Item> getAllItems() {
        return List.copyOf(items.values());
    }

    /**
     * Lookup an item by name
     * @param name the item's name (case insensitive)
     */
    public Optional<Item> getItemByName(String name) {
        return Optional.ofNullable(items.get(name.toLowerCase()));
    }

    /**
     * Get the manifest
     */
    public Manifest getManifest() {
        return manifest;
    }
}
