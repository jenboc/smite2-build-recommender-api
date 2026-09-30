package io.github.jenboc.smite_build_api.chunking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jenboc.smite_build_api.model.Ability;
import io.github.jenboc.smite_build_api.model.AbilityStat;
import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.GodBaseStat;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.ScaledStatData;
import io.github.jenboc.smite_build_api.model.TextStatData;
import io.github.jenboc.smite_build_api.model.TieredStatData;
import io.github.jenboc.smite_build_api.model.God.Role;

@Component
public class GodChunker extends Chunker<God> {

    @Override
    public List<Chunk> chunk(God obj) {
        String godName = obj.getName();
        List<Chunk> chunks = new ArrayList<>();

        chunks.add(chunkOverview(obj));

        obj.getAbilities().getStances().forEach((stanceName, abilitySet) -> {
            abilitySet.getBasicAttack()
                .forEach(a -> chunks.add(chunkAbility(a, godName, stanceName, "Basic Attack")));
            abilitySet.getPassive()
                .forEach(a -> chunks.add(chunkAbility(a, godName, stanceName, "Passive Ability")));
            abilitySet.getFirst()
                .forEach(a -> chunks.add(chunkAbility(a, godName, stanceName, "First Ability")));
            abilitySet.getSecond()
                .forEach(a -> chunks.add(chunkAbility(a, godName, stanceName, "Second Ability")));
            abilitySet.getThird()
                .forEach(a -> chunks.add(chunkAbility(a, godName, stanceName, "Third Ability")));
            abilitySet.getUltimate()
                .forEach(a -> chunks.add(chunkAbility(a, godName, stanceName, "Ultimate Ability")));
        });

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

    private Chunk chunkAbility(Ability ability, String godName, String stanceName, String slotLabel) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(godName).append(", ");

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
        sb.append(formatAbilityStats(ability.getStats())).append("\n");
        appendIfPresent(sb, "Notes", formatNotes(ability.getNotes()));

        return new Chunk(
            sb.toString().strip(),
            Map.of(
                "type", "ability",
                "name", ability.getName(),
                "god", godName,
                "stance", stanceName == null ? "Base" : stanceName,
                "variant", ability.getVariant() == null ? "Base" : ability.getVariant(),
                "slot", slotLabel
            )
        );
    }

    private String formatRoles(List<Role> roles) {
        return String.join(", ", roles.stream().map(this::readableEnumName).toList());
    }

    private String formatGodStats(Map<GodStatType, GodBaseStat> stats) {
        if (stats == null || stats.isEmpty()) return null;

        List<String> parts = new ArrayList<>();

        stats.forEach((name, baseStat) -> parts.add(
            formatBaseStat(baseStat) + " " + readableEnumName(name)
        ));

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

        return joinedValues + unit;
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
