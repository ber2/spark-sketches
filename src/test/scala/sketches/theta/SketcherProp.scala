package com.ber2.spark.sketches.theta

import scala.math.abs

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.{forAll, forAllNoShrink}

class SketcherProp extends Properties("ThetaSketch") {

  lazy val sk = new Sketcher

  object Generators {
    val length = 1000

    def mkDoc(d: Int): String = s"document_$d"

    def twoDocumentListsWithGivenIntersection(
        d: Int
    ): (Seq[String], Seq[String]) = {
      val rest = length - d

      val commonDocs = (0 until d).map(mkDoc).toSeq
      val othersLeft = (d until d + rest).map(mkDoc).toSeq
      val othersRight =
        ((d + length * length) until (d + rest + length * length))
          .map(mkDoc)
          .toSeq

      (commonDocs ++ othersLeft, commonDocs ++ othersRight)
    }

    val common = Gen.choose[Int](0, length)
    val largeLength = Gen.choose[Int](length / 100, length * 10)

    def twoSketchesFromCommon(d: Int): (Theta, Theta) = {
      val (leftDocs, rightDocs) =
        Generators.twoDocumentListsWithGivenIntersection(d)
      (sk.fromStrings(leftDocs), sk.fromStrings(rightDocs))
    }

    def generateSketch(length: Int): Theta = {
      val docs = (0 to length).map(mkDoc)
      sk.fromStrings(docs)
    }

    val largeSketch: Gen[Theta] = for {
      l <- largeLength
    } yield generateSketch(l)
  }

  property("should have good estimations") =
    forAllNoShrink(Generators.largeLength) { (l: Int) =>
      val sketch = Generators.generateSketch(l)
      val relErr = abs(sk.getEstimate(sketch) - l) / l.toDouble
      relErr <= 0.05
    }

  property("should estimate the union of two lists") =
    forAll(Generators.common) { (d: Int) =>
      val (leftSketch, rightSketch) = Generators.twoSketchesFromCommon(d)

      val expectedEstimate = 2L * Generators.length - d
      val union = sk.union(leftSketch, rightSketch)
      sk.getEstimate(union) == expectedEstimate
    }

  property("should estimate the intersection of two lists") =
    forAll(Generators.common) { (d: Int) =>
      val (leftSketch, rightSketch) = Generators.twoSketchesFromCommon(d)
      val intersection = sk.intersection(leftSketch, rightSketch)
      sk.getEstimate(intersection) == d
    }

  property("should estimate the difference of two lists") =
    forAll(Generators.common) { (d: Int) =>
      val (leftSketch, rightSketch) = Generators.twoSketchesFromCommon(d)
      val expectedEstimate = Generators.length - d
      val difference = sk.aNotB(leftSketch, rightSketch)
      sk.getEstimate(difference) == expectedEstimate
    }

  property("should serialize & deserialize") =
    forAllNoShrink(Generators.largeSketch) { (th: Theta) =>
      val recoveredTh = sk.deserialize(sk.serialize(th))
      val thetaMatches = recoveredTh.theta == th.theta
      val arrayLengthMatches = recoveredTh.hashes.size == th.hashes.size
      val arraysMatch =
        recoveredTh.hashes
          .zip(th.hashes)
          .forall { case (a, b) => a == b }

      thetaMatches && arrayLengthMatches && arraysMatch
    }

}