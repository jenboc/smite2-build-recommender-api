package io.github.jenboc.smite_build_api.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

/**
 * Represents a single god ability
 */
@NoArgsConstructor
@AllArgsConstructor
public class Ability {
    private @Getter @Setter String name;
    private @Getter @Setter String variant;
    private @Getter @Setter List<String> tags;
    private @Getter @Setter String description;
    private @Getter @Setter List<AbilityStat> stats;
    private @Getter @Setter List<String> notes;
}
