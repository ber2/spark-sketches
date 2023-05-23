package com.ber2.spark.sketches.minhash

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.{forAll, propBoolean}

class RandomLinearPermutationsProp
    extends Properties("RandomLinearPermutations") {

  val permValuePairs: Gen[(Short, Long)] = for {
    d <- Gen.choose[Short](1, 2048)
    v <- Gen.long
  } yield (d, v)

  property("should have 32-bit Long array values") = forAll(permValuePairs) {
    case (p: Short, v: Long) => {
      val permutations = new RandomLinearPermutations(p)
      permutations(v).forall { d =>
        d >= 0L && d <= Constants.maxHash
      }
    }
  }

  property("should produce the given number of permutations") =
    forAll(permValuePairs) {
      case (d: Short, v: Long) => {
        val perm = new RandomLinearPermutations(d)
        perm(v).length == d
      }
    }

}
