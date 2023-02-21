import pytest

import numpy as np
import numpy.typing as npt

from minhash.deserialize import deserialize_minhash

test_num_perm = 12


@pytest.fixture
def serialized_hash_gb() -> bytes:
    with open("tests/resources/gb.bin", "rb") as fp:
        data_bin = fp.read()
    return data_bin


@pytest.fixture
def hash_gb(serialized_hash_gb) -> npt.NDArray[np.uint64]:
    return deserialize_minhash(serialized_hash_gb)


@pytest.fixture
def serialized_hash_fr() -> bytes:
    with open("tests/resources/fr.bin", "rb") as fp:
        data_bin = fp.read()
    return data_bin


@pytest.fixture
def hash_fr(serialized_hash_fr) -> npt.NDArray[np.uint64]:
    return deserialize_minhash(serialized_hash_fr)


@pytest.fixture
def serialized_joint_hash() -> bytes:
    with open("tests/resources/joint.bin", "rb") as fp:
        data_bin = fp.read()
    return data_bin


@pytest.fixture
def hash_joint(serialized_joint_hash) -> npt.NDArray[np.uint64]:
    return deserialize_minhash(serialized_joint_hash)
