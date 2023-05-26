package com.ber2.spark.sketches.theta

import org.apache.spark.sql.{Encoder, Encoders}
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.functions.{udaf, udf}

object SparkUdfs extends Serializable {

  val stringsToSketch = udaf(SketchPreaggregator)
  val aggSketches = udaf(SketchAggregator)
  val getEstimate = udf(getEstimatePreUdf(_))
  val intersection = udf(intersectionPreUdf(_, _))
  val setDifference = udf(setDifferencePreUdf(_, _))

  private val sk = new Sketcher

  private object SketchPreaggregator
      extends Aggregator[String, Theta, Array[Byte]] {
    def zero: Theta = sk.emptySketch
    def reduce(b: Theta, a: String): Theta = sk.update(b, a)
    def merge(b1: Theta, b2: Theta): Theta = sk.union(b1, b2)
    def finish(reduction: Theta): Array[Byte] = sk.serialize(reduction)
    def bufferEncoder: Encoder[Theta] = Encoders.kryo[Theta]
    def outputEncoder: Encoder[Array[Byte]] = Encoders.BINARY
  }

  private object SketchAggregator
      extends Aggregator[Array[Byte], Theta, Array[Byte]] {
    def zero: Theta = sk.emptySketch
    def reduce(b: Theta, a: Array[Byte]): Theta = sk.union(b, sk.deserialize(a))
    def merge(b1: Theta, b2: Theta): Theta = sk.union(b1, b2)
    def finish(reduction: Theta): Array[Byte] = sk.serialize(reduction)
    def bufferEncoder: Encoder[Theta] = Encoders.kryo[Theta]
    def outputEncoder: Encoder[Array[Byte]] = Encoders.BINARY
  }

  private def getEstimatePreUdf(x: Array[Byte]): Long =
    sk.getEstimate(sk.deserialize(x))

  private def intersectionPreUdf(
      x: Array[Byte],
      y: Array[Byte]
  ): Array[Byte] =
    sk.serialize(sk.intersection(sk.deserialize(x), sk.deserialize(y)))

  private def setDifferencePreUdf(
      x: Array[Byte],
      y: Array[Byte]
  ): Array[Byte] = sk.serialize(sk.aNotB(sk.deserialize(x), sk.deserialize(y)))

}
