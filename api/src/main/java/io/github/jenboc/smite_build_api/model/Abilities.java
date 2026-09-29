package io.github.jenboc.smite_build_api.model;

import java.util.Dictionary;
import java.lang.String;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Abilities {
    private @Getter Dictionary<String, Stance> stances;
}
