package io.github.jenboc.smite_build_api.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an entire "75% Intelligence + 25% Strength" style string.
 */
@NoArgsConstructor 
@AllArgsConstructor
public class ScaledStatData extends StatData {
    private @Getter @Setter List<ScaledComponent> components;
}
