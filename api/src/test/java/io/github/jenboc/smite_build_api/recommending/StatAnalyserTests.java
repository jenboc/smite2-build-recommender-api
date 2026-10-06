package io.github.jenboc.smite_build_api.recommending;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.github.jenboc.smite_build_api.decoding.AspectFlag;
import io.github.jenboc.smite_build_api.decoding.GodMention;
import io.github.jenboc.smite_build_api.model.Abilities;
import io.github.jenboc.smite_build_api.model.Ability;
import io.github.jenboc.smite_build_api.model.AbilitySet;
import io.github.jenboc.smite_build_api.model.AbilityStat;
import io.github.jenboc.smite_build_api.model.Aspect;
import io.github.jenboc.smite_build_api.model.God;
import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.ScaledComponent;
import io.github.jenboc.smite_build_api.model.ScaledStatData;
import io.github.jenboc.smite_build_api.model.ValueUnit;

class StatAnalyserTests {

    @Test
    void returnsBaseScalingWithBaseFlag() {
        AbilitySet baseAbilitySet = new AbilitySet(
            List.of(),
            List.of(),
            List.of(scalingAbility("Intelligence")),
            List.of(scalingAbility("Strength")),
            List.of(scalingAbility("Attack Damage")),
            List.of(scalingAbility("Movement Speed"))
        );

        God god = godWithBaseAbilities(baseAbilitySet);

        GodMention mention = new GodMention(god, AspectFlag.BASE);

        Set<GodStatType> result = StatAnalyser.analyseScalingStats(mention);

        assertEquals(
            Set.of(
                GodStatType.INTELLIGENCE,
                GodStatType.STRENGTH,
                GodStatType.ATTACK_DAMAGE,
                GodStatType.MOVEMENT_SPEED
            ),
            result
        );
    }

    @Test
    void returnsAspectScalingWithAspectFlag() {
        AbilitySet baseAbilitySet = new AbilitySet(
            List.of(),
            List.of(),
            List.of(scalingAbility("Intelligence")),
            List.of(scalingAbility("Strength")),
            List.of(scalingAbility("Attack Damage")),
            List.of(scalingAbility("Movement Speed"))
        );

        AbilitySet aspectAbilitySet = new AbilitySet(
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(scalingAbility("Magical Protection")),
            List.of()
        );

        God god = godWithBaseAbilities(baseAbilitySet);
        god.setAspect(new Aspect(
            "Test Aspect",
            "Replaces the third ability",
            Map.of("base", aspectAbilitySet)
        ));

        GodMention mention = new GodMention(god, AspectFlag.ASPECT);

        Set<GodStatType> result = StatAnalyser.analyseScalingStats(mention);

        assertEquals(
            Set.of(
                GodStatType.INTELLIGENCE,
                GodStatType.STRENGTH,
                GodStatType.MAGICAL_PROTECTION,
                GodStatType.MOVEMENT_SPEED
            ),
            result
        );
    }

    @Test
    void returnsBothScalingsWithUnspecifiedFlag() {
        AbilitySet baseAbilitySet = new AbilitySet(
            List.of(),
            List.of(),
            List.of(scalingAbility("Intelligence")),
            List.of(),
            List.of(scalingAbility("Magical Protection")),
            List.of(scalingAbility("Strength"))
        );

        AbilitySet aspectAbilitySet = new AbilitySet(
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(scalingAbility("Attack Damage")),
            List.of()
        );

        God god = godWithBaseAbilities(baseAbilitySet);
        god.setAspect(new Aspect(
            "Test Aspect",
            "Replaces the third ability",
            Map.of("base", aspectAbilitySet)
        ));

        GodMention mention = new GodMention(god, AspectFlag.UNSPECIFIED);

        Set<GodStatType> result = StatAnalyser.analyseScalingStats(mention);

        assertEquals(
            Set.of(
                GodStatType.INTELLIGENCE,
                GodStatType.MAGICAL_PROTECTION,
                GodStatType.STRENGTH,
                GodStatType.ATTACK_DAMAGE
            ),
            result
        );
    }

    @Test
    void ignoresNonExactStatNameStrings() {
        AbilitySet abilitySet = new AbilitySet(
            List.of(),
            List.of(),
            List.of(
                scalingAbility("Intelligence"),
                scalingAbility("Intelligence Damage"),
                scalingAbility("Magical Proc."),
                scalingAbility("Protections")
            ),
            List.of(),
            List.of(),
            List.of()
        );

        God god = godWithBaseAbilities(abilitySet);

        GodMention mention = new GodMention(god, AspectFlag.BASE);

        Set<GodStatType> result = StatAnalyser.analyseScalingStats(mention);

        assertEquals(
            Set.of(GodStatType.INTELLIGENCE),
            result
        );
    }

    @Test
    void ignoresBasicAttackScaling() {
        AbilitySet abilitySet = new AbilitySet(
            List.of(scalingAbility("Intelligence")),
            List.of(),
            List.of(scalingAbility("Strength")),
            List.of(),
            List.of(),
            List.of()
        );

        God god = godWithBaseAbilities(abilitySet);

        GodMention mention = new GodMention(god, AspectFlag.BASE);

        Set<GodStatType> result = StatAnalyser.analyseScalingStats(mention);

        assertEquals(
            Set.of(GodStatType.STRENGTH),
            result
        );
    }

    @Test
    void ignoresPassiveAbilityScaling() {
        AbilitySet abilitySet = new AbilitySet(
            List.of(),
            List.of(scalingAbility("Intelligence")),
            List.of(scalingAbility("Strength")),
            List.of(),
            List.of(),
            List.of()
        );

        God god = godWithBaseAbilities(abilitySet);

        GodMention mention = new GodMention(god, AspectFlag.BASE);

        Set<GodStatType> result = StatAnalyser.analyseScalingStats(mention);

        assertEquals(
            Set.of(GodStatType.STRENGTH),
            result
        );
    }

    @Test
    void returnsMultipleScalingComponentsFromSingleAbility() {
        Ability ability = new Ability(
            "Test Ability",
            null,
            List.of(),
            "Test ability",
            List.of(
                new AbilityStat(
                    "Scaling",
                    new ScaledStatData(List.of(
                        new ScaledComponent(
                            new ValueUnit(75, ValueUnit.Unit.PERCENT),
                            "Intelligence"
                        ),
                        new ScaledComponent(
                            new ValueUnit(25, ValueUnit.Unit.PERCENT),
                            "Strength"
                        )
                    ))
                )
            ),
            List.of()
        );

        AbilitySet abilitySet = new AbilitySet(
            List.of(),
            List.of(),
            List.of(ability),
            List.of(),
            List.of(),
            List.of()
        );

        God god = godWithBaseAbilities(abilitySet);

        GodMention mention = new GodMention(god, AspectFlag.BASE);

        Set<GodStatType> result = StatAnalyser.analyseScalingStats(mention);

        assertEquals(
            Set.of(
                GodStatType.INTELLIGENCE,
                GodStatType.STRENGTH
            ),
            result
        );
    }

    @Test
    void returnsScalingFromMultipleStances() {
        AbilitySet firstStance = new AbilitySet(
            List.of(),
            List.of(),
            List.of(scalingAbility("Intelligence")),
            List.of(),
            List.of(),
            List.of()
        );

        AbilitySet secondStance = new AbilitySet(
            List.of(),
            List.of(),
            List.of(scalingAbility("Strength")),
            List.of(),
            List.of(),
            List.of()
        );

        God god = new God();

        god.setAbilities(new Abilities(
            Map.of(
                "stanceOne", firstStance,
                "stanceTwo", secondStance
            )
        ));

        GodMention mention = new GodMention(god, AspectFlag.BASE);

        Set<GodStatType> result = StatAnalyser.analyseScalingStats(mention);

        assertEquals(
            Set.of(
                GodStatType.INTELLIGENCE,
                GodStatType.STRENGTH
            ),
            result
        );
    }

    private static God godWithBaseAbilities(AbilitySet abilitySet) {
        God god = new God();

        god.setAbilities(new Abilities(
            Map.of("base", abilitySet)
        ));

        return god;
    }

    private static Ability scalingAbility(String statType) {
        ScaledComponent component = new ScaledComponent(
            null,
            statType
        );

        ScaledStatData data = new ScaledStatData(
            List.of(component)
        );

        AbilityStat stat = new AbilityStat(
            "Scaling",
            data
        );

        return new Ability(
            "Test Ability",
            null,
            List.of(),
            "Test ability",
            List.of(stat),
            List.of()
        );
    }
}
