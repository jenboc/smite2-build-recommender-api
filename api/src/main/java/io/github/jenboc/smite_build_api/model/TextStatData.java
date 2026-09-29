package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TextStatData extends StatData {
    private @Getter String rawText;
}
