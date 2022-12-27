package minhash

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.forAll

trait LinearPermutationsBehavior { this: BaseSpec =>
  def permutationArray(newArray: => Array[Long], minValue: Long) {
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
    extends PropBaseSpec
    with LinearPermutationsBehavior {

  val weights = RandomLinearPermutations.w
  val biases = RandomLinearPermutations.b

  "Weights" should behave like permutationArray(weights, 1)

  "Biases" should behave like permutationArray(biases, 0)

  "Random linear permutations" should "have only positive array values" in forAll {
    (v: Long) => RandomLinearPermutations(v).foreach { _ should be >= 0L }
  }

  it should "have 32-bit Long array values" in forAll { (v: Long) =>
    RandomLinearPermutations(v).foreach { _ should be <= Constants.maxHash }
  }

}
