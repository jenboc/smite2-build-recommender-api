package io.github.jenboc.smite_build_api.model;

import com.fasterxml.jackson.annotation.JsonAlias;

/*
 * Enum of all God stats
 * Note: Gods only start with a fraction of these
 */
public enum GodStatType {
    // Raw attack damage is also referred to as attack power
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

    // Also referred to as simply "health" in the case of god base stats
    @JsonAlias({"HEALTH"})
    MAX_HEALTH,

    // Also referred to as simply "mana" in the case of god base stats
    @JsonAlias({"MANA"})
    MAX_MANA,

    // Also referred to as simply "move speed"
    @JsonAlias({"MOVE_SPEED"})
    MOVEMENT_SPEED,

    PATHFINDING,

    PENETRATION,

    PHYSICAL_PROTECTION,

    PLATING,

    STRENGTH,

    TENACITY
}
