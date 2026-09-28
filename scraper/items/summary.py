from bs4 import BeautifulSoup
import re


def parse_item_type(s: str) -> dict:
    if s == "Relic":
        return {"tier": None, "type": "Relic"}

    split = s.split(" ")
    data = {"tier": int(split[1])}

    if len(split) > 2:
        data["type"] = " ".join(split[2:])

    return data


def parse_item_stats(s: str) -> dict:
    pattern = re.compile(
        r'([-+]?\d+\.?\d*)(%)?\s*([A-Z][a-zA-Z]*(?:\s+[A-Z][a-zA-Z]*)*)'
    )

    stats = {}
    for value, percent_sign, name in pattern.findall(s):
        key = name.strip().lower().replace(" ", "_")
        stats[key] = {
            "value": float(value),
            "unit": "percent" if percent_sign else "flat"
        }

    return stats


def parse_item_summary(soup: BeautifulSoup) -> dict:
    infobox = soup.find("table", class_="infobox")
    data = {}

    data["name"] = infobox.find("th", class_="title").get_text(strip=True)
    infobox_rows = infobox.find_all("tr")

    box_data = {
        r.find("th").text.strip(): r.find("td").text.strip()
        for r in infobox_rows
        if r.find("th") is not None and r.find("td") is not None
    }

    item_type = parse_item_type(box_data["Item Type:"])
    data["tier"] = item_type["tier"]
    data["category"] = item_type["type"]
    data["cost"] = int(box_data["Cost:"])

    try:
        data["total_cost"] = int(box_data["Total Cost:"])
    except (ValueError, KeyError):
        data["cost"]

    data["stats"] = parse_item_stats(box_data["Stats:"])

    data["passive_effect"] = (
        box_data["Passive Effect:"].replace("\n", " ")
        if box_data["Passive Effect:"] else None
    )
    data["active_effect"] = (
        box_data["Active Effect:"].replace("\n", " ")
        if box_data["Active Effect:"] else None
    )

    return data
