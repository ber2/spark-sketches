import numpy as np
import numpy.typing as npt


def jaccard(left: npt.NDArray[np.uint64], right: npt.NDArray[np.uint64]) -> float:
    if left.shape != right.shape:
        raise ValueError("Cannot compute Jaccard index of arrays with unequal shapes")

    array_length = 1.0 * left.shape[0]
    matching = (left == right).astype(np.int64).sum()
    return matching / array_length
