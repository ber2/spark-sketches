import numpy as np
import numpy.typing as npt

from .constants import MAX_HASH

def count_uniques(minhash: npt.NDArray[np.uint64]) -> float:
    l = minhash.shape[0]
    s: float = np.sum(minhash) / (1.0 * MAX_HASH)
    return l / s - 1.0
