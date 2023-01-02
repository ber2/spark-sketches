package minhash

class Sha1Hash32Spec extends BaseSpec {
  behavior of "Sha1Hash32 with the word 'hello'"

  val hash = Sha1Hash32.digest("hello".getBytes)

  it should "export into a positive 32-bit integer of type Long" in {
    assertResult(499578026L)(hash.toLong)
  }
}
