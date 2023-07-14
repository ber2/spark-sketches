import numpy as np

from minhash.constants import MAX_HASH


def test_deserialization_length_is_256(minhash_gb):
    assert minhash_gb.shape == (256,)


def test_deserialization_is_non_trivial(minhash_gb):
    assert np.min(minhash_gb) < MAX_HASH
