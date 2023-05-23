package com.ber2.spark.sketches.minhash

import scala.math.abs
import com.ber2.spark.sketches.common.BaseSpec

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
      numPerm * Constants.bytesInALong
    )
  }

  it should "serialize to expected values" in {
    val oneLongInBytes = Array[Byte](0, 0, 0, 0, -1, -1, -1, -1)
    var expectedValue = Array.emptyByteArray
    (1 to numPerm).foreach(_ => expectedValue = expectedValue ++ oneLongInBytes)

    mh.serialized.bytes should ===(expectedValue)
  }

  it should "serialize and deserialize" in {
    val bytes = mh.serialized.bytes
    val smh = SerializedMinHash(bytes)
    val dsmh = smh.deserialize
    assert(dsmh.isEqual(mh))
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

  def mkDocs(start: Int, end: Int): List[String] =
    (start until end)
      .map(d => s"document_$d")
      .toList

  behavior of "The minhash of a large collection"

  it should "not overflow counting uniques" in {
    val collectionSize = 90000
    val documents = mkDocs(0, collectionSize)
    val largeMh = MinHash.addBatch(mh, documents)

    val estimationError = abs(
      largeMh.countUniques - collectionSize.toDouble
    ) / collectionSize.toDouble

    estimationError should be < 0.08
  }

  behavior of "Increasing the number of permutations"

  it should "improve accuracy of uniques count" in {
    val initial = mkDocs(0, 10000)
    val repeats = mkDocs(0, 200)
    val allDocs = initial ++ repeats

    val expectedUniques = 10000

    val uniqueCountsAbsoluteErrors = List(256, 128, 64)
      .map { p =>
        MinHash
          .addBatch(MinHash.trivialMinHash(p.toShort), allDocs)
          .countUniques
      }
      .map { (estimation: Double) =>
        abs(expectedUniques - estimation)
      }

    uniqueCountsAbsoluteErrors should equal(uniqueCountsAbsoluteErrors.sorted)
  }

  it should "improve accuracy of Jaccard index" in {
    val common = mkDocs(0, 2000)
    val left = mkDocs(2000, 4000)
    val right = mkDocs(4000, 6000)

    val docsLeft = common ++ left
    val docsRight = common ++ right

    val expectedJaccard = 1 / (3.toDouble)

    val jaccards = List(512, 256, 128)
      .map { p =>
        val mhLeft =
          MinHash.addBatch(MinHash.trivialMinHash(p.toShort), docsLeft)
        val mhRight =
          MinHash.addBatch(MinHash.trivialMinHash(p.toShort), docsRight)
        mhLeft.jaccard(mhRight)
      }

    val jaccardErrors = jaccards
      .map { (estimation: Double) =>
        abs(expectedJaccard - estimation)
      }

    jaccardErrors should equal(jaccardErrors.sorted)
  }

}
