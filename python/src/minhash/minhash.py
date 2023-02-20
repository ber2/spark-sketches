from dataclasses import dataclass

import numpy as np
import numpy.typing as npt

from .constants import MAX_HASH


def trivial_minhash(num_perm: int = 256) -> npt.NDArray[np.uint64]:
    return MAX_HASH * np.ones(num_perm, dtype=np.uint64)
