package io.github.jenboc.smite_build_api.model;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class ValueUnit {
    private @Getter @Setter float value;
    private @Getter @Setter Unit unit;

    public enum Unit {
        FLAT,
        PERCENT
    }
}
