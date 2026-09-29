package io.github.jenboc.smite_build_api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@AllArgsConstructor
public class GodBaseStat {
    private @Getter @Setter ValueUnit base;
    private @Getter @Setter ValueUnit perLevel;
}
