package io.github.jenboc.smite_build_api.decoding;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import io.github.jenboc.smite_build_api.loading.DataLoader;
import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.Item;
import io.github.jenboc.smite_build_api.ollama.GenerateOptions;
import io.github.jenboc.smite_build_api.ollama.OllamaClient;
import io.github.jenboc.smite_build_api.prompts.PromptBuilder;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Responsible for the decoding of a raw string query
 */
@Service 
public class QueryDecoder {

    private final DataLoader dataLoader;
    private final OllamaClient ollamaClient;
    private final JsonMapper jsonMapper;

    public QueryDecoder(
            DataLoader dataLoader,
            OllamaClient ollamaClient,
            JsonMapper jsonMapper
    ) {
        this.dataLoader = dataLoader;
        this.ollamaClient = ollamaClient;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Decode a raw query
     * @param query the raw query
     * @returns the decoded query
     */
    public DecodedQuery decode(String query) {
        String prompt = buildPrompt(query);
        
        GenerateOptions options = new GenerateOptions(8192, 0.0);
        String rawResponse = ollamaClient.generate(prompt, options);
        
        LLMDecodeResponse serialisedResponse = serialiseResponse(rawResponse);

        return validateLLMResponse(serialisedResponse, query);
    }

    private LLMDecodeResponse serialiseResponse(String response) {
        try {
            return jsonMapper.readValue(response, LLMDecodeResponse.class);
        } catch (StreamReadException e) {
            throw new IllegalLLMDecodeResponse(
                "Unable to deserialise LLM's raw decode response",
                e
            );
        }
    }

    private DecodedQuery validateLLMResponse(LLMDecodeResponse response, String raw) {
        // We must assume that the lengths of primaryGods and primaryAspects are
        // the same.
        if (response.primaryGods().size() != response.primaryAspects().size()) {
            throw new IllegalLLMDecodeResponse("Expected primaryGods and primaryAspects "
                + "to be the same size");
        }

        // We must make the same assumption about opponentGods and opponentAspects.
        if (response.opponentGods().size() != response.opponentAspects().size()) {
            throw new IllegalLLMDecodeResponse("Expected opponentGods and opponentAspects "
                + "to be the same size");
        }

        List<GodStatType> wantedStats = response.wantedStats().stream()
            .map(s -> tryParseEnum(s, GodStatType.class))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        List<GodStatType> excludedStats = response.excludedStats().stream()
            .map(s -> tryParseEnum(s, GodStatType.class))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        // Everything else is a reformatting/data retrieval job
        return new DecodedQuery(
            parseEnum(response.type(), QueryType.GENERAL),
            collectMentions(response.primaryGods(), response.primaryAspects()),
            collectMentions(response.opponentGods(), response.opponentAspects()),
            collectItems(response.wantedItems()),
            collectItems(response.excludedItems()),
            wantedStats,
            excludedStats,
            raw
        );
    }

    private List<GodMention> collectMentions(List<String> names, List<String> flagStrings) {
        // Assertion which should've been checked prior
        assert names.size() == flagStrings.size();

        List<AspectFlag> flags = flagStrings.stream()
            .map(s -> parseEnum(s, AspectFlag.UNSPECIFIED))
            .toList();

        List<GodMention> mentions = new ArrayList<>();

        for (int i = 0; i < names.size(); i++) {
            Optional<God> god = dataLoader.getGodByName(names.get(i));

            if (god.isEmpty()) continue;

            mentions.add(new GodMention(god.get(), flags.get(i)));
        }

        return mentions;
    }

    private static <E extends Enum<E>> Optional<E> tryParseEnum(String value, Class<E> enumClass) {
        if (value == null || value.isBlank()) return Optional.empty();

        String normalised = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");

        try {
            return Optional.of(Enum.valueOf(enumClass, normalised));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

    }

    private static <E extends Enum<E>> E parseEnum(String value, E defaultValue) {
        return tryParseEnum(value, defaultValue.getDeclaringClass())
            .orElseGet(() -> defaultValue);
    }

    private List<Item> collectItems(List<String> names) {
        return names.stream()
            .map(name -> dataLoader.getItemByName(name))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    private String buildPrompt(String userQuery) {
        return new PromptBuilder()
            .withInstructions(PROMPT_INSTRUCTIONS)
            .withDomainKnowledge(promptDomainKnowledge())
            .withUserQuery(userQuery)
            .build();
    }

    private String promptDomainKnowledge() {
        String godNames = dataLoader.getAllGods().stream()
            .map(God::getName)
            .toList()
            .toString();

        String itemNames = dataLoader.getAllItems().stream()
            .map(Item::getName)
            .toList()
            .toString();

        String statNames = GodStatType.values().toString();

        return """
            There are 3 types of request:
            1. "BUILD_RECOMMENDATION" - the user is requesting a build recommendation for a god
            2. "ITEM_SYNERGY" - the user is querying about item synergies
            3. "GENERAL" - a general query, or a query that cannot be categorised into the 2 above types

            The only valid god names are: %s

            The only valid item names are: %s

            There are 3 types of aspect flags:
            1. "BASE" - the god's aspect IS NOT being used
            2. "ASPECT" - the god's aspect IS being used
            3. "UNSPECIFIED" - the user hasn't specified whether the aspect is or is not being used

            The following stat names are in the game: %s
            NOTE: the user may infer a reference to any of these, and may not refer
            to them directly. For instance, a lone "protections" refers to both
            "MAGICAL_PROTECTION" and "PHYSICAL_PROTECTION".
        """.strip().formatted(godNames, itemNames, statNames);
    }

    private final String PROMPT_INSTRUCTIONS = """
        Extract structured information from a SMITE 2 related user query.
        Respond ONLY with valid JSON, no other text, matching exactly this
        shape:

        {
            "type": "...",
            "primary_gods": [...],
            "opponent_gods": [...],
            "primary_aspects": [...],
            "opponent_aspects": [...],
            "wanted_items": [...],
            "excluded_items": [...],
            "wanted_stats": [...],
            "excluded_stats": [...]
        }

        Fields:
        - type: the type of request
        - primary_gods: the names of gods that the user wishes to play as
        - opponent_gods: the names of gods that the user is playing against
        - primary_aspects: contains a flag for each primary_god depicting whether their aspect is being used
        - opponent_aspects: contains a flag for each opponent_god depicting whether their aspect is being used
        - wanted_items: the names of items the user explicitly wants to use
        - excluded_items: the names of items the user explicitly wants to avoid
        - wanted_stats: the names of stats which the user explicitly wants to focus on
        - excluded_stats: the names of stats which the user explicitly wants to avoid, or doesn't care about

        Rules:
        - ONLY use EXACT strings from your DOMAIN KNOWLEDGE lists to fill these fields 
        - primary_gods and primary_aspects must be the same length
        - opponent_gods and opponent_aspects must be the same length
        - Ensure that primary_gods[i] relates to primary_aspects[i]
        - Ensure that opponent_gods[i] relates to opponent_aspects[i]
        - Do not fill the fields with anything which cannot be inferred, or is not mentioned
        - If nothing applies to a field, use an empty array (DO NOT USE null!)
    """.strip();
}
