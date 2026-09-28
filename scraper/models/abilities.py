from pydantic import BaseModel, Field, field_validator, model_validator
from typing import Literal
from models.stats import AbilityStat

SlotName = Literal["basic_attack", "passive", "first", "second", "third",
                   "ultimate"]
ALL_SLOTS: frozenset[SlotName] = frozenset(
    {"basic_attack", "passive", "first", "second", "third", "ultimate"}
)


class Ability(BaseModel):
    name: str = Field(min_length=1)
    variant: str | None = None
    tags: list[str] = Field(default_factory=list)
    description: str
    stats: list[AbilityStat] = Field(default_factory=dict),
    notes: list[str] = Field(default_factory=list)


class AbilityOverrides(BaseModel):
    """A partially-complete ability set. Used for both partial stances
    and aspect modifications"""
    basic_attack: list[Ability] = Field(default_factory=list)
    passive: list[Ability] = Field(default_factory=list)
    first: list[Ability] = Field(default_factory=list, alias="1")
    second: list[Ability] = Field(default_factory=list, alias="2")
    third: list[Ability] = Field(default_factory=list, alias="3")
    ultimate: list[Ability] = Field(default_factory=list)

    model_config = {"populate_by_name": True}

    def filled_slots(self) -> set[SlotName]:
        return {slot for slot in ALL_SLOTS if getattr(self, slot)}


class Stance(AbilityOverrides):
    """An ability set a god can switch to mid-match"""
    pass


class Aspect(BaseModel):
    name: str
    description: str
    modifies: AbilityOverrides = Field(default_factory=AbilityOverrides)


class Abilities(BaseModel):
    stances: dict[str, Stance] = Field(min_length=1)

    @field_validator("stances")
    @classmethod
    def no_blank_stance_names(cls, v: dict[str, Stance]) -> dict[str, Stance]:
        for name in v:
            if (not isinstance(name, str)
                or not name.strip()
                or name.lower() in ("null", "none")):
                raise ValueError(
                        f"Invalid stance name: {name!r}"
                )
        return v

    @model_validator(mode="after")
    def base_plus_stance_covers_all_slots(self) -> "Abilities":
        base = self.stances.get("base")
        base_slots = base.filled_slots() if base else set()

        for name, stance in self.stances.items():
            if name == "base":
                continue

            combined = base_slots | stance.filled_slots()
            missing = ALL_SLOTS - combined

            if missing:
                raise ValueError(
                    f"Stance {name!r} + Base are missing: {sorted(missing)}"
                )

        if (len(self.stances) == 1
            and "base" in self.stances
            and base_slots != ALL_SLOTS):
            missing = ALL_SLOTS - base_slots
            raise ValueError(
                f"Base is the only stance and is missing: {sorted(missing)}"
            )

        return self
