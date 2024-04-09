import pytest

from theta.estimate import get_estimate
from theta.set_operations import union, intersection, a_not_b, multiple_union
from theta.theta import Theta

from conftest import load_sketch


@pytest.fixture
def union_of_all_keys() -> Theta:
    sketches = [load_sketch(f"key_{d}") for d in range(10)]
    return multiple_union(sketches)


@pytest.fixture
def intersection_sketch() -> Theta:
    sketch_0 = load_sketch("key_0_intersection")
    sketch_1 = load_sketch("key_1_intersection")
    return intersection(sketch_0, sketch_1)


@pytest.fixture
def set_difference_sketch() -> Theta:
    sketch_0 = load_sketch("key_0_intersection")
    sketch_1 = load_sketch("key_1_intersection")
    return a_not_b(sketch_0, sketch_1)


def test_union_estimate(union_of_all_keys):
    expected_count = 57282.679252
    actual_count = get_estimate(union_of_all_keys)
    pytest.approx(expected_count, actual_count)


def test_intersection_estimate(intersection_sketch):
    expected_count = 2030.647149
    actual_count = get_estimate(intersection_sketch)
    pytest.approx(expected_count, actual_count)


def test_set_difference_estimate(set_difference_sketch):
    expected_count = 7326.310821
    actual_count = get_estimate(set_difference_sketch)
    pytest.approx(expected_count, actual_count)
