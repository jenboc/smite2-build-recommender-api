package io.github.jenboc.smite_build_api.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

/**
 * Represents a god's aspect
 *
 * <p>In SMITE 2, an aspect is a partial modification to the god's ability kit</p>
*/
@NoArgsConstructor
@AllArgsConstructor
public class Aspect {
    private @Getter @Setter String name;
    private @Getter @Setter String description;
    private @Getter @Setter Map<String, AbilitySet> modifies;
}
