package io.github.jenboc.smite_build_api.model;

import java.util.Map;
import java.lang.String;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a god's full ability kit
 */
@NoArgsConstructor
@AllArgsConstructor
public class Abilities {
    private @Getter @Setter Map<String, AbilitySet> stances;
}
