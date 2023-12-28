import pytest

from theta.estimate import get_estimate
from theta.theta import Theta, empty_sketch

from conftest import load_sketch, compare_floats


def test_estimation_of_empty_sketch():
    assert get_estimate(empty_sketch) == 0.0


@pytest.mark.parametrize(
    ["key", "expected_count"],
    [
        ("key_10", 10046.138362093685),
        ("key_6", 9727.349499608525),
        ("key_9", 9975.8534894143),
        ("key_1", 9955.450789817754),
        ("key_7", 9838.054838833625),
        ("key_5", 9642.942401494389),
        ("key_0", 10053.11305903162),
        ("key_2", 9861.01815298071),
        ("key_8", 9937.512421806423),
        ("key_3", 9844.665694364256),
        ("key_4", 9721.705962611619),
    ],
)
def test_estimation(key, expected_count):
    sketch = load_sketch(key)
    actual_count = get_estimate(sketch)
    compare_floats(actual_count, expected_count)


def test_estimation_joint(joint_theta):
    expected_count = 59732.901909
    actual_count = get_estimate(joint_theta)
    compare_floats(actual_count, expected_count)
