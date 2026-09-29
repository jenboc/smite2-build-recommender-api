package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@AllArgsConstructor
public class TextStatData extends StatData {
    private @Getter @Setter String rawText;
}
