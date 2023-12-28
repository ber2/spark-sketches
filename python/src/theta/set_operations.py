from functools import reduce
from typing import List, Set

import numpy as np

from .theta import Theta, empty_sketch
from .constants import MAX_VALUE


def union(sketches: List[Theta]) -> Theta:
    min_theta = 1.0
    all_hashes: Set[np.int64] = set()
    for a in sketches:
        if a.theta < min_theta:
            min_theta = a.theta
        all_hashes = all_hashes.union(a.hashes)

    hashes_to_keep: Set[np.int64] = set()
    for h in all_hashes:
        if h < min_theta * MAX_VALUE:
            hashes_to_keep.add(h)

    print(len(all_hashes), len(hashes_to_keep))
    return Theta(min_theta, hashes_to_keep)


def intersection(a: Theta, b: Theta) -> Theta:
    return empty_sketch


def a_not_b(a: Theta, b: Theta) -> Theta:
    return empty_sketch
