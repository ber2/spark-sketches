package minhash

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.forAll

class Sha1Hash32Prop extends Properties("Sha1Hash32") {

  val hashes: Gen[Sha1Hash32] = for {
    s <- Gen.alphaNumStr
  } yield Sha1Hash32.digest(s)

  property("should build byte arrays of length 4") = forAll(hashes) {
    (h: Sha1Hash32) => h.digest.length == 4
  }

  property("should export hex strings of length 8") = forAll(hashes) {
    (h: Sha1Hash32) => h.toHex.length == 8
  }

  property("should export a 32-bit positive Long") = forAll(hashes) {
    (h: Sha1Hash32) => (0L < h.toLong) && (h.toLong <= Constants.maxHash)
  }

}
