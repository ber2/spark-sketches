import pytest

import numpy as np
import numpy.typing as npt

from minhash.deserialize import deserialize_minhash

test_num_perm = 12


@pytest.fixture
def serialized_minhash_gb() -> bytes:
    with open("tests/minhash/resources/gb.bin", "rb") as fp:
        data_bin = fp.read()
    return data_bin


@pytest.fixture
def minhash_gb(serialized_minhash_gb) -> npt.NDArray[np.uint64]:
    return deserialize_minhash(serialized_minhash_gb)


@pytest.fixture
def serialized_minhash_fr() -> bytes:
    with open("tests/minhash/resources/fr.bin", "rb") as fp:
        data_bin = fp.read()
    return data_bin


@pytest.fixture
def minhash_fr(serialized_minhash_fr) -> npt.NDArray[np.uint64]:
    return deserialize_minhash(serialized_minhash_fr)


@pytest.fixture
def serialized_joint_minhash() -> bytes:
    with open("tests/minhash/resources/joint.bin", "rb") as fp:
        data_bin = fp.read()
    return data_bin


@pytest.fixture
def minhash_joint(serialized_joint_minhash) -> npt.NDArray[np.uint64]:
    return deserialize_minhash(serialized_joint_minhash)
