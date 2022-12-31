package minhash

trait LinearPermutationsBehavior { this: BaseSpec =>
  def permutationArray(newArray: => Array[Long], minValue: Long): Unit = {
    it should "have expected length" in {
      newArray.length should equal(Constants.numPerm)
    }

    it should "have values below Mersenne prime" in {
      all(newArray) should be <= Constants.mersennePrime
    }

    it should s"have values above or equal to $minValue" in {
      all(newArray) should be >= 0L
    }
  }
}

class RandomLinearPermutationsSpec
    extends BaseSpec
    with LinearPermutationsBehavior {

  val weights = RandomLinearPermutations.w
  val biases = RandomLinearPermutations.b

  "Weights" should behave like permutationArray(weights, 1)

  "Biases" should behave like permutationArray(biases, 0)

}
