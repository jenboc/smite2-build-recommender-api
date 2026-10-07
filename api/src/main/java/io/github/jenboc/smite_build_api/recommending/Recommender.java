package io.github.jenboc.smite_build_api.recommending;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.jenboc.smite_build_api.decoding.DecodedQuery;
import io.github.jenboc.smite_build_api.decoding.QueryDecoder;
import io.github.jenboc.smite_build_api.decoding.QueryType;
import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.ollama.OllamaClient;
import io.github.jenboc.smite_build_api.prompts.PromptBuilder;

@Service
public class Recommender {

    private final QueryDecoder queryDecoder;
    private final ContextGatherer contextGatherer;
    private final OllamaClient ollamaClient;

    public Recommender(
            QueryDecoder queryDecoder,
            ContextGatherer contextGatherer,
            OllamaClient ollamaClient
    ) {
        this.queryDecoder = queryDecoder;
        this.contextGatherer = contextGatherer;
        this.ollamaClient = ollamaClient;
    }

    /**
     * Query the LLM for a build recommendation
     * @param userQuery the raw string user query
     * @returns the raw string of the LLM response
     */
    public RecommendationResponse queryForRecommendation(String userQuery) {
        DecodedQuery decoded = queryDecoder.decode(userQuery);
        return queryForRecommendation(decoded);
    }

    /**
     * Query the LLM for a build recommendation using a decoded query
     * @param userQuery the decoded user query
     * @returns the raw string of the LLM response
     */
    public RecommendationResponse queryForRecommendation(DecodedQuery userQuery) {
        List<IndexedChunk> ctx = contextGatherer.gather(userQuery);

        String prompt = switch (userQuery.type()) {
            case QueryType.BUILD_RECOMMENDATION -> buildRecommendationPrompt(userQuery.rawQuery(), ctx);
            case QueryType.ITEM_SYNERGY -> buildSynergyPrompt(userQuery.rawQuery(), ctx);
            default -> buildGeneralPrompt(userQuery.rawQuery(), ctx);
        };

        String response = ollamaClient.generate(prompt);

        return new RecommendationResponse(
                userQuery.type(),
                userQuery.rawQuery(),
                response
        );
    }

    private String buildRecommendationPrompt(String userQuery, List<IndexedChunk> context) {
        String domainKnowledge = String.join("\n\n", List.of(CONTEXT_USAGE_KNOWLEDGE,
                    ROLE_KNOWLEDGE, STAT_KNOWLEDGE));

        return new PromptBuilder()
            .withInstructions(RECOMMENDATION_PROMPT_INSTRUCTIONS)
            .withDomainKnowledge(domainKnowledge)
            .withContext(context)
            .withUserQuery(userQuery)
            .build();
    }

    private String buildSynergyPrompt(String userQuery, List<IndexedChunk> context) {
        String domainKnowledge = String.join("\n\n", List.of(CONTEXT_USAGE_KNOWLEDGE,
                    STAT_KNOWLEDGE));

        return new PromptBuilder()
            .withInstructions(SYNERGY_PROMPT_INSTRUCTIONS)
            .withDomainKnowledge(domainKnowledge)
            .withContext(context)
            .withUserQuery(userQuery)
            .build();
    }

    private String buildGeneralPrompt(String userQuery, List<IndexedChunk> context) {
        String domainKnowledge = String.join("\n\n", List.of(CONTEXT_USAGE_KNOWLEDGE,
                    ROLE_KNOWLEDGE, STAT_KNOWLEDGE));

        return new PromptBuilder()
            .withInstructions(GENERAL_PROMPT_INSTRUCTIONS)
            .withDomainKnowledge(domainKnowledge)
            .withContext(context)
            .withUserQuery(userQuery)
            .build();
    }

    private final String RECOMMENDATION_PROMPT_INSTRUCTIONS = """
        You are a SMITE 2 build recommendation assistant. You recommend item
        builds for gods applying ONLY the DOMAIN KNOWLEDGE and CONTEXT provided
        below.

        Only use information present in the retrieved context. DO NOT invent
        gods, items, abilities, stats or effects. If the context does not
        contain enough information to answer fully, say so explicitly rather
        than guessing.

        When considering items, ensure that you consider the item's passive
        and active abilities, do not solely base your recommendations on the
        item's stats.

        If a user requests something which conflicts with "known facts" (e.g.
        if the user requests a strength build for a character which does not feature
        strength scaling abilities), point this out clearly before fulfilling their query.
        Do not silently ignore the request, nor silently comply without noting the conflict.

        Give your answer in the following EXACT format:

        Starter: <item name, or "none available in context">
        Items: <item 1>, <item 2>, <item 3>, <item 4>, <item 5>, <item 6>
        Relic: <relic name, or "none available in context">
        Reasoning: <2-4 sentences explaining your recommendation, referencing ONLY facts present
        in your DOMAIN KNOWLEDGE or CONTEXT>

        Only write STARTER items in the "Starter" row, tier 3 items of any category,
        or non-free relics in the "Items" row and free relics on the "Relic" row.
    """.strip();
    
    private final String SYNERGY_PROMPT_INSTRUCTIONS = """
        You are a SMITE 2 assistant. You help players understand how different
        items interact with each other, and why certain item combinations can
        work well together. Respond to the USER QUERY using ONLY the CONTEXT
        and DOMAIN KNOWLEDGE provided below.

        Only use information present in the retrieved context. DO NOT invent
        gods, items, abilities, stats or effects. If the context does not
        contain enough information to answer fully, say so explicitly rather
        than guessing.

        When considering items, ensure that you consider the item's passive
        and active abilities, do not solely base your recommendations on the
        item's stats.

        Two items "synergise" when one amplifies, enables or compensates for
        the other -- for example, one item benefits from a stat, passive or active abilty
        provided by the other item. Another example would be if one item covers a weakness
        the other has, e.g. if one item provides penetration while the other provides raw power.

        For each synergy you identify, summarise the synergy in this exact format:

        Items: <Names of compared items>
        Synergy: <Yes/No/Partial>
        Explanation: <2-4 sentences, citing the information gathered about the items
        from the context>
    """.strip();

    private final String GENERAL_PROMPT_INSTRUCTIONS = """
        You are a SMITE 2 assistant. Answer the user's question using ONLY
        the retrieved context provided below.

        Do not invent gods, items, abilities, stats, or effects, and do not
        use outside knowledge of SMITE, SMITE 2, or any other game. If the
        retrieved context does not contain enough information to answer, say
        so clearly rather than guessing.

        Answer directly and concisely, referencing only facts present in the
        retrieved context.
    """.strip();

    private final String CONTEXT_USAGE_KNOWLEDGE = """
        How to use context:
        - "Cooldown" inside of an item's active ability description is highly likely to
          relate to the active ability's cooldown, rather than the cooldown of gods' abilities.
        - "Cooldown" inside of an ability entry is related to THAT ability's cooldown, and is unrelated
          to other abilities or items.
        - A scaling value like "75% intelligence" means the effect's magnitude is 75% of the
          god's current intelligence stat.
    """.strip();

    private final String ROLE_KNOWLEDGE = """
        Gods fulfill (at least) one of the following roles:
        - SOLO: a bruiser and off-tank, tries to damage and disrupt weaker enemies.
        - SUPPORT: a tank and utility support, tries to mitigate damage and disrupt enemies.
        - JUNGLE: a roamer and ganker, tries to flank and ambush enemies.
        - CARRY: a damage dealer (typically physical), tries to defeat bosses and towers.
        - MID: a damage dealer (typically magical), tries to secure objectives in the centre of the map.
    """.strip();

    private final String STAT_KNOWLEDGE = """
        The context will make reference to the following stats:
        - ATTACK DAMAGE:
        - ATTACK SPEED: increases the rate of attacks.
        - COOLDOWN RATE: 1 cooldown rate = use abilities 1% more often.
        - CRITICAL CHANCE: increases the chance of an attack to critically strike, dealing 150% damage.
        - DAMPENING: 1 dampening = 1% damage mitigation against abilities (caps at 35).
        - ECHO: 1 echo = 1% chance for abilities to echo, dealing 115% damage.
        - HEALTH REGEN: increases passive health regeneration.
        - INTELLIGENCE: increases damage of intelligence scaling abilities and attacks.
        - LIFESTEAL: heals a proportion of damage dealt.
        - MAGICAL PROTECTION: 1 magical protection = withstand 1% more magical damage.
        - MANA REGEN: increases passive mana regeneration.
        - MAX HEALTH: increases total health pool.
        - MAX MANA: increases total mana pool.
        - MOVEMENT SPEED: how fast a god can move.
        - PATHFINDING: increases movement speed.
        - PENETRATION: ignore a proportion of target protections (caps at 50 flat, or 40%).
        - PHYSICAL PROTECTION: 1 physical protection = withstand 1% more physical damage.
        - PLATING: 1 plating = 1% damage mitigation (caps at 35).
        - STRENGTH: increases damage of strength scaling abilities and attacks.
        - TENACITY: reduces the duration and strength of crowd control (caps at 50%).

        "Attacks" refer to gods' basic attack abilities, "abilities" refer to
        gods' first, second, third and ultimate abilities.
    """.strip();
}
