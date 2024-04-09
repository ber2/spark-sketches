from .theta import Theta


def get_estimate(a: Theta) -> float:
    return len(a.hashes) / a.theta
