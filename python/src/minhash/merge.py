from typing import List

import numpy as np
import numpy.typing as npt


def pack_hashes_into_single_array(
    hashes: List[npt.NDArray[np.uint64]],
) -> npt.NDArray[np.uint64]:
    return np.stack(hashes)


def merge_hashes_array(hashes_array: npt.NDArray[np.uint64]) -> npt.NDArray[np.int64]:
    return np.min(hashes_array, axis=0)


def merge_hashes(minhashes: List[npt.NDArray[np.uint64]]) -> npt.NDArray[np.int64]:
    packed = pack_hashes_into_single_array(minhashes)
    return merge_hashes_array(packed)
