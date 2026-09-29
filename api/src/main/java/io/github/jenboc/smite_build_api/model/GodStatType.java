package io.github.jenboc.smite_build_api.model;

import com.fasterxml.jackson.annotation.JsonAlias;

/*
 * Enum of all God stats
 * Note: Gods only start with a fraction of these
 */
public enum GodStatType {
    @JsonAlias({"ATTACK_POWER"})
    ATTACK_DAMAGE,

    ATTACK_SPEED,

    COOLDOWN_RATE,

    CRITICAL_CHANCE,

    DAMPENING,

    ECHO,

    HEALTH_REGEN,

    INTELLIGENCE,

    LIFESTEAL,

    MAGICAL_PROTECTION,

    MANA_REGEN,

    @JsonAlias({"HEALTH"})
    MAX_HEALTH,

    @JsonAlias({"MANA"})
    MAX_MANA,

    @JsonAlias({"MOVE_SPEED"})
    MOVEMENT_SPEED,

    PATHFINDING,

    PENETRATION,

    PHYSICAL_PROTECTION,

    PLATING,

    STRENGTH,

    TENACITY
}
