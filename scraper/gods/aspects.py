from bs4 import BeautifulSoup

from gods.abilities import search_for_abilities
from models.abilities import Aspect


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
    ability_block = None

    for sibling in aspect_div.find_next_siblings():
        classes = sibling.get("class", [])
        if sibling.name == "div" and "mw-heading" in classes:
            break
        if sibling.name == "table" and "wikitable" in classes:
            info_table = sibling
        if sibling.name == "div" and "mw-customcollapsible-aspectedability" in sibling.get("id"):
            ability_block = sibling.find("div", class_="mw-collapsible-content")

    if info_table is None:
        return None

    aspect = parse_aspect_info(info_table)

    if ability_block is None:
        return Aspect.model_validate(aspect)

    aspect["modifies"] = search_for_abilities(ability_block.children)
    return Aspect.model_validate(aspect)
