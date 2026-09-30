package io.github.jenboc.smite_build_api.chunking;

import java.util.Map;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jenboc.smite_build_api.model.Item;

@Component
public class ItemChunker extends Chunker<Item> {
   
    @Override
    public List<Chunk> chunk(Item obj) {
        return List.of(
            new Chunk(buildChunkText(obj), buildMetadata(obj))
        );
    }

    private String buildChunkText(Item item) {
        StringBuilder sb = new StringBuilder();

        sb.append(formatHeader(item)).append("\n");
        sb.append("Cost: ").append(formatCost(item)).append("\n");

        appendIfPresent(sb, "Stats", formatStats(item.getStats()));
        appendIfPresent(sb, "Passive", item.getPassiveEffect());
        appendIfPresent(sb, "Active", item.getActiveEffect());
        appendIfPresent(sb, "Notes", formatNotes(item.getNotes()));

        return sb.toString().strip();
    }

    private Map<String, String> buildMetadata(Item item) {
        return Map.of(
            "type", "item",
            "name", item.getName(),
            "category", readableEnumName(item.getCategory())
        );
    }

    private String formatHeader(Item item) {
        String categoryName = readableEnumName(item.getCategory());
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
}
