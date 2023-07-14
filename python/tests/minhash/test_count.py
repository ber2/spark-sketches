from minhash.count import count_uniques


def test_count_uniques(minhash_joint):
    expected_value = 1264403
    actual_value = count_uniques(minhash_joint)

    assert abs(expected_value - actual_value) < 1.0
