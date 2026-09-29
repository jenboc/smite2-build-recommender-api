package io.github.jenboc.smite_build_api.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ScaledStatData extends StatData {
    private @Getter List<ScaledComponent> components;
}
