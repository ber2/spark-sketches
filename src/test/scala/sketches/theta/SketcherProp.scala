package com.ber2.spark.sketches.theta

import scala.math.abs

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.{forAll, forAllNoShrink}

class SketcherProp extends Properties("ThetaSketch") {

  lazy val sk = new Sketcher

  object Generators {
    val maxLength = 1000
    val lengths = Gen.choose[Int](200, maxLength)

    val stringGenerator: (Int) => Gen[List[String]] = (d: Int) =>
      Gen.containerOfN[List, String](d, Gen.stringOfN(24, Gen.hexChar))

    val twoDocumentListsWithGivenIntersection
        : Gen[(Int, List[String], List[String])] = for {
      d <- lengths
      commonDocs <- stringGenerator(d)
      leftDocs <- stringGenerator(maxLength - d)
      rightDocs <- stringGenerator(maxLength - d)
    } yield (d, commonDocs ++ leftDocs, commonDocs ++ rightDocs)

    val twoSketchesWithGivenIntersection: Gen[(Int, Theta, Theta)] = for {
      (d, leftDocs, rightDocs) <- twoDocumentListsWithGivenIntersection
    } yield (d, sk.fromStrings(leftDocs), sk.fromStrings(rightDocs))

    val sketchOfGivenLength: Gen[(Int, Theta)] = for {
      d <- lengths
      docs <- stringGenerator(d)
    } yield (d, sk.fromStrings(docs))
  }

  def relativeErrorBelow(
      threshold: Double
  )(actual: Double, expected: Double): Boolean = {
    val relErr = abs(actual - expected) / expected
    relErr < threshold
  }

  property("should not be trivial after updating") =
    forAllNoShrink(Generators.sketchOfGivenLength) {
      case (d: Int, sketch: Theta) =>
        val thetaIsBelowOne = sketch.theta < 1.0
        val thereAreHashes = sketch.hashes.size > 0
        thetaIsBelowOne && thereAreHashes
    }

  property("should estimate from a single sketch") =
    forAllNoShrink(Generators.sketchOfGivenLength) {
      case (d: Int, sketch: Theta) =>
        val actual = sk.getEstimate(sketch)
        relativeErrorBelow(0.05)(actual, d)
    }

  property("should estimate the union of two sketches") =
    forAllNoShrink(Generators.twoSketchesWithGivenIntersection) {
      case (d: Int, leftSketch: Theta, rightSketch: Theta) =>
        val expectedEstimate = 2L * Generators.maxLength - d
        val union = sk.union(leftSketch, rightSketch)
        val actualEstimate = sk.getEstimate(union)
        relativeErrorBelow(0.05)(actualEstimate, expectedEstimate)
    }

  property("should estimate the intersection of two sketches") =
    forAllNoShrink(Generators.twoSketchesWithGivenIntersection) {
      case (d: Int, left: Theta, right: Theta) =>
        val intersection = sk.intersection(left, right)
        val actualEstimate = sk.getEstimate(intersection)
        relativeErrorBelow(0.2)(actualEstimate, d)
    }

  property("should estimate the difference of two sketches") =
    forAllNoShrink(Generators.twoSketchesWithGivenIntersection) {
      case (d: Int, leftSketch: Theta, rightSketch: Theta) =>
        val expectedEstimate = Generators.maxLength - d
        val difference = sk.aNotB(leftSketch, rightSketch)
        val actualEstimate = sk.getEstimate(difference)
        relativeErrorBelow(0.2)(actualEstimate, expectedEstimate)
    }

  property("should serialize & deserialize") =
    forAllNoShrink(Generators.sketchOfGivenLength) { case (_: Int, th: Theta) =>
      val recoveredTh = sk.deserialize(sk.serialize(th))
      val thetasMatch = recoveredTh.theta == th.theta
      val arrayLengthsMatch = recoveredTh.hashes.size == th.hashes.size
      val arraysMatch =
        recoveredTh.hashes
          .zip(th.hashes)
          .forall { case (a, b) => a == b }

      thetasMatch && arrayLengthsMatch && arraysMatch
    }

}
