import pytest

from theta.estimate import get_estimate
from theta.set_operations import union, intersection, a_not_b
from theta.theta import Theta

from conftest import load_sketch, compare_floats, compare_floats_rel


@pytest.fixture
def union_of_all_keys() -> Theta:
    sketches = [load_sketch(f"key_{d}") for d in range(10)]
    return union(sketches)


@pytest.mark.xfail
def test_union(union_of_all_keys, joint_theta):
    compare_floats_rel(joint_theta.theta, union_of_all_keys.theta, 1e-3)
    assert len(union_of_all_keys.hashes) == len(joint_theta.hashes)


def test_union_estimate(union_of_all_keys):
    expected_count = 57282.679252
    actual_count = get_estimate(union_of_all_keys)
    compare_floats(expected_count, actual_count)


def test_intersection_estimate():
    # expected_count = 268
    # actual_count = get_estimate(intersection(theta_gb, theta_fr))
    # assert expected_count == actual_count
    assert False


def test_set_difference_estimate():
    # expected_count = 513843
    # actual_count = get_estimate(a_not_b(theta_gb, theta_fr))
    # assert expected_count == actual_count
    assert False
