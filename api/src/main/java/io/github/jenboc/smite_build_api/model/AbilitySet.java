package io.github.jenboc.smite_build_api.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a (potentially incomplete) set of god abilities.
 */
@NoArgsConstructor
@AllArgsConstructor
public class AbilitySet {
    private @Getter @Setter List<Ability> basicAttack;
    private @Getter @Setter List<Ability> passive;
    private @Getter @Setter List<Ability> first;
    private @Getter @Setter List<Ability> second;
    private @Getter @Setter List<Ability> third;
    private @Getter @Setter List<Ability> ultimate;
}
