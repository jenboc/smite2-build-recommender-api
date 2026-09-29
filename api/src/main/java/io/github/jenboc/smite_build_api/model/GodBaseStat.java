package io.github.jenboc.smite_build_api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class GodBaseStat {
    private @Getter ValueUnit base;
    private @Getter ValueUnit perLevel;
}
