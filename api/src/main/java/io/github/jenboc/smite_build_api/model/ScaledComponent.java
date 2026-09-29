package io.github.jenboc.smite_build_api.model;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@NoArgsConstructor
@AllArgsConstructor
public class ScaledComponent {
    private @Getter @Setter ValueUnit valueUnit;
    private @Getter @Setter String of;
}
