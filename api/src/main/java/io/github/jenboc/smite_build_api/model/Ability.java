package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Ability {
    private @Getter String name;
    private @Getter String variant;
    private @Getter List<String> tags;
    private @Getter String description;
    private @Getter List<AbilityStat> stats;
    private @Getter List<String> notes;
}
