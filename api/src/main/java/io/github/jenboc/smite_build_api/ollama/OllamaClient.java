package io.github.jenboc.smite_build_api.ollama;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * A SpringBoot service used to interact with Ollama's API
 */
@Service 
public class OllamaClient {

    private final RestClient restClient;
    private final String embeddingModel;
    private final String generationModel;

    public OllamaClient(
            RestClient ollamaRestClient,
            @Value("${ollama.embedding-model}") String embeddingModel,
            @Value("${ollama.generation-model}") String generationModel
    ) {
        this.restClient = ollamaRestClient;
        this.embeddingModel = embeddingModel;
        this.generationModel = generationModel;
    }

    /**
     * Send a list of strings to Ollama to be embedded.
     * @param strings the list of strings to be embedded separately
     * @returns the vectors representing strings
     */
    public List<List<Double>> embed(List<String> strings) {
        EmbedResponse resp = restClient.post()
            .uri("/api/embed")
            .body(new EmbedRequest(embeddingModel, strings))
            .retrieve()
            .body(EmbedResponse.class);

        if (resp == null) {
            throw new IllegalStateException("Ollama returned empty state for /api/embed");
        }

        return resp.embeddings();
    }

    /**
     * Send a generation prompt to Ollama
     * @param prompt the prompt to send
     * @returns the LLM's response to the prompt
     */
    public String generate(String prompt) {
        GenerateResponse resp = restClient.post()
            .uri("/api/generate")
            .body(new GenerateRequest(
                        generationModel, 
                        prompt, 
                        new GenerateOptions(8192), 
                        false))
            .retrieve()
            .body(GenerateResponse.class);

        if (resp == null) {
            throw new IllegalStateException("Ollama returned empty state for /api/generate");
        }

        return resp.response();
    }
}
