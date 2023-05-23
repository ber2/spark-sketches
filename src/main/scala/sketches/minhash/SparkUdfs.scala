package com.ber2.spark.sketches.minhash

import scala.math.{round, min}
import org.apache.spark.sql.{Encoder, Encoders}
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.functions.{udf, udaf}

object SparkUdfs128 extends SparkUdfs {
  val numPerm: Short = 128
}

object SparkUdfs256 extends SparkUdfs {
  val numPerm: Short = 256
}

object SparkUdfs512 extends SparkUdfs {
  val numPerm: Short = 512
}

trait SparkUdfs extends Serializable {

  def numPerm: Short

  val aggStringToHash = udaf(SketchPreaggregator)
  val aggHashes = udaf(SketchAggregator)

  val countUniques = udf(countUniquesPreUdf(_))
  val jaccard = udf(jaccardPreUdf(_, _))
  val overlap = udf(overlapPreUdf(_, _))

  private object SketchPreaggregator
      extends Aggregator[String, MinHash, Array[Byte]] {
    def zero: MinHash = MinHash.trivialMinHash(numPerm)
    def reduce(b: MinHash, a: String): MinHash = b.add(a)
    def merge(b1: MinHash, b2: MinHash): MinHash = b1.merge(b2)
    def finish(reduction: MinHash): Array[Byte] = reduction.serialized.bytes
    def bufferEncoder: Encoder[MinHash] = Encoders.javaSerialization[MinHash]
    def outputEncoder: Encoder[Array[Byte]] = Encoders.BINARY
  }

  private object SketchAggregator
      extends Aggregator[Array[Byte], MinHash, Array[Byte]] {
    def zero: MinHash = MinHash.trivialMinHash(numPerm)
    def reduce(b: MinHash, a: Array[Byte]): MinHash =
      b.merge(SerializedMinHash(a).deserialize)
    def merge(b1: MinHash, b2: MinHash): MinHash = b1.merge(b2)
    def finish(reduction: MinHash): Array[Byte] = reduction.serialized.bytes
    def bufferEncoder: Encoder[MinHash] = Encoders.javaSerialization[MinHash]
    def outputEncoder: Encoder[Array[Byte]] = Encoders.BINARY
  }

  private def countUniquesPreUdf(sh: Array[Byte]): Long = {
    val mh: MinHash = SerializedMinHash(sh).deserialize
    round(mh.countUniques)
  }

  private def jaccardPreUdf(
      left: Array[Byte],
      right: Array[Byte]
  ): Double = {
    val leftHash = SerializedMinHash(left).deserialize
    val rightHash = SerializedMinHash(right).deserialize
    leftHash.jaccard(rightHash)
  }

  private def overlapPreUdf(
      left: Array[Byte],
      right: Array[Byte]
  ): Long = {
    val leftHash = SerializedMinHash(left).deserialize
    val rightHash = SerializedMinHash(right).deserialize

    val a = leftHash.countUniques
    val b = rightHash.countUniques
    val j = leftHash.jaccard(rightHash)

    val o = (a + b) * j / (j + 1)
    Array(round(a), round(b), round(o)).min
  }

}
