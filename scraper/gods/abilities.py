from bs4 import BeautifulSoup
import re

from models.stats import (TieredStat, ScaledComponent,
                          ScaledStat, TextStat, AbilityStat)
from models.abilities import Ability, Stance, Abilities


TITLE_WORD_RE = re.compile(r'\b[A-Z][a-zA-Z]*\b')
COMPONENT_RE = re.compile(
    r'^\s*([-+]?\d+\.?\d*)(%)?\s+(?:(?:of|per|for)\s+)?(.+)$',
    re.IGNORECASE
)


def classify_value(raw_value: str) -> str:
    """A line is 'scaled' if, after stripping numbers/%, a Title Case word remains."""
    stripped = re.sub(r'[-+]?\d+\.?\d*%?', '', raw_value)
    return "scaled" if TITLE_WORD_RE.search(stripped) else "tiered"


def parse_tiered(raw_value: str) -> TieredStat:
    if raw_value.endswith("%"):
        unit, body = "percent", raw_value[:-1]
    else:
        match = re.search(r'([a-z]+)$', raw_value)  # units are lowercase
        if match:
            unit, body = match.group(1), raw_value[:match.start()]
        else:
            unit, body = "flat", raw_value

    values = [float(v.strip()) for v in body.split("|")]  # let ValueError propagate
    return TieredStat(values=values, unit=unit)


def parse_scaled_component(term: str) -> ScaledComponent:
    match = COMPONENT_RE.match(term.strip())
    if not match:
        raise ValueError(f"Not a valid scaled component: {term!r}")
    value, percent_sign, stat = match.groups()

    return ScaledComponent(
        value=float(value),
        unit="percent" if percent_sign else "flat",
        of=stat.strip()
    )


def parse_scaled(raw_value: str) -> ScaledStat:
    components = [parse_scaled_component(term) for term in raw_value.split("+")]
    return ScaledStat(components=components)


def parse_ability_statistic(line: str) -> AbilityStat:
    label, _, raw_value = line.partition(":")
    label, raw_value = label.strip(), raw_value.strip()

    if not label:
        return None

    try:
        parsed = parse_scaled(raw_value) if classify_value(raw_value) == "scaled" else parse_tiered(raw_value)
    except (ValueError, AttributeError):
        parsed = TextStat(raw=raw_value)

    return AbilityStat(name=label, data=parsed)


def parse_ability_block(block: BeautifulSoup) -> Ability:
    trs = block.find_all("tr")

    # Notes are given in as a list in <td> in first row
    notes = [
        n.get_text(strip=False)
        for n in trs[0].find("td").find_all("li")
    ]

    # First row contains:
    # - Ability type: passive, ultimate, 1st, 2nd, 3rd, basic attack
    # - Ability name
    # - Tags
    header_text = trs[0].find("th").get_text(strip=True)
    type_str = header_text.split("-")[0].lower()

    key = None
    variant = None
    if type_str == "basic attack":
        key = "basic_attack"
    elif type_str == "passive":
        key = "passive"
    elif type_str.startswith("ultimate"):
        key = "ultimate"
        variant = (
            type_str
            .replace("ultimate", "")
            .replace("(", "")
            .replace(")", "")
            .strip().title()
        )
    elif type_str == "1st ability":
        key = "1"
    elif type_str == "2nd ability":
        key = "2"
    elif re.match(r"\d(st|nd|rd) ability", type_str):
        key = type_str[0]

    if key is None:
        print(f"[!] Ability type not found ({type_str}), leaving raw string as key")
        key = type_str

    raw_metadata = header_text.split("-")[1].split("|")
    name = raw_metadata[0].title()

    tags = []
    if len(raw_metadata) > 1:
        tags = [x.strip().title() for x in re.split(r"[,;]", raw_metadata[1])]

    # Second row contains a description
    description = trs[1].find_all("td")[1].get_text(strip=False).strip()

    # Third row contains a list of
    # - Level Scaling (70 | 115 | 160 | 205 | 250)
    # - Damage Scaling (75% intelligence + 60% strength)
    # - Effects
    list_items = [
        n.get_text(strip=True)
        for n in trs[2].find_all("li")
    ]

    stats = [parse_ability_statistic(line) for line in list_items]

    obj = Ability(
        name=name,
        variant=variant,
        tags=tags,
        description=description,
        stats=[s for s in stats if s],
        notes=notes
    )

    return {key: obj}


def merge_slot(target: dict, key: str, value: dict, allow_none=False) -> None:
    target.setdefault(key, []).append(value)


def parse_stance_div(div: BeautifulSoup) -> Stance:
    ability_tables = div.find_all("table", class_="wikitable")
    blocks = [parse_ability_block(block) for block in ability_tables]

    stance_data = {}
    for d in blocks:
        for k, v in d.items():
            merge_slot(stance_data, k, v)

    return {div.get("id"): Stance.model_validate(stance_data)}


def parse_ability_section(soup: BeautifulSoup) -> Abilities:
    heading_h2 = soup.find("h2", id="Abilities")
    heading_div = heading_h2.find_parent("div")

    ability_tables = []
    stance_divs = []
    for sibling in heading_div.find_next_siblings():
        if sibling.name == "div" and "mw-heading" in sibling.get("class", []):
            break
        if sibling.name == "div" and "img-tab-wrapper" in sibling.get("class", []):
            stance_divs = sibling.find_all("div", class_="img-tab-panel")
        if sibling.name == "table" and "wikitable" in sibling.get("class", []):
            ability_tables.append(sibling)

    blocks = [parse_ability_block(b) for b in ability_tables]
    stances = {}

    if len(stance_divs) == 1:
        raise ValueError(f"Found {len(stance_divs)} stances, expected 0 or >2")

    if len(stance_divs) > 0:
        stances = {
            k: v
            for div in stance_divs
            for k, v in parse_stance_div(div).items()
        }

    stances["base"] = {}
    for d in blocks:
        for k, v in d.items():
            merge_slot(stances["base"], k, v)

    stances["base"] = Stance.model_validate(stances["base"])

    return Abilities(stances=stances)
