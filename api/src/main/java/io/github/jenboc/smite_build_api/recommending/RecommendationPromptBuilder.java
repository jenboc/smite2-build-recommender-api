package io.github.jenboc.smite_build_api.recommending;

import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jenboc.smite_build_api.indexing.IndexedChunk;

/**
 * Responsible for building the LLM prompt asking to recommend builds
 */
@Component 
public class RecommendationPromptBuilder {

    private final String PROMPT_PREAMBLE = """
        You are a SMITE 2 build recommendation assistant.

        Your task is to recommend builds using ONLY the information provided
        in the retrieved context.
    """.strip();

    private final String PROMPT_INSTRUCTIONS = """
        Use the retrieved context as your factual source.
        
        Do not invent:
        - Gods
        - Items
        - Abilities
        - Stats
        - Item Effects
        - Ability Effects

        If the retrieved context does not contain enough information to answer
        the request, say that the available data is insufficient.

        When recommending items, only recommend items present in the retrieved
        context.

        Explain briefly why the recommended items fit the user's request.
    """.strip();

    /**
     * Build a prompt to pass to an LLM for a recommendation request
     * @param userQuery the raw user query
     * @param chunks the chunks the LLM should use to formulate the response
     * @returns the prompt to pass to the LLM
     */
    public String buildPrompt(String userQuery, List<IndexedChunk> chunks) {
        StringBuilder sb = new StringBuilder();
        sb.append(PROMPT_PREAMBLE).append("\n\n");

        sb.append("Retrieved context:\n");
        if (chunks != null && !chunks.isEmpty()) {
            List<String> chunkTexts = chunks.stream().map(IndexedChunk::text).toList();
            sb.append(String.join("\n\n", chunkTexts)).append("\n\n");
        } else {
            sb.append("\n");
        }

        sb.append("User request:\n");

        if (userQuery != null) {
            sb.append(userQuery).append("\n\n");
        } else {
            sb.append("\n");
        }

        sb.append("Instructions:\n").append(PROMPT_INSTRUCTIONS);

        return sb.toString().strip();        
    }

    /**
     * Get the prompt's preamble
     * @returns prompt preamble
     */
    public String getPreamble() {
        return PROMPT_PREAMBLE;
    }

    /**
     * Get the prompt's instructions
     * @returns prompt instructions
     */
    public String getInstructions() {
        return PROMPT_INSTRUCTIONS;
    }
}
