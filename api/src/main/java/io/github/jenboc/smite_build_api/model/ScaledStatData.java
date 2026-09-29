package io.github.jenboc.smite_build_api.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@AllArgsConstructor
public class ScaledStatData extends StatData {
    private @Getter @Setter List<ScaledComponent> components;
}
