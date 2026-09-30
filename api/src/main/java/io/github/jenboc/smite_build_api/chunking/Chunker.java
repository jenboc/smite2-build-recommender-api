package io.github.jenboc.smite_build_api.chunking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.ValueUnit;


/*
 * Class which takes an object of type T and turns it into a chunk
 * for embedding
 */
public abstract class Chunker<T> {

    public Chunk chunk(T obj) {
        return new Chunk(buildChunkText(obj), buildMetadata(obj));
    }

    protected abstract String buildChunkText(T obj);
    protected abstract Map<String, String> buildMetadata(T obj);

    protected String formatStats(Map<GodStatType, ValueUnit> stats) {
        if (stats == null || stats.isEmpty()) return null;

        List<String> parts = new ArrayList<>();

        stats.forEach((name, vu) -> parts.add(
            formatValueUnit(vu) + " " + readableStatName(name)
        ));

        return String.join(", ", parts);
    }

    protected String formatNotes(List<String> notes) {
        if (notes == null || notes.isEmpty()) return null;

        return String.join(" ", notes);
    }

    protected void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(label).append(": ").append(value).append("\n");
        }
    }

    protected String formatValueUnit(ValueUnit vu) {
        String value = formatNumber(vu.getValue());
        return (vu.getUnit() == ValueUnit.Unit.PERCENT)
            ? value + "%"
            : value;
    }

    protected String readableStatName(GodStatType key) {
        // e.g. MAX_HEALTH -> Max Health
        String name = key.toString();

        String[] words = name.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();

        for (String w : words) {
            result
                .append(Character.toUpperCase(w.charAt(0)))
                .append(w.substring(1))
                .append(" ");
        }

        return result.toString().strip();
    }

    protected String formatNumber(double value) {
        // Trim decimal point off of whole numbers
        return (value == (long)value)
            ? String.valueOf((long)value)
            : String.valueOf(value);
    }

}
