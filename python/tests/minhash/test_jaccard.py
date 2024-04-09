from minhash.jaccard import jaccard


def test_jaccard(minhash_gb, minhash_fr):
    expected_jaccard = 0.0
    actual_jaccard = jaccard(minhash_gb, minhash_fr)

    assert actual_jaccard == expected_jaccard
