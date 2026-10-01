package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a "10 | 20 | 30 | 40 degrees" style string
 */
@NoArgsConstructor
@AllArgsConstructor
public class TieredStatData extends StatData {
    private @Getter @Setter List<Double> values;
    private @Getter @Setter String unit;
}
