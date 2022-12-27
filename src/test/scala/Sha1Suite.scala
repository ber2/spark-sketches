package minhash

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.forAll

class Sha1Hash32Spec extends PropBaseSpec {

  val hashes: Gen[Sha1Hash32] = for {
    s <- Gen.alphaNumStr
  } yield Sha1Hash32.digest(s)

  behavior of "Sha1Hash32 with the word 'hello'"

  val hash = Sha1Hash32.digest("hello")

  it should "digest into a byte array" in {
    assertResult(Array(-86, -12, -58, 29))(hash.digest)
  }

  it should "export to hex string" in {
    assertResult("aaf4c61d")(hash.toHex)
  }

  it should "export into a positive 32-bit integer of type Long" in {
    assertResult(2868168221L)(hash.toLong)
  }

  behavior of "Sha1Hash32 with general strings"

  it should "build byte arrays of length 4" in forAll(hashes) {
    (h: Sha1Hash32) => h.digest.length == 4
  }

  it should "export hex strings of length 8" in forAll(hashes) {
    (h: Sha1Hash32) => h.toHex.length == 8
  }

  it should "export a 32-bit positive Long" in forAll(hashes) {
    (h: Sha1Hash32) => (0L < h.toLong) && (h.toLong <= Constants.maxHash)
  }

}
