package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single SMITE 2 tier 3 item, starter item or relic
 *
 * <p>In SMITE 2, an item is an object which the player can buy from the
 * shop in order to boost their god's stats, and gain a passive or active ability</p>
 */
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private @Getter @Setter String name;
    private @Getter @Setter Integer tier;
    private @Getter @Setter Category category;
    private @Getter @Setter double cost;
    private @Getter @Setter double totalCost;
    private @Getter @Setter Map<GodStatType, ValueUnit> stats;
    private @Getter @Setter String passiveEffect;
    private @Getter @Setter String activeEffect;
    private @Getter @Setter List<String> notes;

    public enum Category {
        RELIC,
        STARTER,
        OFFENSIVE,
        DEFENSIVE,
        HYBRID
    }
}
