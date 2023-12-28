from dataclasses import dataclass
from typing import Set

import numpy as np

@dataclass
class Theta:
    theta: float
    hashes: Set[np.int64]


empty_sketch = Theta(1.0, set())
