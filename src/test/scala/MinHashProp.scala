package com.ber2.spark.minhash

import scala.math.{floor, round, abs}
import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.{forAll, forAllNoShrink}

class MinHashProp extends Properties("MinHash") with MinHash {

  object Generators {
    val documents: Gen[List[String]] = Gen.listOf(Gen.alphaNumStr)

    val longStrings = for {
      cs <- Gen.listOfN(24, Gen.alphaNumChar)
    } yield cs.mkString

    val longDocuments = Gen.listOfN(50, longStrings)

    val minHashes: Gen[MinHash] = for {
      ds <- documents
    } yield addBatch(trivialMinHash, ds)

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
      val len = 1000
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

    val jaccards = Gen.choose[Double](0.1, 1.0)

  }

  property("should have expected array length") = forAll(Generators.minHashes) {
    (x: MinHash) => x.length == Constants.numPerm
  }

  property("should have admissible values") = forAll(Generators.minHashes) {
    (x: MinHash) => x.forall { d => (d >= 0L) && (d <= Constants.maxHash) }
  }

  property("should have decreasing minimums when updated") =
    forAll(Generators.minHashStringPairs) { case (x: MinHash, s: String) =>
      x.add(s).zip(x).forall { case (n: Long, m: Long) => n <= m }
    }

  property("should recover union of two minhashes from merge") = forAll(
    Generators.twoDocumentLists
  ) { case (l1: List[String], l2: List[String]) =>
    val l3 = l1 ++ l2
    val mh1 = addBatch(trivialMinHash, l1)
    val mh2 = addBatch(trivialMinHash, l2)
    val expected = addBatch(trivialMinHash, l3)
    val actual = mh1.merge(mh2)

    actual.deep == expected.deep
  }

  property("should compute jaccard of two lists") =
    forAllNoShrink(Generators.jaccards) { (j: Double) =>
      val (d1, d2) = Generators.twoDocumentListsWithGivenJaccard(j)
      val mh1 = addBatch(trivialMinHash, d1)
      val mh2 = addBatch(trivialMinHash, d2)

      val actualJ = mh1.jaccard(mh2)
      val error = abs(actualJ - j)

      if (error >= 0.1) { println(error) }
      error < 0.1
    }

  property("should approximately count uniques") =
    forAllNoShrink(Generators.longDocuments) { (ls: List[String]) =>
      val expectedUniques = ls.toSet.size.toDouble
      val mh = addBatch(trivialMinHash, ls)
      val actualUniques = mh.countUniques
      val relativeError = abs(actualUniques - expectedUniques) / expectedUniques

      relativeError < 0.23
    }

  property("should serialize & deserialize") = forAll(Generators.minHashes) {
    (mh: MinHash) => mh.toBytes.toLong.deep == mh.deep
  }

}
