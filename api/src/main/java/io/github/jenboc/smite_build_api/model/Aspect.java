package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Aspect {
    private @Getter String name;
    private @Getter String description;
    private @Getter AbilityOverrides modifies;
}
