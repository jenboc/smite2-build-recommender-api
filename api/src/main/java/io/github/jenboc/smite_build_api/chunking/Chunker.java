package io.github.jenboc.smite_build_api.chunking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.ValueUnit;


/**
 * Class responsible for breaking an object into chunks
 * @see Chunk
 */
public abstract class Chunker<T> {

    /**
     * Break an object down into chunks
     * @see Chunk
     */
    public abstract List<Chunk> chunk(T obj);

    protected String formatStats(Map<GodStatType, ValueUnit> stats) {
        if (stats == null || stats.isEmpty()) return null;

        List<String> parts = new ArrayList<>();

        stats.forEach((name, vu) -> parts.add(
            formatValueUnit(vu) + " " + readableEnumName(name)
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

    protected String readableEnumName(Enum<?> key) {
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
