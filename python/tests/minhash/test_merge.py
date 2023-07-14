import numpy as np
import numpy.typing as npt

import pytest

from minhash.merge import merge_hashes_array, pack_hashes_into_single_array


@pytest.fixture
def stacked_array(minhash_gb, minhash_fr) -> npt.NDArray[np.uint64]:
    return pack_hashes_into_single_array([minhash_gb, minhash_fr])


@pytest.fixture
def merged_minhash(stacked_array) -> npt.NDArray[np.int64]:
    return merge_hashes_array(stacked_array)


def test_pack_minhashes_result_array_shape(stacked_array):
    assert stacked_array.shape == (2, 256)


def test_merge_hashes_array_shape(merged_minhash):
    assert merged_minhash.shape == (256,)


def test_merge_hashes_array_content(merged_minhash, minhash_joint):
    assert np.all(merged_minhash == minhash_joint)
