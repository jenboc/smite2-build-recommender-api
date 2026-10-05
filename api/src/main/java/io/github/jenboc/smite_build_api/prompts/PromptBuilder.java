package io.github.jenboc.smite_build_api.prompts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.jenboc.smite_build_api.indexing.IndexedChunk;

/**
 * Responsible for forming an arbitary prompt to give to an LLM
 */
public class PromptBuilder {

    private final Map<String, String> components = new HashMap<>();
    private final List<String> ordering = new ArrayList<>();

    /**
     * Build the prompt where components are assembled in the order they
     * were given to the builder
     * @returns the completed prompt
     */
    public String build() {
        return String.join("\n\n", ordering.stream()
                .filter(k -> components.containsKey(k))
                .map(k -> formatHeader(k) + "\n" + components.get(k))
                .toList()).strip();
    }
   
    /**
     * Add context to the prompt
     * @param chunks the chunks to add as context
     * @returns the builder object
     */
    public PromptBuilder withContext(List<IndexedChunk> chunks) {
        List<String> chunkTexts = chunks == null
            ? List.of()
            : chunks.stream().map(IndexedChunk::text).toList();

        addContent("context", String.join("\n\n", chunkTexts));
        return this;
    }

    /**
     * Add instructions to the prompt
     * @param instructions raw string containing instructions
     * @returns the builder object
     */
    public PromptBuilder withInstructions(String instructions) {
        addContent("instructions", instructions);
        return this;
    }

    /**
     * Add domain knowledge to the prompt
     * @param knowledge raw string containing domain knowledge
     * @returns the builder object
     */
    public PromptBuilder withDomainKnowledge(String knowledge) {
        addContent("domain knowledge", knowledge);
        return this;
    }

    /**
     * Add examples to the prompt
     * @param examples a list of raw string examples
     * @returns the builder object
     */
    public PromptBuilder withExamples(List<String> examples) {
        StringBuilder content = new StringBuilder();

        for (int i = 0; i < examples.size(); i++) {
            content.append("Example ").append(i + 1).append(":\n");
            content.append(examples.get(i)).append("\n\n");
        }

        addContent("examples", content.toString().strip());
        return this;
    }

    /**
     * Add a user query to the prompt
     * @param userQuery the raw string user query
     * @returns the builder object
     */
    public PromptBuilder withUserQuery(String userQuery) {
        addContent("user query", userQuery);
        return this;
    }

    private void addContent(String key, String content) {
        // Add the key to the ordering
        // The key should only exist once in the ordering, so if it is in there,
        // get rid of it.
        ordering.remove(key);
        ordering.add(key);

        // Overwrite (or add) content in the map
        components.put(key, content == null ? "" : content);
    }

    private String formatHeader(String header) {
        return "==== " + header.toUpperCase() + " ====";
    }
}
