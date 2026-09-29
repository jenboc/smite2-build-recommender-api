package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class AbilitySet {
    private @Getter Ability basicAttack;
    private @Getter Ability passive;
    private @Getter Ability first;
    private @Getter Ability second;
    private @Getter Ability third;
    private @Getter Ability ultimate;
}
