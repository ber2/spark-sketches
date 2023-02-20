from minhash.jaccard import jaccard


def test_jaccard(hash_gb, hash_fr):
    expected_jaccard = 0.0
    actual_jaccard = jaccard(hash_gb, hash_fr)

    assert actual_jaccard == expected_jaccard
