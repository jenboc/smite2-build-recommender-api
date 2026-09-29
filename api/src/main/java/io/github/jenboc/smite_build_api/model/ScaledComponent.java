package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ScaledComponent {
    private @Getter float value;
    private @Getter String unit;
    private @Getter String of;
}
