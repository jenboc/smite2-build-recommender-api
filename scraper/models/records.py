from pydantic import BaseModel, Field, field_validator, model_validator
from models.stats import GodBaseStat, ValueUnit
from models.abilities import Abilities, Aspect


class God(BaseModel):
    name: str = Field(min_length=1)
    base_stats: dict[str, GodBaseStat | None]
    abilities: Abilities
    aspect: Aspect | None = None

    @field_validator("base_stat")
    @classmethod
    def mana_may_be_none(cls, v: dict[str, GodBaseStat | None]) -> dict[str, GodBaseStat | None]:
        # (In the case of Manaless Gods, e.g. Bari)
        expected_none = {"mana", "mana_regen"}
        for key, val in v.items():
            if val is None and key not in expected_none:
                raise ValueError(
                    f"base_stats[{key!r}] is None, only " +
                    "{sorted(expected_none)} are allowed to be None"
                )
        return v


class Item(BaseModel):
    name: str = Field(min_length=1)
    tier: int | None = None
    category: str = Field(min_length=1)
    cost: float = Field(ge=0.0)
    total_cost: float = Field(ge=0.0)
    stats: dict[str, ValueUnit] = Field(default_factory=dict)
    passive_effect: str | None = None
    active_effect: str | None = None
    notes: list[str] = Field(default_factory=list)

    @model_validator(mode="before")
    @classmethod
    def default_total_cost_to_cost(cls, data):
        if isinstance(data, dict) and not data.get("total_cost"):
            data["total_cost"] = data.get("cost")
        return data

    @model_validator(mode="after")
    def total_at_least_cost(self) -> "Item":
        if self.total_cost < self.cost:
            raise ValueError(
                f"total_cost ({self.total_cost}) is less than " +
                f"cost ({self.cost})"
            )
        return self
