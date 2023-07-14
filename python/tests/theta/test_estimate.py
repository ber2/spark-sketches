from theta.estimate import get_estimate


def test_estimation_joint(joint_theta):
    expected_value = 704278
    actual_value = get_estimate(joint_theta)

    assert expected_value == actual_value


def test_estimation_gb(theta_gb):
    expected_value = 514111
    actual_value = get_estimate(theta_gb)

    assert expected_value == actual_value


def test_estimation_fr(theta_fr):
    expected_value = 190435
    actual_value = get_estimate(theta_fr)

    assert expected_value == actual_value
