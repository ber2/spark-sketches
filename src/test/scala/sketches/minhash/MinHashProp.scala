package com.ber2.spark.sketches.minhash

import scala.math.{floor, round, abs}
import scala.util.{Try, Success, Failure}
import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.{forAll, forAllNoShrink}

class MinHashProp extends Properties("MinHash") {

  val numPerm: Short = 256
  val trivialMinHash = MinHash.trivialMinHash(numPerm)

  object Generators {
    val documents: Gen[List[String]] = Gen.listOf(Gen.alphaNumStr)

    val longStrings = for {
      cs <- Gen.listOfN(24, Gen.alphaNumChar)
    } yield cs.mkString

    val longDocuments = Gen.listOfN(50, longStrings)

    val minHashes: Gen[MinHash] = for {
      ds <- documents
    } yield MinHash.addBatch(trivialMinHash, ds)

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

    val minHashesWithVaryingNumPerms = for {
      n <- Gen.choose[Short](1, 1024)
      ls <- longDocuments
    } yield MinHash.addBatch(MinHash.trivialMinHash(n), ls)

    val twoMinHashesDifferentPerms = for {
      n <- Gen.choose[Short](1, 1024)
      m <- Gen.choose[Short](1, 1024)
      (l1, l2) <- twoDocumentLists
      mh1 = MinHash.addBatch(MinHash.trivialMinHash(n), l1)
      mh2 = MinHash.addBatch(MinHash.trivialMinHash(m), l2)
    } yield (mh1, mh2)

  }

  property("should have expected array length") = forAll(Generators.minHashes) {
    (x: MinHash) => x.hashValues.length == x.numPerm
  }

  property("should have admissible values") = forAll(Generators.minHashes) {
    (x: MinHash) =>
      x.hashValues.forall { d => (d >= 0L) && (d <= Constants.maxHash) }
  }

  property("should have decreasing minimums when updated") =
    forAll(Generators.minHashStringPairs) { case (x: MinHash, s: String) =>
      val y = x.add(s)
      y.hashValues.zip(x.hashValues).forall { case (n: Long, m: Long) =>
        n <= m
      }
    }

  property("should recover union of two minhashes from merge") = forAll(
    Generators.twoDocumentLists
  ) { case (l1: List[String], l2: List[String]) =>
    val l3 = l1 ++ l2
    val mh1 = MinHash.addBatch(trivialMinHash, l1)
    val mh2 = MinHash.addBatch(trivialMinHash, l2)
    val expected = MinHash.addBatch(trivialMinHash, l3)
    val actual = mh1.merge(mh2)

    actual.isEqual(expected)
  }

  property("should compute jaccard of two lists") =
    forAllNoShrink(Generators.jaccards) { (j: Double) =>
      val (d1, d2) = Generators.twoDocumentListsWithGivenJaccard(j)
      val mh1 = MinHash.addBatch(trivialMinHash, d1)
      val mh2 = MinHash.addBatch(trivialMinHash, d2)

      val actualJ = mh1.jaccard(mh2)
      val error = abs(actualJ - j)

      if (error >= 0.1) { println(error) }
      error < 0.1
    }

  property("should approximately count uniques") =
    forAllNoShrink(Generators.longDocuments) { (ls: List[String]) =>
      val expectedUniques = ls.toSet.size.toDouble
      val mh = MinHash.addBatch(trivialMinHash, ls)
      val actualUniques = mh.countUniques
      val relativeError = abs(actualUniques - expectedUniques) / expectedUniques

      relativeError < 0.23
    }

  property("should serialize & deserialize") =
    forAll(Generators.minHashesWithVaryingNumPerms) { (mh: MinHash) =>
      SerializedMinHash(mh.serialized.bytes).deserialize.isEqual(mh)
    }

  property("should fail to merge on distinct permutation numbers") =
    forAll(Generators.twoMinHashesDifferentPerms) {
      case (mh1: MinHash, mh2: MinHash) => {
        val result = Try(mh1.merge(mh2))
        result match {
          case Success(mh) =>
            mh1.numPerm == mh2.numPerm && mh.numPerm == mh1.numPerm
          case Failure(_) => true
        }
      }
    }

  property(
    "should fail to compute jaccard index on distinct permutation numbers"
  ) = forAll(Generators.twoMinHashesDifferentPerms) {
    case (mh1: MinHash, mh2: MinHash) => {
      val result = Try(mh1.jaccard(mh2))
      result match {
        case Success(j) => mh1.numPerm == mh2.numPerm && j >= 0.0 && j <= 1.0
        case Failure(_) => true
      }
    }
  }

}
