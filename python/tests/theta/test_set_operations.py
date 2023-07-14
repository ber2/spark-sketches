import pytest

from theta.estimate import get_estimate
from theta.set_operations import union, intersection, a_not_b
from theta.theta import Theta


@pytest.fixture
def union_of_gb_and_fr(theta_gb, theta_fr) -> Theta:
    return union(theta_gb, theta_fr)


def test_union(union_of_gb_and_fr, joint_theta):
    assert union_of_gb_and_fr.theta == joint_theta.theta
    assert union_of_gb_and_fr.hashes == joint_theta.hashes

def test_union_estimate(union_of_gb_and_fr):
    expected_count = 704278
    actual_count = get_estimate(union_of_gb_and_fr)
    assert expected_count == actual_count

def test_intersection_estimate(theta_gb, theta_fr):
    expected_count = 268
    actual_count = get_estimate(intersection(theta_gb, theta_fr))
    assert expected_count == actual_count

def test_set_difference_estimate(theta_gb, theta_fr):
    expected_count = 513843
    actual_count = get_estimate(a_not_b(theta_gb, theta_fr))
    assert expected_count == actual_count
