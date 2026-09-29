package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TieredStatData extends StatData {
    private @Getter List<Float> values;
    private @Getter String unit;
}
