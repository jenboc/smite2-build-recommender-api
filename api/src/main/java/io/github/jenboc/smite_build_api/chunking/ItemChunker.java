package io.github.jenboc.smite_build_api.chunking;

import java.util.Map;

import org.springframework.stereotype.Component;

import io.github.jenboc.smite_build_api.model.Item;

@Component
public class ItemChunker extends Chunker<Item> {
    
    @Override
    protected String buildChunkText(Item item) {
        StringBuilder sb = new StringBuilder();

        sb.append(formatHeader(item)).append("\n");
        sb.append("Cost: ").append(formatCost(item)).append("\n");

        appendIfPresent(sb, "Stats", formatStats(item.getStats()));
        appendIfPresent(sb, "Passive", item.getPassiveEffect());
        appendIfPresent(sb, "Active", item.getActiveEffect());
        appendIfPresent(sb, "Notes", formatNotes(item.getNotes()));

        return sb.toString().strip();
    }

    @Override
    protected Map<String, String> buildMetadata(Item item) {
        return Map.of(
            "type", "item",
            "name", item.getName(),
            "category", readableCategoryName(item.getCategory())
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

    private String readableCategoryName(Item.Category category) {
        // e.g. RELIC -> Relic
        // Unlike GodStatType, there are no spaces here.
        String name = category.toString();
        StringBuilder result = new StringBuilder();

        result
            .append(name.charAt(0))
            .append(name.substring(1).toLowerCase());

        return result.toString();
    }
}
