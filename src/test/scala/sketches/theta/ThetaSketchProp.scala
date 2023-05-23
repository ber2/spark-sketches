package com.ber2.spark.sketches.theta

import scala.math.abs

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.{forAll, forAllNoShrink}

class ThetaSketchProp extends Properties("ThetaSketch") {

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

    def twoSketchesFromCommon(d: Int): (ThetaSketch, ThetaSketch) = {
      val (leftDocs, rightDocs) =
        Generators.twoDocumentListsWithGivenIntersection(d)
      (ThetaSketch.fromStrings(leftDocs), ThetaSketch.fromStrings(rightDocs))
    }

    def generateSketch(length: Int): ThetaSketch = {
      val docs = (0 to length).map(mkDoc)
      ThetaSketch.fromStrings(docs)
    }

    val largeSketch: Gen[ThetaSketch] = for {
      l <- largeLength
    } yield generateSketch(l)
  }

  property("should have good estimations") =
    forAllNoShrink(Generators.largeLength) { (l: Int) =>
      val sketch = Generators.generateSketch(l)
      val relErr = abs(sketch.getEstimate - l) / l.toDouble
      relErr <= 0.05
    }

  property("should estimate the union of two lists") =
    forAll(Generators.common) { (d: Int) =>
      val (leftSketch, rightSketch) = Generators.twoSketchesFromCommon(d)

      val expectedEstimate = 2L * Generators.length - d
      val union = leftSketch.union(rightSketch)
      union.getEstimate == expectedEstimate
    }

  property("should estimate the intersection of two lists") =
    forAll(Generators.common) { (d: Int) =>
      val (leftSketch, rightSketch) = Generators.twoSketchesFromCommon(d)
      val intersection = leftSketch.intersection(rightSketch)
      intersection.getEstimate == d
    }

  property("should estimate the difference of two lists") =
    forAll(Generators.common) { (d: Int) =>
      val (leftSketch, rightSketch) = Generators.twoSketchesFromCommon(d)
      val expectedEstimate = Generators.length - d
      val difference = leftSketch.aNotB(rightSketch)
      difference.getEstimate == expectedEstimate
    }

  property("should serialize & deserialize") =
    forAllNoShrink(Generators.largeSketch) { (th: ThetaSketch) =>
      val recoveredTh = th.serialized.deserialized
      val thetaMatches = recoveredTh.theta == th.theta 
      val arrayLengthMatches = recoveredTh.hashValues.size == th.hashValues.size
      val arraysMatch = 
        recoveredTh.hashValues
          .zip(th.hashValues)
          .map { case (a, b) => a == b }
          .foldLeft(true)(_ && _)

      thetaMatches && arrayLengthMatches && arraysMatch
    }

}
