package io.github.jenboc.smite_build_api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a god's base stat.
 *
 * <p>Base stats provide information about the initial value (at level 1), as well
 * as how much this increases by per level</p>
 */
@NoArgsConstructor 
@AllArgsConstructor
public class GodBaseStat {
    private @Getter @Setter ValueUnit base;
    private @Getter @Setter ValueUnit perLevel;
}
