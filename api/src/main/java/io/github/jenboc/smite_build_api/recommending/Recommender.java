package io.github.jenboc.smite_build_api.recommending;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.ollama.OllamaClient;
import io.github.jenboc.smite_build_api.prompts.PromptBuilder;
import io.github.jenboc.smite_build_api.retrieval.VectorRetriever;

@Service
public class Recommender {

    private final OllamaClient ollamaClient;
    private final VectorRetriever retriever;

    public Recommender(
            OllamaClient ollamaClient,
            VectorRetriever retriever
    ) {
        this.ollamaClient = ollamaClient;
        this.retriever = retriever;
    }

    /**
     * Query the LLM for a build recommendation
     * @param userQuery the user query passed to the API
     * @returns a RecommendationResponse DTO which contains the response
     * @see RecommendationResponse
     */
    public String queryForRecommendation(String userQuery) {
        List<Double> vec = ollamaClient.embed(List.of(userQuery)).get(0);
        return ollamaClient.generate(buildPrompt(userQuery, retriever.retrieve(vec, 25)));
    }

    private String buildPrompt(String userQuery, List<IndexedChunk> context) {
        return new PromptBuilder()
            .withInstructions(PROMPT_INSTRUCTIONS)
            .withContext(context)
            .withUserQuery(userQuery)
            .build();
    }

    private final String PROMPT_INSTRUCTIONS = """
        You are a SMITE 2 build recommendation assistant.

        Your task is to recommend builds using ONLY the information provided
        in the retrieved context.

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
}
