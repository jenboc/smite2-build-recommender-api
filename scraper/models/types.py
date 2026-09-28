from typing import Literal

# Regular Units are either flat numbers or percentages
Unit = Literal["flat", "percent"]

# Keep open ended, hard to constrain
TieredUnit = str
