package io.github.jenboc.smite_build_api.model;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single value bundled with its unit
 */
@NoArgsConstructor
@AllArgsConstructor
public class ValueUnit {
    private @Getter @Setter double value;
    private @Getter @Setter Unit unit;

    // In this case, the value is either a flat value or a percentage
    public enum Unit {
        FLAT,
        PERCENT
    }
}
