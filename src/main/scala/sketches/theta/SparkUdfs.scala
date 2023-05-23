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

  private object SketchPreaggregator
      extends Aggregator[String, ThetaSketch, Array[Byte]] {
    def zero: ThetaSketch = ThetaSketch()
    def reduce(x: ThetaSketch, s: String): ThetaSketch = x.update(s)
    def merge(x: ThetaSketch, y: ThetaSketch): ThetaSketch = x.union(y)
    def finish(x: ThetaSketch): Array[Byte] = x.serialized.bytes
    def bufferEncoder: Encoder[ThetaSketch] =
      Encoders.javaSerialization[ThetaSketch]
    def outputEncoder: Encoder[Array[Byte]] = Encoders.BINARY
  }

  private object SketchAggregator
      extends Aggregator[Array[Byte], ThetaSketch, Array[Byte]] {
    def zero: ThetaSketch = ThetaSketch()
    def reduce(x: ThetaSketch, s: Array[Byte]): ThetaSketch =
      x.union(SerializedThetaSketch(s).deserialized)
    def merge(x: ThetaSketch, y: ThetaSketch): ThetaSketch = x.union(y)
    def finish(x: ThetaSketch): Array[Byte] = x.serialized.bytes
    def bufferEncoder: Encoder[ThetaSketch] =
      Encoders.javaSerialization[ThetaSketch]
    def outputEncoder: Encoder[Array[Byte]] = Encoders.BINARY
  }

  private def getEstimatePreUdf(x: Array[Byte]): Long = SerializedThetaSketch(
    x
  ).deserialized.getEstimate
  private def intersectionPreUdf(
      x: Array[Byte],
      y: Array[Byte]
  ): Array[Byte] = {
    val xd = SerializedThetaSketch(x).deserialized
    val yd = SerializedThetaSketch(y).deserialized
    xd.intersection(yd).serialized.bytes
  }
  private def setDifferencePreUdf(
      x: Array[Byte],
      y: Array[Byte]
  ): Array[Byte] = {
    val xd = SerializedThetaSketch(x).deserialized
    val yd = SerializedThetaSketch(y).deserialized
    xd.aNotB(yd).serialized.bytes
  }

}
