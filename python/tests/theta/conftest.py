import pytest

from theta.deserialize import deserialize
from theta.theta import Theta


@pytest.fixture
def serialized_theta_gb() -> bytes:
    with open("tests/theta/resources/gb.bin", "rb") as fp:
        data_bin = fp.read()

    return data_bin


@pytest.fixture
def theta_gb(serialized_theta_gb) -> Theta:
    return deserialize(serialized_theta_gb)


@pytest.fixture
def serialized_theta_fr() -> bytes:
    with open("tests/theta/resources/fr.bin", "rb") as fp:
        data_bin = fp.read()

    return data_bin


@pytest.fixture
def theta_fr(serialized_theta_fr) -> Theta:
    return deserialize(serialized_theta_fr)


@pytest.fixture
def serialized_joint_theta() -> bytes:
    with open("tests/theta/resources/joint.bin", "rb") as fp:
        data_bin = fp.read()

    return data_bin


@pytest.fixture
def joint_theta(serialized_joint_theta) -> Theta:
    return deserialize(serialized_joint_theta)
