package io.github.jenboc.smite_build_api.chunking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.github.jenboc.smite_build_api.model.GodStatType;
import io.github.jenboc.smite_build_api.model.Item;
import io.github.jenboc.smite_build_api.model.ValueUnit;

@Component
public class ItemChunker {
    
    public Chunk chunk(Item item) {
        StringBuilder sb = new StringBuilder();

        sb.append(formatHeader(item)).append("\n");
        sb.append("Cost: ").append(formatCost(item)).append("\n");

        appendIfPresent(sb, "Stats", formatStats(item.getStats()));
        appendIfPresent(sb, "Passive", item.getPassiveEffect());
        appendIfPresent(sb, "Active", item.getActiveEffect());
        appendIfPresent(sb, "Notes", formatNotes(item.getNotes()));

        return new Chunk(
            sb.toString().strip(),
            buildMetadata(item)
        );
    }

    private String formatHeader(Item item) {
        String categoryName = readableCategoryName(item.getCategory());
        String bracket = (item.getTier() != null)
            ? "Tier " + item.getTier() + " " + categoryName
            : categoryName;

        return item.getName() + " (" + bracket + ")";
    }

    private String formatCost(Item item) {
        String cost = formatNumber(item.getCost());
        String total = formatNumber(item.getTotalCost());

        return cost + " (" + total + " total)";
    }

    private String formatStats(Map<GodStatType, ValueUnit> stats) {
        if (stats == null || stats.isEmpty()) return null;

        List<String> parts = new ArrayList<>();

        stats.forEach((name, vu) -> parts.add(
            formatValueUnit(vu) + " " + readableStatName(name)
        ));

        return String.join(", ", parts);
    }

    private String formatNotes(List<String> notes) {
        if (notes == null || notes.isEmpty()) return null;

        return String.join(" ", notes);
    }

    private void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(label).append(": ").append(value).append("\n");
        }
    }

    private String formatValueUnit(ValueUnit vu) {
        String value = formatNumber(vu.getValue());
        return (vu.getUnit() == ValueUnit.Unit.PERCENT)
            ? value + "%"
            : value;
    }

    private String readableStatName(GodStatType key) {
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

    private String formatNumber(double value) {
        // Trim decimal point off of whole numbers
        return (value == (long)value)
            ? String.valueOf((long)value)
            : String.valueOf(value);
    }

    private Map<String, String> buildMetadata(Item item) {
        return Map.of(
            "type", "item",
            "name", item.getName(),
            "category", readableCategoryName(item.getCategory())
        );
    }

    private String readableCategoryName(Item.Category category) {
        // e.g. RELIC -> relic
        // Unlike GodStatType, there are no spaces here.
        String name = category.toString();
        StringBuilder result = new StringBuilder();

        result
            .append(name.charAt(0))
            .append(name.substring(1).toLowerCase());

        return result.toString();
    }
}
