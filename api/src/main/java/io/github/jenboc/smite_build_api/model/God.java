package io.github.jenboc.smite_build_api.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

/**
 * Represents a single SMITE 2 god.
 *
 * <p>In SMITE 2, a god is a playable character</p>
 */
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

    /**
     * Represents a possible role that the god may fulfill
     *
     * <p>Roles are only suggestions, not hard constraints. They describe how
     * the god is typically used in the Conquest game mode</p>
     */
    public enum Role {
        SOLO,
        MID,
        CARRY,
        SUPPORT,
        JUNGLE
    }

    /**
     * Represents the god's core damage type
     *
     * <p>In SMITE 2, god's can either deal physical or magical damage</p>
     */
    public enum DamageType {
        PHYSICAL,
        MAGICAL
    }

    /**
     * Represents the range at which a god typically deals damage
     * 
     * <p>In SMITE 2, god's are either melee and ranged. Note, however,
     * that some melee god's may have abilities with some range</p>
     */
    public enum DamageRange {
        MELEE,
        RANGED
    }
}
