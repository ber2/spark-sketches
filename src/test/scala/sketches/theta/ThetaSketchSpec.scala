package com.ber2.spark.sketches.theta

import com.ber2.spark.sketches.common.BaseSpec

class ThetaSketchSpec extends BaseSpec {
  lazy val trivialSketch = ThetaSketch()

  "The trivial ThetaSketch" should "have 0 as estimate" in {
    trivialSketch.getEstimate should ===(0L)
  }

  it should "serialize & deserialize" in {
    val actualSketch = trivialSketch.serialized.deserialized

    actualSketch.theta should ===(trivialSketch.theta)
    actualSketch.hashValues should ===(trivialSketch.hashValues)
  }

  it should "have a non-zero estimate when updated with distinct values" in {
    val thetaSketch = ThetaSketch.fromStrings(Seq("value1", "value2"))
    thetaSketch.getEstimate should be > 0L
  }

  it should "have a higher estimate when updated with more distinct values" in {
    val thetaSketch1 = ThetaSketch.fromStrings(Seq("value1", "value2"))
    val thetaSketch2 =
      ThetaSketch.fromStrings(Seq("value1", "value2", "value3"))
    thetaSketch2.getEstimate should be > thetaSketch1.getEstimate
  }

  it should "produce estimate 0 when united with itself" in {
    val result = trivialSketch.union(trivialSketch)
    result.getEstimate should ===(0L)
  }

  it should "produce estimate 0 when intersected with itself" in {
    val result = trivialSketch.intersection(trivialSketch)
    result.getEstimate should ===(0L)
  }

  it should "produce estimate 0 when diff'd with itself" in {
    val result = trivialSketch.aNotB(trivialSketch)
    result.getEstimate should ===(0L)
  }

}
