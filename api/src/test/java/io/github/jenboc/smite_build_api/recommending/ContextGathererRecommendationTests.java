package io.github.jenboc.smite_build_api.recommending;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jenboc.smite_build_api.decoding.AspectFlag;
import io.github.jenboc.smite_build_api.decoding.DecodedQuery;
import io.github.jenboc.smite_build_api.decoding.GodMention;
import io.github.jenboc.smite_build_api.decoding.QueryType;
import io.github.jenboc.smite_build_api.indexing.IndexedChunk;
import io.github.jenboc.smite_build_api.model.Abilities;
import io.github.jenboc.smite_build_api.model.Ability;
import io.github.jenboc.smite_build_api.model.AbilitySet;
import io.github.jenboc.smite_build_api.model.AbilityStat;
import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.Item;
import io.github.jenboc.smite_build_api.model.ScaledComponent;
import io.github.jenboc.smite_build_api.model.ScaledStatData;
import io.github.jenboc.smite_build_api.retrieval.GodMentionRetriever;
import io.github.jenboc.smite_build_api.retrieval.GodStatTypeRetriever;
import io.github.jenboc.smite_build_api.retrieval.ItemRetriever;
import io.github.jenboc.smite_build_api.retrieval.VectorRetriever;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextGathererRecommendationTests {

    @Mock private GodMentionRetriever godRetriever;
    @Mock private ItemRetriever itemRetriever;
    @Mock private GodStatTypeRetriever statRetriever;
    @Mock private VectorRetriever vecRetriever;

    private ContextGatherer contextGatherer;

    @BeforeEach
    void setUp() {
        contextGatherer = new ContextGatherer(
                godRetriever,
                itemRetriever,
                statRetriever,
                vecRetriever
        );
    }

    @Test
    void gathersPrimaryAndOpponentGods() {
        GodMention primary = godMention();
        GodMention opponent = godMention();

        IndexedChunk primaryChunk = chunk("primary");
        IndexedChunk opponentChunk = chunk("opponent");

        when(godRetriever.retrieve(primary)).thenReturn(List.of(primaryChunk));
        when(godRetriever.retrieve(opponent)).thenReturn(List.of(opponentChunk));
        when(vecRetriever.retrieveByString("build", 15)).thenReturn(List.of());

        DecodedQuery query = recommendationQuery(
                List.of(primary),
                List.of(opponent),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "build"
        );

        List<IndexedChunk> result = contextGatherer.gather(query);

        assertTrue(result.contains(primaryChunk));
        assertTrue(result.contains(opponentChunk));

        verify(godRetriever).retrieve(primary);
        verify(godRetriever).retrieve(opponent);
    }

    @Test
    void deduplicatesMentionedGods() {
        GodMention god = godMention();
        IndexedChunk godChunk = chunk("god");

        when(godRetriever.retrieve(god)).thenReturn(List.of(godChunk));
        when(vecRetriever.retrieveByString("build", 15)).thenReturn(List.of());

        DecodedQuery query = recommendationQuery(
                List.of(god),
                List.of(god),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "build"
        );

        contextGatherer.gather(query);

        verify(godRetriever).retrieve(god);
    }

    @Test
    void retrievesScalingStatsFromPrimaryGod() {
        God god = godWithScaling(GodStatType.INTELLIGENCE);
        GodMention mention = new GodMention(god, AspectFlag.BASE);

        when(statRetriever.retrieve(GodStatType.INTELLIGENCE))
                .thenReturn(List.of(chunk("intelligence")));

        when(vecRetriever.retrieveByString("build", 15))
                .thenReturn(List.of());

        DecodedQuery query = recommendationQuery(
                List.of(mention),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "build"
        );

        contextGatherer.gather(query);

        verify(statRetriever).retrieve(GodStatType.INTELLIGENCE);
    }

    @Test
    void retrievesProtectionStatsForSoloGod() {
        God god = godWithRoles(God.Role.SOLO);
        GodMention mention = new GodMention(god, AspectFlag.BASE);

        when(statRetriever.retrieve(GodStatType.MAGICAL_PROTECTION))
                .thenReturn(List.of(chunk("magical protection")));

        when(statRetriever.retrieve(GodStatType.PHYSICAL_PROTECTION))
                .thenReturn(List.of(chunk("physical protection")));

        when(vecRetriever.retrieveByString("build", 15))
                .thenReturn(List.of());

        DecodedQuery query = recommendationQuery(
                List.of(mention),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "build"
        );

        contextGatherer.gather(query);

        verify(statRetriever).retrieve(GodStatType.MAGICAL_PROTECTION);
        verify(statRetriever).retrieve(GodStatType.PHYSICAL_PROTECTION);
    }

    @Test
    void retrievesProtectionStatsForSupportGod() {
        God god = godWithRoles(God.Role.SUPPORT);
        GodMention mention = new GodMention(god, AspectFlag.BASE);

        when(statRetriever.retrieve(GodStatType.MAGICAL_PROTECTION))
                .thenReturn(List.of(chunk("magical protection")));

        when(statRetriever.retrieve(GodStatType.PHYSICAL_PROTECTION))
                .thenReturn(List.of(chunk("physical protection")));

        when(vecRetriever.retrieveByString("build", 15))
                .thenReturn(List.of());

        DecodedQuery query = recommendationQuery(
                List.of(mention),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "build"
        );

        contextGatherer.gather(query);

        verify(statRetriever).retrieve(GodStatType.MAGICAL_PROTECTION);
        verify(statRetriever).retrieve(GodStatType.PHYSICAL_PROTECTION);
    }

    @Test
    void doesNotRetrieveProtectionStatsForNonTankRole() {
        God god = godWithRoles(God.Role.MID);
        GodMention mention = new GodMention(god, AspectFlag.BASE);

        when(vecRetriever.retrieveByString("build", 15))
                .thenReturn(List.of());

        DecodedQuery query = recommendationQuery(
                List.of(mention),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "build"
        );

        contextGatherer.gather(query);

        verify(statRetriever, never()).retrieve(GodStatType.MAGICAL_PROTECTION);
        verify(statRetriever, never()).retrieve(GodStatType.PHYSICAL_PROTECTION);
    }

    @Test
    void retrievesWantedStats() {
        when(statRetriever.retrieve(GodStatType.STRENGTH))
                .thenReturn(List.of(chunk("strength")));

        when(statRetriever.retrieve(GodStatType.ATTACK_SPEED))
                .thenReturn(List.of(chunk("attack speed")));

        when(vecRetriever.retrieveByString("build", 15))
                .thenReturn(List.of());

        DecodedQuery query = recommendationQuery(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(GodStatType.STRENGTH, GodStatType.ATTACK_SPEED),
                List.of(),
                "build"
        );

        contextGatherer.gather(query);

        verify(statRetriever).retrieve(GodStatType.STRENGTH);
        verify(statRetriever).retrieve(GodStatType.ATTACK_SPEED);
    }

    @Test
    void doesNotRetrieveExcludedStats() {
        when(vecRetriever.retrieveByString("build", 15))
                .thenReturn(List.of());

        DecodedQuery query = recommendationQuery(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(GodStatType.STRENGTH, GodStatType.ATTACK_SPEED),
                List.of(GodStatType.STRENGTH),
                "build"
        );

        contextGatherer.gather(query);

        verify(statRetriever, never()).retrieve(GodStatType.STRENGTH);
        verify(statRetriever).retrieve(GodStatType.ATTACK_SPEED);
    }

    @Test
    void retrievesWantedItems() {
        Item wantedItem = item("Spear");
        IndexedChunk itemChunk = itemChunk("Spear");

        when(itemRetriever.retrieve(wantedItem))
                .thenReturn(List.of(itemChunk));

        when(vecRetriever.retrieveByString("build", 15))
                .thenReturn(List.of());

        DecodedQuery query = recommendationQuery(
                List.of(),
                List.of(),
                List.of(wantedItem),
                List.of(),
                List.of(),
                List.of(),
                "build"
        );

        List<IndexedChunk> result = contextGatherer.gather(query);

        assertTrue(result.contains(itemChunk));
        verify(itemRetriever).retrieve(wantedItem);
    }

    @Test
    void performsGeneralRetrievalForRecommendation() {
        IndexedChunk generalChunk = chunk("general");

        when(vecRetriever.retrieveByString("build query", 15))
                .thenReturn(List.of(generalChunk));

        DecodedQuery query = recommendationQuery(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "build query"
        );

        List<IndexedChunk> result = contextGatherer.gather(query);

        assertTrue(result.contains(generalChunk));
        verify(vecRetriever).retrieveByString("build query", 15);
    }

    @Test
    void combinesRecommendationContext() {
        God god = godWithScaling(GodStatType.STRENGTH);
        GodMention primary = new GodMention(god, AspectFlag.BASE);

        Item wantedItem = item("Spear");

        IndexedChunk godChunk = chunk("god");
        IndexedChunk statChunk = chunk("strength");
        IndexedChunk itemChunk = itemChunk("Spear");
        IndexedChunk generalChunk = chunk("general");

        when(godRetriever.retrieve(primary))
                .thenReturn(List.of(godChunk));

        when(statRetriever.retrieve(GodStatType.STRENGTH))
                .thenReturn(List.of(statChunk));

        when(itemRetriever.retrieve(wantedItem))
                .thenReturn(List.of(itemChunk));

        when(vecRetriever.retrieveByString("build", 15))
                .thenReturn(List.of(generalChunk));

        DecodedQuery query = recommendationQuery(
                List.of(primary),
                List.of(),
                List.of(wantedItem),
                List.of(),
                List.of(),
                List.of(),
                "build"
        );

        List<IndexedChunk> result = contextGatherer.gather(query);

        assertTrue(result.contains(godChunk));
        assertTrue(result.contains(statChunk));
        assertTrue(result.contains(itemChunk));
        assertTrue(result.contains(generalChunk));
    }

    @Test
    void removesExcludedItems() {
        Item excludedItem = item("Spear");
        Item retainedItem = item("Shield");

        IndexedChunk excludedChunk = itemChunk("Spear");
        IndexedChunk retainedChunk = itemChunk("Shield");
        IndexedChunk godChunk = chunk("god");

        when(itemRetriever.retrieve(excludedItem))
                .thenReturn(List.of(excludedChunk));

        when(itemRetriever.retrieve(retainedItem))
                .thenReturn(List.of(retainedChunk));

        when(vecRetriever.retrieveByString("build", 15))
                .thenReturn(List.of(excludedChunk, retainedChunk, godChunk));

        DecodedQuery query = recommendationQuery(
                List.of(),
                List.of(),
                List.of(excludedItem, retainedItem),
                List.of(excludedItem),
                List.of(),
                List.of(),
                "build"
        );

        List<IndexedChunk> result = contextGatherer.gather(query);

        assertTrue(result.contains(retainedChunk));
        assertTrue(result.contains(godChunk));
        assertTrue(result.stream().noneMatch(excludedChunk::equals));
    }

    @Test
    void retainsNonExcludedItems() {
        Item item = item("Shield");
        IndexedChunk itemChunk = itemChunk("Shield");

        when(itemRetriever.retrieve(item))
                .thenReturn(List.of(itemChunk));

        when(vecRetriever.retrieveByString("build", 15))
                .thenReturn(List.of());

        DecodedQuery query = recommendationQuery(
                List.of(),
                List.of(),
                List.of(item),
                List.of(),
                List.of(),
                List.of(),
                "build"
        );

        List<IndexedChunk> result = contextGatherer.gather(query);

        assertTrue(result.contains(itemChunk));
    }

    @Test
    void retainsNonItemChunksWhenItemsAreExcluded() {
        Item excludedItem = item("Spear");

        IndexedChunk excludedItemChunk = itemChunk("Spear");
        IndexedChunk abilityChunk = chunk("ability");

        when(itemRetriever.retrieve(excludedItem))
                .thenReturn(List.of(excludedItemChunk));

        when(vecRetriever.retrieveByString("build", 15))
                .thenReturn(List.of(excludedItemChunk, abilityChunk));

        DecodedQuery query = recommendationQuery(
                List.of(),
                List.of(),
                List.of(excludedItem),
                List.of(excludedItem),
                List.of(),
                List.of(),
                "build"
        );

        List<IndexedChunk> result = contextGatherer.gather(query);

        assertTrue(result.contains(abilityChunk));
        assertTrue(result.stream().noneMatch(excludedItemChunk::equals));
    }

    @Test
    void deduplicatesIdenticalChunks() {
        IndexedChunk duplicate = chunk("duplicate");

        when(vecRetriever.retrieveByString("build", 15))
                .thenReturn(List.of(duplicate, duplicate));

        DecodedQuery query = recommendationQuery(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "build"
        );

        List<IndexedChunk> result = contextGatherer.gather(query);

        assertEquals(1, result.size());
        assertEquals(duplicate, result.get(0));
    }

    private static DecodedQuery recommendationQuery(
            List<GodMention> primaryGods,
            List<GodMention> opponentGods,
            List<Item> wantedItems,
            List<Item> excludedItems,
            List<GodStatType> wantedStats,
            List<GodStatType> excludedStats,
            String rawQuery
    ) {
        return new DecodedQuery(
                QueryType.BUILD_RECOMMENDATION,
                primaryGods,
                opponentGods,
                wantedItems,
                excludedItems,
                wantedStats,
                excludedStats,
                rawQuery
        );
    }

    private static GodMention godMention() {
        God god = new God();
        god.setRoles(List.of());
        god.setAbilities(new Abilities(Map.of()));

        return new GodMention(god, AspectFlag.BASE);
    }

    private static God godWithRoles(God.Role role) {
        God god = mock(God.class);

        when(god.getRoles()).thenReturn(List.of(role));
        when(god.getAbilities()).thenReturn(new Abilities(Map.of()));

        return god;
    }

    private static God godWithScaling(GodStatType statType) {
        Ability scalingAbility = scalingAbility(statType.name());

        AbilitySet abilitySet = new AbilitySet(
                List.of(),
                List.of(),
                List.of(scalingAbility),
                List.of(),
                List.of(),
                List.of()
        );

        God god = mock(God.class);

        when(god.getRoles()).thenReturn(List.of());
        when(god.getAbilities())
                .thenReturn(new Abilities(Map.of("base", abilitySet)));

        return god;
    }

    private static Ability scalingAbility(String statType) {
        ScaledComponent component = new ScaledComponent(
                null,
                statType
        );

        ScaledStatData data = new ScaledStatData(List.of(component));
        AbilityStat stat = new AbilityStat("Scaling", data);

        return new Ability(
                "Test Ability",
                null,
                List.of(),
                "Test ability",
                List.of(stat),
                List.of()
        );
    }

    private static Item item(String name) {
        Item item = new Item();
        item.setName(name);
        return item;
    }

    private static IndexedChunk chunk(String text) {
        return new IndexedChunk(
                text,
                Map.of("type", "ability", "name", text),
                List.of()
        );
    }

    private static IndexedChunk itemChunk(String name) {
        return new IndexedChunk(
                name,
                Map.of("type", "item", "name", name),
                List.of()
        );
    }
}
