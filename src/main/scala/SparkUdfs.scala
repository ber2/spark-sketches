package com.ber2.spark.minhash

import scala.math.{round, min}
import org.apache.spark.sql.{Encoder, Encoders, SparkSession}
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.functions.{udf, udaf}

object SparkUdfs extends MinHash {

  private object MinHashPreaggregator
      extends Aggregator[String, MinHash, SerializedMinHash] {
    def zero: MinHash = trivialMinHash
    def reduce(b: MinHash, a: String): MinHash = b.add(a)
    def merge(b1: MinHash, b2: MinHash): MinHash = b1.merge(b2)
    def finish(reduction: MinHash): SerializedMinHash = reduction.toBytes
    def bufferEncoder: Encoder[MinHash] = Encoders.javaSerialization[MinHash]
    def outputEncoder: Encoder[SerializedMinHash] = Encoders.BINARY
  }

  private object MinHashAggregator
      extends Aggregator[SerializedMinHash, MinHash, SerializedMinHash] {
    def zero: MinHash = trivialMinHash
    def reduce(b: MinHash, a: SerializedMinHash): MinHash = b.merge(a.toLong)
    def merge(b1: MinHash, b2: MinHash): MinHash = b1.merge(b2)
    def finish(reduction: MinHash): SerializedMinHash = reduction.toBytes
    def bufferEncoder: Encoder[MinHash] = Encoders.javaSerialization[MinHash]
    def outputEncoder: Encoder[Array[Byte]] = Encoders.BINARY
  }

  private def countUniquesPreUdf(sh: SerializedMinHash): Long = {
    val mh: MinHash = sh.toLong
    round(mh.countUniques)
  }

  private def jaccardPreUdf(
      left: SerializedMinHash,
      right: SerializedMinHash
  ): Double = {
    val leftHash = left.toLong
    val rightHash = right.toLong
    leftHash.jaccard(rightHash)
  }

  private def overlapPreUdf(
      left: SerializedMinHash,
      right: SerializedMinHash
  ): Long = {
    val leftHash = left.toLong
    val rightHash = right.toLong

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
