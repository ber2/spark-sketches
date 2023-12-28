import pytest

from theta.deserialize import deserialize
from theta.theta import Theta


@pytest.fixture
def serialized_joint_theta() -> bytes:
    with open("tests/theta/resources/joint.bin", "rb") as fp:
        data_bin = fp.read()

    return data_bin


@pytest.fixture
def joint_theta(serialized_joint_theta) -> Theta:
    return deserialize(serialized_joint_theta)


def load_sketch(key: str) -> Theta:
    with open(f"tests/theta/resources/{key}.bin", "rb") as fp:
        sketch = fp.read()
    return deserialize(sketch)


def compare_floats(expected: float, actual: float, tol: float = 1e-2):
    assert abs(expected - actual) < tol


def compare_floats_rel(expected: float, actual: float, tol: float = 1e-2):
    assert abs(expected - actual) / abs(actual) < tol
