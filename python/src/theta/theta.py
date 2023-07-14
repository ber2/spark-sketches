from dataclasses import dataclass
from typing import Set

import numpy as np

@dataclass
class Theta:
    theta: float
    hashes: Set[np.int64]


def empty_sketch() -> Theta:
    return Theta(0.0, set())
