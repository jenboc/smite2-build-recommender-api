package io.github.jenboc.smite_build_api.chunking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jenboc.smite_build_api.model.Ability;
import io.github.jenboc.smite_build_api.model.AbilitySet;
import io.github.jenboc.smite_build_api.model.AbilityStat;
import io.github.jenboc.smite_build_api.model.Aspect;
import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.GodBaseStat;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.ScaledStatData;
import io.github.jenboc.smite_build_api.model.TextStatData;
import io.github.jenboc.smite_build_api.model.TieredStatData;
import io.github.jenboc.smite_build_api.model.God.Role;

/**
 * Breaks a god down into chunks.
 * @see God
 * @see Chunk
*/
@Component
public class GodChunker extends Chunker<God> {

    private static final Map<String, Function<AbilitySet, List<Ability>>> SLOT_ACCESSORS = Map.of(
        "Basic Attack", AbilitySet::getBasicAttack,
        "Passive Ability", AbilitySet::getPassive,
        "First Ability", AbilitySet::getFirst,
        "Second Ability", AbilitySet::getSecond,
        "Third Ability", AbilitySet::getThird,
        "Ultimate Ability", AbilitySet::getUltimate
    );

    /**
     * Break a god down into chunks. Chunks include
     *
     * <ul>
     *      <li>A God overview</li>
     *      <li>Ability overviews</li>
     *      <li>Aspect overview</li>
     *      <li>Aspect modification overviews</li>
     * </ul>
     */
    @Override
    public List<Chunk> chunk(God obj) {
        String godName = obj.getName();
        List<Chunk> chunks = new ArrayList<>();

        chunks.add(chunkOverview(obj));

        obj.getAbilities().getStances().forEach((stanceName, abilitySet) -> 
            forEachSlot(abilitySet, (slotName, ability) ->
                chunks.add(chunkAbility(ability, godName, stanceName, slotName, null))));

        if (obj.getAspect() != null) {
            chunks.addAll(chunkAspect(obj.getAspect(), godName));
        }

        return chunks;
    }

    private Chunk chunkOverview(God god) {
        StringBuilder sb = new StringBuilder();

        sb.append(god.getName()).append(" - ").append(formatRoles(god.getRoles())).append("\n");
        sb.append(readableEnumName(god.getDamageType()))
            .append(" ").append(readableEnumName(god.getDamageRange())).append("\n");

        appendIfPresent(sb, "Specialisations",
            god.getSpecialisations() != null ? String.join(", ", god.getSpecialisations()) : null);
        appendIfPresent(sb, "Aspect",
            god.getAspect() != null ? god.getAspect().getName() : null);

        sb.append("Base stats: ").append(formatGodStats(god.getBaseStats()));

        return new Chunk(
            sb.toString().strip(),
            Map.of(
                "type", "god",
                "name", god.getName(),
                "roles", formatRoles(god.getRoles()),
                "damage_type", readableEnumName(god.getDamageType()),
                "damage_range", readableEnumName(god.getDamageRange())
            )
        );
    }

    private List<Chunk> chunkAspect(Aspect aspect, String godName) {
        List<Chunk> chunks = new ArrayList<>();

        chunks.add(chunkAspectOverview(aspect, godName));

        aspect.getModifies().forEach((stanceName, overrides) ->
            forEachSlot(overrides, (slotLabel, ability) ->
                chunks.add(chunkAbility(ability, godName, stanceName, slotLabel, aspect.getName()))));

        return chunks;
    }

    private Chunk chunkAspectOverview(Aspect aspect, String godName) {
        StringBuilder sb = new StringBuilder();

        sb.append(godName).append(", ").append(aspect.getName()).append("\n");
        sb.append(aspect.getDescription()).append("\n");
        appendIfPresent(sb, "Modifies", formatAspectModSummary(aspect.getModifies()));

        return new Chunk(
            sb.toString().strip(),
            Map.of(
                "type", "aspect",
                "name", aspect.getName(),
                "god", godName
            )
        );
    }

    private void forEachSlot(AbilitySet set, BiConsumer<String, Ability> action) {
        SLOT_ACCESSORS.forEach((slotLabel, accessor) -> {
            List<Ability> abilities = accessor.apply(set);

            if (abilities != null) {
                abilities.forEach(ability -> action.accept(slotLabel, ability));
            }
        });
    }

    private String formatAspectModSummary(Map<String, AbilitySet> mods) {
        if (mods == null || mods.isEmpty()) return null;

        List<String> parts = new ArrayList<>();
        mods.forEach((stanceName, overrides) -> {
            List<String> slots = new ArrayList<>();
            forEachSlot(overrides, (slotLabel, ability) -> slots.add(slotLabel));

            String stancePrefix = "base".equalsIgnoreCase(stanceName) ? "" : stanceName + " Stance (";
            parts.add(stancePrefix + String.join(", ", slots) + ")");
        });

        return String.join("; ", parts);
    }

    private Chunk chunkAbility(Ability ability, String godName, String stanceName, String slotLabel, String aspectName) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(godName).append(", ");

        if (aspectName != null && !aspectName.isEmpty()) {
            sb.append(aspectName).append(", ");
        }

        if (stanceName != null && !"base".equalsIgnoreCase(stanceName)) {
            sb.append(stanceName).append(" Stance, ");
        }

        sb.append(slotLabel).append(" - ").append(ability.getName());

        if (ability.getVariant() != null) {
            sb.append(" (").append(ability.getVariant()).append(")");
        }

        sb.append("\n");
        sb.append(ability.getDescription()).append("\n\n");

        appendIfPresent(sb, "Tags", formatAbilityTags(ability.getTags()));

        if (ability.getStats() != null && !ability.getStats().isEmpty()) {
            sb.append(formatAbilityStats(ability.getStats())).append("\n");
        }

        appendIfPresent(sb, "Notes", formatNotes(ability.getNotes()));

        Map<String, String> metadata = new HashMap<>();
        
        metadata.put("type", "ability");
        metadata.put("name", ability.getName());
        metadata.put("god", godName);
        metadata.put("stance", stanceName == null ? "Base" : stanceName);
        metadata.put("variant", ability.getVariant() == null ? "none" : ability.getVariant());
        metadata.put("slot", slotLabel);

        if (aspectName != null && !aspectName.isEmpty()) {
            metadata.put("aspect", aspectName);
        }

        return new Chunk(
            sb.toString().strip(),
            metadata
        );
    }

    private String formatRoles(List<Role> roles) {
        return String.join(", ", roles.stream().map(this::readableEnumName).toList());
    }

    private String formatGodStats(Map<GodStatType, GodBaseStat> stats) {
        if (stats == null || stats.isEmpty()) return null;

        List<String> parts = new ArrayList<>();

        stats.forEach((name, baseStat) -> {
            // Skip null base stats (e.g. mana + mana regen for manaless gods)
            if (baseStat == null) {
                return;
            }

            parts.add(
                formatBaseStat(baseStat) + " " + readableEnumName(name)
            );
        });

        return String.join(", ", parts);
    }

    private String formatAbilityStats(List<AbilityStat> stats) {
        StringBuilder sb = new StringBuilder();

        stats.forEach(stat -> {
            sb.append(stat.getName()).append(": ");

            switch (stat.getData()) {
                case TieredStatData tiered:
                    sb.append(formatTieredStatData(tiered));
                    break;
                case ScaledStatData scaled:
                    sb.append(formatScaledStatData(scaled));
                    break;
                case TextStatData text:
                    sb.append(text.getRawText());
                    break;
                default:
                    throw new IllegalStateException(
                        "Unhandled StatData subtype: " + stat.getData().getClass().getSimpleName()
                    );
            }

            sb.append("\n");
        });

        return sb.toString().strip();
    }

    private String formatTieredStatData(TieredStatData data) {
        String joinedValues = data.getValues().stream()
            .map(v -> formatNumber(v))
            .collect(Collectors.joining(" | "));

        String unit = data.getUnit();

        if ("percent".equalsIgnoreCase(unit))
            return joinedValues + "%";

        if ("flat".equalsIgnoreCase(unit))
            return joinedValues;

        return joinedValues + " " + unit;
    }

    private String formatScaledStatData(ScaledStatData data) {
        return data.getComponents().stream()
            .map(c -> formatValueUnit(c.getValueUnit()) + " " + c.getOf())
            .collect(Collectors.joining(" + "));
    }

    private String formatAbilityTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        return String.join(", ", tags);
    }

    private String formatBaseStat(GodBaseStat baseStat) {
        return formatValueUnit(baseStat.getBase()) + " (+"
            + formatValueUnit(baseStat.getPerLevel()) + " per level)";
    }
}
