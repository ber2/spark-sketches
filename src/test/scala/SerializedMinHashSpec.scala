package com.ber2.spark.minhash

class SerializedMinHashSpec extends BaseSpec {

  val numPerm: Short = 128
  val mh = MinHash.trivialMinHash(numPerm)

  behavior of "Deserialization of a MinHash"

  it should "fail if bytes length is not divisible by 8" in {
    val bytes = Array[Byte](0, 0, 0, 0, -1, -1, -1, -1, 0)
    val smh = SerializedMinHash(bytes)

    an [IndexOutOfBoundsException] should be thrownBy (smh.deserialize)
  }

  it should "deserialize an array of ones" in {
    val oneLongInBytes = Array[Byte](0, 0, 0, 0, -1, -1, -1, -1)
    var bytes = Array.emptyByteArray
    (1 to numPerm).foreach { _ => bytes = bytes ++ oneLongInBytes }

    val smh = SerializedMinHash(bytes)

    assert(smh.deserialize.isEqual(mh))
  }
}
