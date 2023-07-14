

def test_deserialization_is_non_trivial(theta_gb):
    assert theta_gb.theta < 1.0
    assert len(theta_gb.hashes) > 0
