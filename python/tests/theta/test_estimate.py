import pytest

from theta.estimate import get_estimate
from theta.theta import Theta, empty_sketch

from conftest import load_sketch


def test_estimation_of_empty_sketch():
    assert get_estimate(empty_sketch) == 0.0


@pytest.mark.parametrize(
    ["key", "expected_count"],
    [
        ("key_10", 10032.660262905873),
        ("key_6", 9765.621675265083),
        ("key_9", 9921.627669041494),
        ("key_1", 9936.380891623185),
        ("key_7", 9853.381218344617),
        ("key_5", 9643.670278823227),
        ("key_0", 9985.538666372871),
        ("key_2", 9857.609393504095),
        ("key_8", 9886.857090292508),
        ("key_3", 9848.071387484875),
        ("key_4", 9797.702815120301),
    ],
)
def test_estimation(key, expected_count):
    sketch = load_sketch(key)
    actual_count = get_estimate(sketch)
    pytest.approx(expected_count, actual_count)


def test_estimation_joint(joint_theta):
    expected_count = 59705.785016
    actual_count = get_estimate(joint_theta)
    pytest.approx(expected_count, actual_count)
