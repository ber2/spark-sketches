package com.ber2.spark.minhash

import scala.math.{round, min}
import org.apache.spark.sql.{Encoder, Encoders, SparkSession}
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.functions.{udf, udaf}

object SparkUdfs {

  private object MinHashPreaggregator
      extends Aggregator[String, MinHash, Array[Byte]] {
    def zero: MinHash = MinHash.trivialMinHash(Constants.numPerm)
    def reduce(b: MinHash, a: String): MinHash = b.add(a)
    def merge(b1: MinHash, b2: MinHash): MinHash = b1.merge(b2)
    def finish(reduction: MinHash): Array[Byte] = reduction.serialized.bytes
    def bufferEncoder: Encoder[MinHash] = Encoders.javaSerialization[MinHash]
    def outputEncoder: Encoder[Array[Byte]] = Encoders.BINARY
  }

  private object MinHashAggregator
      extends Aggregator[SerializedMinHash, MinHash, Array[Byte]] {
    def zero: MinHash = MinHash.trivialMinHash(Constants.numPerm)
    def reduce(b: MinHash, a: SerializedMinHash): MinHash = b.merge(a.deserialize)
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

  val aggStringToHash = udaf(MinHashPreaggregator)
  val aggHashes = udaf(MinHashAggregator)

  val countUniques = udf(countUniquesPreUdf(_))
  val jaccard = udf(jaccardPreUdf(_, _))
  val overlap = udf(overlapPreUdf(_, _))
}
