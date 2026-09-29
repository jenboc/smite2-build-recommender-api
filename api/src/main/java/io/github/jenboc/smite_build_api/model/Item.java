package io.github.jenboc.smite_build_api.model;

import java.lang.String;
import java.util.List;
import java.util.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Item {
    private @Getter String name;
    private @Getter Integer tier;
    private @Getter String category;
    private @Getter float cost;
    private @Getter float totalCost;
    private @Getter Dictionary<String, ValueUnit> stats;
    private @Getter String passiveEffect;
    private @Getter String activeEffect;
    private @Getter List<String> notes;
}
