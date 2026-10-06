package io.github.jenboc.smite_build_api.recommending;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.jenboc.smite_build_api.decoding.AspectFlag;
import io.github.jenboc.smite_build_api.decoding.GodMention;
import io.github.jenboc.smite_build_api.model.Ability;
import io.github.jenboc.smite_build_api.model.AbilitySet;
import io.github.jenboc.smite_build_api.model.AbilityStat;
import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.ScaledStatData;

public class StatAnalyser {

    public static Set<GodStatType> analyseScalingStats(GodMention mention) {
        return switch (mention.aspect()) {
            case AspectFlag.ASPECT -> aspectScaling(mention.god());
            case AspectFlag.BASE -> baseScaling(mention.god());
            default -> unspecifiedScaling(mention.god());
        };
    }

    private static Set<GodStatType> unspecifiedScaling(God god) {
        Set<GodStatType> scalingStats = new HashSet<>();

        // Get scaling from base
        scalingStats.addAll(baseScaling(god));

        // Get scaling from ability overwrites
        scalingStats.addAll(god.getAspect().getModifies().values().stream()
            .flatMap(set -> abilitySetScaling(set).stream())
            .collect(Collectors.toSet()));

        return scalingStats;
    }

    private static Set<GodStatType> baseScaling(God god) {
        return god.getAbilities().getStances().values().stream()
            .flatMap(set -> abilitySetScaling(set).stream())
            .collect(Collectors.toSet());
    }

    private static Set<GodStatType> aspectScaling(God god) {
        if (god.getAspect() == null) {
            return baseScaling(god);
        }

        // Return ability scaling, accounting for aspect ability overwrites.
        Set<GodStatType> stats = new HashSet<>();

        for (Map.Entry<String, AbilitySet> stance : god.getAbilities().getStances().entrySet()) {
            String stanceName = stance.getKey();
            AbilitySet baseSet = stance.getValue();
            AbilitySet aspectSet = god.getAspect().getModifies().get(stanceName);

            AbilitySet overriden = new AbilitySet(
                aspectOverride(baseSet, aspectSet, AbilitySet::getBasicAttack),
                aspectOverride(baseSet, aspectSet, AbilitySet::getPassive),
                aspectOverride(baseSet, aspectSet, AbilitySet::getFirst),
                aspectOverride(baseSet, aspectSet, AbilitySet::getSecond),
                aspectOverride(baseSet, aspectSet, AbilitySet::getThird),
                aspectOverride(baseSet, aspectSet, AbilitySet::getUltimate)
            );

            stats.addAll(abilitySetScaling(overriden));
        }

        return stats;
    }

    // Choose the aspect slot if it is available
    private static List<Ability> aspectOverride(
            AbilitySet base,
            AbilitySet aspect,
            Function<AbilitySet, List<Ability>> getter
    ) {
        List<Ability> mod = aspect == null ? List.of() : getter.apply(aspect);
        return mod.isEmpty() ? getter.apply(base) : mod;
    }

    private static Set<GodStatType> abilitySetScaling(AbilitySet abilitySet) {
        // Only base off of main 4 abilities
        // Basic attack always scales off of everything AND passive obviously
        // is passive
        Stream<Ability> abilities = List.of(abilitySet.getFirst(), abilitySet.getSecond(),
                abilitySet.getThird(), abilitySet.getUltimate())
            .stream().flatMap(List::stream);

        return abilities
            .flatMap(a -> a.getStats().stream())
            .map(AbilityStat::getData)
            .filter(d -> d instanceof ScaledStatData)
            .flatMap(d -> ((ScaledStatData)d).getComponents().stream())
            .map(c -> parseStatType(c.getOf()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }

    private static Optional<GodStatType> parseStatType(String value) {
        String formatted = value.trim().toUpperCase().replace(" ", "_");

        try {
            return Optional.of(GodStatType.valueOf(formatted));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
