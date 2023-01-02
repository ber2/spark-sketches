package minhash

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.forAll

class Sha1Hash32Prop extends Properties("Sha1Hash32") {

  val hashes: Gen[Long] = for {
    s <- Gen.alphaNumStr
  } yield Sha1Hash32.digest(s.getBytes)

  property("should export a 32-bit positive Long") = forAll(hashes) {
    (h: Long) => (0L < h) && (h <= Constants.maxHash)
  }

}
