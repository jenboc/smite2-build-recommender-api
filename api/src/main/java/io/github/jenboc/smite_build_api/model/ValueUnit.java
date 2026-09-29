package io.github.jenboc.smite_build_api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ValueUnit {
    private @Getter float value;
    private @Getter String unit;
}
