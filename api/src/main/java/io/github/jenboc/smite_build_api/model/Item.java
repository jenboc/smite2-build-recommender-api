package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private @Getter @Setter String name;
    private @Getter @Setter Integer tier;
    private @Getter @Setter Category category;
    private @Getter @Setter double cost;
    private @Getter @Setter double totalCost;
    private @Getter @Setter Map<String, ValueUnit> stats;
    private @Getter @Setter String passiveEffect;
    private @Getter @Setter String activeEffect;
    private @Getter @Setter List<String> notes;

    public enum Category {
        RELIC,
        STARTER,
        OFFENSIVE,
        DEFENSIVE,
        HYBRID
    }
}
