from bs4 import BeautifulSoup

from gods.abilities import parse_ability_block, merge_slot
from models.abilities import Aspect, AbilityOverrides


def parse_aspect_info(info_table: BeautifulSoup) -> dict:
    dds = info_table.find_all("dd")

    return {
        "name": dds[0].get_text(strip=False),
        "description": dds[1].get_text(strip=False)
    }


def parse_aspect_section(soup: BeautifulSoup) -> Aspect:
    aspect_h2 = soup.find("h2", id="God_Aspect")

    if aspect_h2 is None:
        return None

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
    modifies_data = {}
    for d in blocks:
        for k, v in d.items():
            merge_slot(modifies_data, k, v)

    aspect["modifies"] = AbilityOverrides.model_validate(modifies_data)

    return Aspect.model_validate(aspect)
