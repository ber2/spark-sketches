import numpy as np

from minhash.constants import MAX_HASH


def test_deserialization_length_is_256(hash_gb):
    assert hash_gb.shape == (256,)


def test_deserialization_is_non_trivial(hash_gb):
    assert np.min(hash_gb) < MAX_HASH
