from bs4 import BeautifulSoup
from tqdm import tqdm
from argparse import ArgumentParser
import requests
import time
import random
import json
import os

from gods import get_god_data
from items import get_item_data
from manifest import create_manifest

WIKI_HOME = "https://wiki.smite2.com"
DATA_ROOT = "./data"

SCRAPE_ITEM_H3_IDS = {
    "Relics_2",
    "Upgraded_Starters",
    "Tier_III_-_Offensive",
    "Tier_III_-_Defensive",
    "Tier_III_-_Hybrid"
}


def sleep_random_delay() -> None:
    time.sleep(random.uniform(0.5, 1.5))


def get_god_list() -> list[str]:
    resp = requests.get(WIKI_HOME)
    soup = BeautifulSoup(resp.text, "html.parser")

    god_icons = soup.find_all("div", class_="roster-item")

    god_urls = []
    for icon in god_icons:
        anchor = icon.find("a")
        god_urls.append(f"{WIKI_HOME}{anchor.get('href')}")

    print(f"[+] Found {len(god_urls)} gods")
    return god_urls


def get_item_list() -> list[str]:
    resp = requests.get(os.path.join(WIKI_HOME, "w", "Items"))
    soup = BeautifulSoup(resp.text, "html.parser")

    item_urls = []

    for h3_id in SCRAPE_ITEM_H3_IDS:
        h3 = soup.find("h3", id=h3_id)
        item_table = h3.find_parent("div").find_next_sibling(
            "table",
            class_="wikitable"
        )

        hrefs = [a_tag.get("href") for a_tag in item_table.find_all("a")]
        item_urls += [
            f"{WIKI_HOME}{href}"
            for href in dict.fromkeys(hrefs)
        ]

    print(f"[+] Found {len(item_urls)} items")
    return item_urls


def save_json(data: dict, output_dir: str) -> None:
    os.makedirs(output_dir, exist_ok=True)

    filename = data["name"].lower().replace(" ", "_").replace("'", "") + ".json"
    filepath = os.path.join(output_dir, filename)

    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)


def scrape_gods(outpath: str = DATA_ROOT) -> None:
    gods_urls = get_god_list()

    for url in tqdm(gods_urls):
        sleep_random_delay()
        god_data = get_god_data(url)
        save_json(god_data, os.path.join(outpath, "gods"))


def scrape_items(outpath: str = DATA_ROOT) -> None:
    item_urls = get_item_list()

    for url in tqdm(item_urls):
        sleep_random_delay()
        item_data = get_item_data(url)
        save_json(item_data, os.path.join(outpath, "items"))


if __name__ == "__main__":
    parser = ArgumentParser(
        prog="SMITE 2 WIKI Scraper",
        description="Scrapes item and god information from wiki for RAG app"
    )

    parser.add_argument("--skip-gods", "-g", action="store_true")
    parser.add_argument("--skip-items", "-i", action="store_true")
    parser.add_argument("--outpath", "-o", type=str, default=DATA_ROOT)
    parser.add_argument("--manifest", "-m", type=str, default="TODO")

    args = parser.parse_args()

    if not args.skip_gods:
        print("Scraping Gods:")
        scrape_gods(args.outpath)

    if not args.skip_items:
        print("Scraping Items:")
        scrape_items(args.outpath)

    if args.manifest:
        print("Building Manifest")
        create_manifest(args.outpath, args.manifest)

    print("Done :)")
    print(f"JSON files saved in {args.outpath}")
