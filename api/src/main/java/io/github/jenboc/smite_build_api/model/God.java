package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import java.util.List;
import java.util.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class God {
    private @Getter String name;
    private @Getter List<String> roles;
    private @Getter String damageType;
    private @Getter String damageRange;
    private @Getter List<String> specialisations;
    private @Getter Dictionary<String, GodBaseStat> baseStats;
    private @Getter Abilities abilities;
    private @Getter Aspect aspect;
}
