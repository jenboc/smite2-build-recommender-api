package io.github.jenboc.smite_build_api.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@NoArgsConstructor
@AllArgsConstructor
public class God {
    private @Getter @Setter String name;
    private @Getter @Setter List<Role> roles;
    private @Getter @Setter DamageType damageType;
    private @Getter @Setter DamageRange damageRange;
    private @Getter @Setter List<String> specialisations;
    private @Getter @Setter Map<GodStatType, GodBaseStat> baseStats;
    private @Getter @Setter Abilities abilities;
    private @Getter @Setter Aspect aspect;

    public enum Role {
        SOLO,
        MID,
        CARRY,
        SUPPORT,
        JUNGLE
    }

    public enum DamageType {
        PHYSICAL,
        MAGICAL
    }

    public enum DamageRange {
        MELEE,
        RANGED
    }
}
