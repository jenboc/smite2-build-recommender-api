package io.github.jenboc.smite_build_api.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TieredStatData.class, name = "tiered"),
    @JsonSubTypes.Type(value = ScaledStatData.class, name = "scaled"),
    @JsonSubTypes.Type(value = TextStatData.class, name = "text")
})
public class StatData {
}
