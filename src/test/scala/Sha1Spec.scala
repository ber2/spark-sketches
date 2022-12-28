package minhash

class Sha1Hash32Spec extends BaseSpec {
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
}
