from pydantic import BaseModel, Field, field_validator
from models.types import Unit, TieredUnit
from typing import Annotated, Literal, Union


class ValueUnit(BaseModel):
    """A single number which either represents a flat number or a percentage"""
    value: float
    unit: Unit


class TieredStat(BaseModel):
    """'70 | 115 | 160 | 205 | 250', '8.8 meters', '75%'"""
    type: Literal["tiered"] = "tiered"
    values: list[float] = Field(min_length=1)
    unit: TieredUnit


class ScaledComponent(BaseModel):
    """'70% Intelligence', '70% of Recent Damage'"""
    value_unit: ValueUnit
    of: str

    @field_validator("of")
    @classmethod
    def not_blank(cls, v: str) -> str:
        if not v.strip():
            raise ValueError("'of' must not be blank")
        return v


class ScaledStat(BaseModel):
    """'75% Intelligence + 60% Strength', '22.5% of Recent Damage Taken'"""
    type: Literal["scaled"] = "scaled"
    components: list[ScaledComponent] = Field(min_length=1)


class TextStat(BaseModel):
    """Fallback for anything which does not fit the Scaled/Tiered grammar,
    e.g. '2.5% Physical + Magical Protection' ellipsis"""
    type: Literal["text"] = "text"
    raw: str


class GodBaseStat(BaseModel):
    """'356 (3)', '20.4 (1.24%)"""
    base: ValueUnit
    per_level: ValueUnit


# Pydantic figures out types based on the type attribute
Stat = Annotated[
    Union[TieredStat, ScaledStat, TextStat], Field(discriminator="type")
]


class AbilityStat(BaseModel):
    name: str = Field(min_length=1)
    data: Stat
