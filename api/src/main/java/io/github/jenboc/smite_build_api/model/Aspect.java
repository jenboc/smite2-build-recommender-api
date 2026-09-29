package io.github.jenboc.smite_build_api.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@NoArgsConstructor
@AllArgsConstructor
public class Aspect {
    private @Getter @Setter String name;
    private @Getter @Setter String description;
    private @Getter @Setter AbilitySet modifies;
}
