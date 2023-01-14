package com.ber2.spark.minhash

import scala.math.abs

class MinHashSpec extends BaseSpec with MinHash {

  behavior of "Trivial minhash"

  it should "have length given by number of permutations" in {
    trivialMinHash.length should equal(Constants.numPerm)
  }

  it should "have the maximum hash in each value" in {
    all(trivialMinHash) should ===(Constants.maxHash)
  }

  it should "get smaller minimum if updated" in {
    val updated = trivialMinHash.add(Array[Byte](1))
    updated.min should be < Constants.maxHash
  }

  it should "have zero count" in {
    trivialMinHash.countUniques should ===(0.0)
  }

  it should "have Jaccard one with itself" in {
    trivialMinHash.jaccard(trivialMinHash) should ===(1.0)
  }

  it should "trivially merge with itself" in {
    trivialMinHash.merge(trivialMinHash) should ===(trivialMinHash)
  }

  it should "serialize to length" in {
    trivialMinHash.toBytes.length should equal(
      Constants.numPerm * Constants.hashValueByteSize
    )
  }

  it should "serialize to expected values" in {
    val oneLongInBytes = Array[Byte](0, 0, 0, 0, -1, -1, -1, -1)
    var expectedValue = Array.emptyByteArray
    (1 to Constants.numPerm).foreach(_ =>
      expectedValue = expectedValue ++ oneLongInBytes
    )

    trivialMinHash.toBytes should ===(expectedValue)
  }

  it should "serialize and deserialize" in {
    trivialMinHash.toBytes.toLong should ===(trivialMinHash)
  }

  behavior of "The minhash of a large collection"

  it should "not overflow counting uniques" in {
    val collectionSize = 90000
    val documents = (0 until collectionSize).map(d => s"document_$d").toList
    val mh = addBatch(trivialMinHash, documents)

    val estimationError = abs(mh.countUniques - collectionSize.toDouble) / collectionSize.toDouble

    estimationError should be < 0.08
  }

}
