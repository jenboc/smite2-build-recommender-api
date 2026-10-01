package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the data of a statistic which the scraper left as raw text
 */
@NoArgsConstructor 
@AllArgsConstructor
public class TextStatData extends StatData {
    private @Getter @Setter String rawText;
}
