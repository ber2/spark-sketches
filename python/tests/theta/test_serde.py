

def test_deserialization_is_non_trivial(joint_theta):
    assert joint_theta.theta < 1.0
    assert len(joint_theta.hashes) > 0
