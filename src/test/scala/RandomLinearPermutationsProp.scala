package minhash

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.forAll

class RandomLinearPermutationsProp
    extends Properties("RandomLinearPermutations") {

  property("should have 32-bit Long array values") = forAll { (v: Long) =>
    RandomLinearPermutations(v).forall { d =>
      d >= 0L && d <= Constants.maxHash
    }
  }

}
