package io.github.jenboc.smite_build_api.model;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

/**
 * Represents a single component in a "75% Intelligence + 25% Strength" string
 * (i.e. 75% Intelligence)
 */
@NoArgsConstructor
@AllArgsConstructor
public class ScaledComponent {
    private @Getter @Setter ValueUnit valueUnit;
    private @Getter @Setter String of;
}
