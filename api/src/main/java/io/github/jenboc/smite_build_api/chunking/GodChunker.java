package io.github.jenboc.smite_build_api.chunking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.github.jenboc.smite_build_api.model.Ability;
import io.github.jenboc.smite_build_api.model.AbilitySet;
import io.github.jenboc.smite_build_api.model.Aspect;
import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.GodBaseStat;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.God.Role;

@Component
public class GodChunker extends Chunker<God> {

    @Override
    public List<Chunk> chunk(God obj) {
        List<Chunk> chunks = new ArrayList<>();

        chunks.add(chunkOverview(obj));

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

    private String formatBaseStat(GodBaseStat baseStat) {
        return formatValueUnit(baseStat.getBase()) + " (+"
            + formatValueUnit(baseStat.getPerLevel()) + " per level)";
    }
}
