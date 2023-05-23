package com.ber2.spark.sketches.minhash

import com.ber2.spark.sketches.common.BaseSpec

trait LinearPermutationsBehavior { this: BaseSpec =>
  def permutationArray(newArray: => Array[Long], minValue: Long, numPerm: Int): Unit = {
    it should "have expected length" in {
      newArray.length should equal(numPerm)
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

  val numPerm: Short = 256

  val permutations = new RandomLinearPermutations(numPerm)
  val weights = permutations.w
  val biases = permutations.b

  "Weights" should behave like permutationArray(weights, 1L, numPerm)

  "Biases" should behave like permutationArray(biases, 0L, numPerm)

}
