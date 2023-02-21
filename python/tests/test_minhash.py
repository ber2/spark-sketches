import numpy as np

import pytest

from minhash.minhash import trivial_minhash
from minhash.constants import MAX_HASH


test_num_perm = 12


@pytest.fixture
def mh():
    return trivial_minhash(num_perm=test_num_perm)


def test_trivial_minhash_length(mh):
    assert mh.shape == (test_num_perm,)


def test_trivial_minhash_should_have_max_hash_in_each_entry(mh):
    assert np.all(mh == MAX_HASH)
