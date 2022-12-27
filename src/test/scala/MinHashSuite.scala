package minhash

import scala.math.{floor, round}
import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.forAll

class MinHashSpec extends BaseSpec with MinHash {

  behavior of "Trivial minhash"

  it should "have length given by number of permutations" in {
    trivialMinHash.length should equal(Constants.numPerm)
  }

  it should "have the maximum hash in each value" in {
    all(trivialMinHash) should ===(Constants.maxHash)
  }

  it should "get smaller minimum if updated" in {
    val updated = trivialMinHash.update(Array[Byte](1))
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

}

class MinHashProp extends Properties("MinHash") with MinHash {

  object Generators {
    val documents: Gen[List[String]] = Gen.listOf(Gen.alphaNumStr)

    val minHashes: Gen[MinHash] = for {
      ds <- documents
    } yield updateRecursively(trivialMinHash, ds)

    val minHashStringPairs = for {
      mh <- minHashes
      s <- Gen.alphaNumStr
    } yield (mh, s)

    val twoDocumentLists = for {
      l1 <- documents
      l2 <- documents
    } yield (l1, l2)

    val mkDoc: Int => String = d => s"document_$d"

    def twoDocumentListsWithGivenJaccard(
        j: Double
    ): (List[String], List[String]) = {
      val len = 10000
      val common = floor((len + len) * j / (1 + j)).toInt
      val rest = len - common

      val commonDocs = (0 until common).map(mkDoc).toList
      val othersLeft = (common until common + rest).map(mkDoc).toList
      val othersRight =
        ((common + len * len) until (common + rest + len * len))
          .map(mkDoc)
          .toList

      (commonDocs ++ othersLeft, commonDocs ++ othersRight)
    }

    val jaccards = Gen.choose[Double](0.0, 1.0)

  }

  property("should have expected array length") = forAll(Generators.minHashes) {
    (x: MinHash) => x.length == Constants.numPerm
  }

  property("should have admissible values") = forAll(Generators.minHashes) {
    (x: MinHash) => x.forall { d => (d >= 0L) && (d <= Constants.maxHash) }
  }

  property("should have decreasing minimums when updated") =
    forAll(Generators.minHashStringPairs) { case (x: MinHash, s: String) =>
      x.update(s).zip(x).forall { case (n: Long, m: Long) => n <= m }
    }

  property("should recover union of two minhashes from merge") = forAll(
    Generators.twoDocumentLists
  ) { case (l1: List[String], l2: List[String]) =>
    val l3 = l1 ++ l2
    val mh1 = updateRecursively(trivialMinHash, l1)
    val mh2 = updateRecursively(trivialMinHash, l2)
    val expected = updateRecursively(trivialMinHash, l3)
    val actual = mh1.merge(mh2)

    actual.deep == expected.deep
  }

  property("should compute jaccard of two lists") =
    forAll(Generators.jaccards) { (j: Double) =>
      val (d1, d2) = Generators.twoDocumentListsWithGivenJaccard(j)
      val mh1 = updateRecursively(trivialMinHash, d1)
      val mh2 = updateRecursively(trivialMinHash, d2)

      val actualJ = mh1.jaccard(mh2)

      actualJ == j
    }

  property("should approximately count uniques") =
    forAll(Generators.documents) { (ls: List[String]) =>
      val expectedUniques = ls.toSet.size
      val mh = updateRecursively(trivialMinHash, ls)

      round(mh.countUniques) == expectedUniques
    }

  property("should serialize & deserialize") =
    forAll(Generators.minHashes) {
      (mh: MinHash) => mh.toBytes.toLong.deep == mh.deep
    }

}
