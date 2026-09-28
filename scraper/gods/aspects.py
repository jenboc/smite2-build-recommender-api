from gods.abilities import parse_ability_block
from bs4 import BeautifulSoup


def parse_aspect_info(info_table: BeautifulSoup) -> dict:
    dds = info_table.find_all("dd")

    return {
        "name": dds[0].get_text(strip=False),
        "description": dds[1].get_text(strip=False)
    }


def parse_aspect_section(soup: BeautifulSoup) -> dict:
    aspect_h2 = soup.find("h2", id="God_Aspect")

    if aspect_h2 is None:
        return {}

    aspect_div = aspect_h2.find_parent("div")

    # Neighbouring wikitable has aspect name and description

    # Neigbouring mw-customcollapsable-aspectedability contains
    # a mw-collapsible-content which cotnains wikitables of
    # changed abilities

    info_table = None
    changed_abilities = []

    for sibling in aspect_div.find_next_siblings():
        classes = sibling.get("class", [])
        if sibling.name == "div" and "mw-heading" in classes:
            break
        if sibling.name == "table" and "wikitable" in classes:
            info_table = sibling
        if sibling.name == "div" and "mw-customcollapsible-aspectedability" in sibling.get("id"):
            changed_abilities = sibling.find_all("table", class_="wikitable")

    if info_table is None:
        return {}

    aspect = parse_aspect_info(info_table)

    if len(changed_abilities) == 0:
        return aspect

    blocks = [parse_ability_block(block) for block in changed_abilities]
    aspect["changed_abilities"] = {k: v for d in blocks for k, v in d.items()}

    return aspect
