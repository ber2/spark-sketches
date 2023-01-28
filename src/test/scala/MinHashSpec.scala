package com.ber2.spark.minhash

import scala.math.abs

class MinHashSpec extends BaseSpec {

  behavior of "Trivial minhash"

  val numPerm: Short = 256
  val mh = MinHash.trivialMinHash(numPerm)
  val hv = mh.hashValues

  it should "have length given by number of permutations" in {
    hv.length should equal(numPerm)
  }

  it should "have the maximum hash in each value" in {
    all(hv) should ===(Constants.maxHash)
  }

  it should "get smaller minimum if updated" in {
    val updated = mh.add(Array[Byte](1))
    updated.hashValues.min should be < Constants.maxHash
  }

  it should "have zero count" in {
    mh.countUniques should ===(0.0)
  }

  it should "have Jaccard one with itself" in {
    mh.jaccard(mh) should ===(1.0)
  }

  it should "trivially merge with itself" in {
    assert(mh.merge(mh).isEqual(mh))
  }

  it should "serialize to length" in {
    mh.serialized.bytes.length should equal(
      numPerm * Constants.hashValueByteSize
    )
  }

  it should "serialize to expected values" in {
    val oneLongInBytes = Array[Byte](0, 0, 0, 0, -1, -1, -1, -1)
    var expectedValue = Array.emptyByteArray
    (1 to numPerm).foreach(_ => expectedValue = expectedValue ++ oneLongInBytes)

    mh.serialized.bytes should ===(expectedValue)
  }

  it should "serialize and deserialize" in {
    assert(SerializedMinHash(mh.serialized.bytes).deserialize.isEqual(mh))
  }

  behavior of "Two trivial minhashes with differing permutation numbers"

  val mh1 = MinHash.trivialMinHash(128)
  val mh2 = MinHash.trivialMinHash(256)

  it should "fail when trying to merge" in {
    an[IllegalArgumentException] should be thrownBy (mh1.merge(mh2))
  }

  it should "fail when computing Jaccard index" in {
    an[IllegalArgumentException] should be thrownBy (mh1.jaccard(mh2))
  }

  behavior of "The minhash of a large collection"

  it should "not overflow counting uniques" in {
    val collectionSize = 90000
    val documents = (0 until collectionSize).map(d => s"document_$d").toList
    val largeMh = MinHash.addBatch(mh, documents)

    val estimationError = abs(
      largeMh.countUniques - collectionSize.toDouble
    ) / collectionSize.toDouble

    estimationError should be < 0.08
  }

}
