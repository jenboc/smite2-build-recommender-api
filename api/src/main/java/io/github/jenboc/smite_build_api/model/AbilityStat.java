package io.github.jenboc.smite_build_api.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class AbilityStat {
    private @Getter @Setter String name;
    private @Getter @Setter StatData data;
}
