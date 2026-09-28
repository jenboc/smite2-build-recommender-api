from items.summary import parse_item_summary
from bs4 import BeautifulSoup
import requests

from models.records import Item


def parse_item_notes(soup: BeautifulSoup) -> dict:
    notes_h2 = soup.find("h2", id="Notes")

    if notes_h2 is None:
        return []

    notes_div = notes_h2.find_parent("div")

    # ul containing notes is the very next element
    notes_ul = notes_div.find_next_sibling()

    if notes_ul.name != "ul":
        return []

    return [
        n.get_text(strip=False)
        for n in notes_ul.find_all("li")
    ]


def get_item_data(url: str) -> Item:
    resp = requests.get(url)
    soup = BeautifulSoup(resp.text, "html.parser")

    data = parse_item_summary(soup)
    data["notes"] = parse_item_notes(soup)

    return Item.model_validate(data)
