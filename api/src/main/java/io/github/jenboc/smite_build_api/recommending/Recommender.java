package io.github.jenboc.smite_build_api.recommending;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.ollama.OllamaClient;
import io.github.jenboc.smite_build_api.retrieval.Retriever;

@Service
public class Recommender {

    private final OllamaClient ollamaClient;
    private final RecommendationPromptBuilder promptBuilder;
    private final Retriever retriever;

    public Recommender(
            OllamaClient ollamaClient,
            RecommendationPromptBuilder promptBuilder,
            Retriever retriever
    ) {
        this.ollamaClient = ollamaClient;
        this.promptBuilder = promptBuilder;
        this.retriever = retriever;
    }

    /**
     * Query the LLM for a build recommendation
     * @param userQuery the user query passed to the API
     * @returns a RecommendationResponse DTO which contains the response
     * @see RecommendationResponse
     */
    public String queryForRecommendation(String userQuery) {
        List<IndexedChunk> context = retriever.retrieve(userQuery, 25);
        String prompt = promptBuilder.buildPrompt(userQuery, context);

        return ollamaClient.generate(prompt);
    }
}
