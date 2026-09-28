from bs4 import BeautifulSoup
import re


# Parse strings of the form "56.25 (+25%)" or "115 (+30)"
def parse_base_stat_value_string(s: str) -> dict[str, float]:
    match = re.search(r'([-+]?\d+\.?\d*)\s*(%)?', s)

    if match is None:
        return None

    value = float(match.group(1))
    unit = "percent" if match.group(2) else "flat"
    return {"value": value, "unit": unit}


def parse_base_stat(s: str) -> dict:
    matches = re.findall(r'[-+]?\d+\.?\d*%?', s)

    if matches is None or len(matches) == 0:
        return None

    parsed = [parse_base_stat_value_string(m) for m in matches]

    if len(parsed) != 2:
        raise ValueError(f"Expected 3 values, found {len(parsed)}")

    return {"base": parsed[0], "per_level": parsed[1]}


def parse_god_summary(soup: BeautifulSoup) -> dict:
    data = {}
    infobox = soup.find("table", class_="infobox")

    # Find name
    data["name"] = infobox.find("th", class_="title").get_text(strip=True)

    # Get raw summary data
    infobox_rows = infobox.find_all("tr")
    box_data = {
        r.find("th").text.strip(): r.find("td").text.strip()
        for r in infobox_rows
        if r.find("th") is not None and r.find("td") is not None
    }

    # Coerce data into a nicer format
    data["roles"] = box_data["Roles:"].replace(":", "").strip().split(" ")

    attack_type = box_data["Attack Type:"].replace(":", "").strip().split(" ")
    data["damage_type"] = attack_type[1]
    data["damage_range"] = attack_type[0]

    data["specialisations"] = re.findall(
            r'[A-Z][a-z]+(?:\s[A-Z][a-z]+)*',
            box_data["Specializations:"]
    )

    data["base_stats"] = {
        "health": parse_base_stat(box_data["Health:"]),
        "health_regen": parse_base_stat(box_data["Health Regen:"]),
        "mana": parse_base_stat(box_data["Mana:"]),
        "mana_regen": parse_base_stat(box_data["Mana Regen:"]),
        "physical_protection": parse_base_stat(box_data["Physical Pro.:"]),
        "magical_protection": parse_base_stat(box_data["Magical Pro.:"]),
        "attack_speed": parse_base_stat(box_data["Attack Speed:"]),
        "attack_power": parse_base_stat(box_data["Attack Power:"]),
        "move_speed": parse_base_stat(box_data["Move Speed:"])
    }

    return data
