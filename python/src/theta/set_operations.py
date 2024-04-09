from functools import reduce
from typing import List, Set

import numpy as np

from .theta import Theta, empty_sketch
from .constants import MAX_VALUE


def union(a: Theta, b: Theta) -> Theta:
    theta = min(a.theta, b.theta)

    hashes = set(filter(lambda x: x < theta * MAX_VALUE, a.hashes.union(b.hashes)))
    return Theta(theta, hashes)


def multiple_union(sketches: List[Theta]) -> Theta:
    result = empty_sketch
    for sketch in sketches:
        result = union(result, sketch)
    return result


def intersection(a: Theta, b: Theta) -> Theta:
    theta = min(a.theta, b.theta)
    hashes = set(
        filter(lambda x: x < theta * MAX_VALUE, a.hashes.intersection(b.hashes))
    )
    return Theta(theta, hashes)


def a_not_b(a: Theta, b: Theta) -> Theta:
    hashes = a.hashes.difference(b.hashes)
    return Theta(a.theta, hashes)
