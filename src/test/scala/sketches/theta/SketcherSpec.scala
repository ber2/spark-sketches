package com.ber2.spark.sketches.theta

import com.ber2.spark.sketches.common.BaseSpec

class SketcherSpec extends BaseSpec {
  lazy val sk = new Sketcher
  lazy val emptyTs = sk.emptySketch

  "The trivial ThetaSketch" should "have 0 as estimate" in {
    sk.getEstimate(emptyTs) should ===(0L)
  }

  it should "serialize & deserialize" in {
    val actualSketch = sk.deserialize(sk.serialize(emptyTs))

    actualSketch.theta should ===(emptyTs.theta)
    actualSketch.hashes should ===(emptyTs.hashes)
  }

  it should "have a non-zero estimate when updated with distinct values" in {
    val ts = sk.fromStrings(Seq("value1", "value2"))
    sk.getEstimate(ts) should be > 0.0
  }

  it should "have a higher estimate when updated with more distinct values" in {
    val ts1 = sk.fromStrings(Seq("value1", "value2"))
    val ts2 = sk.fromStrings(Seq("value1", "value2", "value3"))
    sk.getEstimate(ts2) should be > sk.getEstimate(ts1)
  }

  it should "produce estimate 0 when united with itself" in {
    val result = sk.union(emptyTs, emptyTs)
    sk.getEstimate(result) should ===(0.0)
  }

  it should "produce estimate 0 when intersected with itself" in {
    val result = sk.intersection(emptyTs, emptyTs)
    sk.getEstimate(result) should ===(0.0)
  }

  it should "produce estimate 0 when diff'd with itself" in {
    val result = sk.aNotB(emptyTs, emptyTs)
    sk.getEstimate(result) should ===(0.0)
  }

}
