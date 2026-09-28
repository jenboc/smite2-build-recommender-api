from gods.abilities import parse_ability_section
from gods.summary import parse_god_summary
from gods.aspects import parse_aspect_section
from bs4 import BeautifulSoup
import requests


def get_god_data(url: str) -> dict:
    resp = requests.get(url)
    soup = BeautifulSoup(resp.text, "html.parser")

    data = parse_god_summary(soup)
    data["abilities"] = parse_ability_section(soup)
    data["aspect"] = parse_aspect_section(soup)

    return data
