package io.github.jenboc.smite_build_api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class AbilitySet {
    private @Getter @Setter List<Ability> basicAttack;
    private @Getter @Setter List<Ability> passive;
    private @Getter @Setter @JsonProperty("1") List<Ability> first;
    private @Getter @Setter @JsonProperty("2") List<Ability> second;
    private @Getter @Setter @JsonProperty("3") List<Ability> third;
    private @Getter @Setter List<Ability> ultimate;
}
