package com.ber2.spark.sketches.theta

import scala.math.{round, log, pow, min, max}
import scala.util.hashing.MurmurHash3

import java.nio.ByteBuffer

import Constants.{M, ALPHA}

case class ThetaSketch(theta: Double, hashValues: Set[Long]) {
  def update(s: String): ThetaSketch = {
    val hash = MurmurHash3.bytesHash(s.getBytes).toLong
    val i = hash & ((1 << M) - 1)
    val r = (hash >>> M) | (1L << 63 - M)

    val newTheta = min(pow(ALPHA, i), theta)
    val newHashes = (hashValues + r).filter(_ < newTheta)

    ThetaSketch(newTheta, newHashes)
  }

  def getEstimate: Long = round(hashValues.size.toDouble / theta)

  def union(other: ThetaSketch): ThetaSketch = {
    val newTheta = min(theta, other.theta)
    val newValues = (hashValues ++ other.hashValues).filter(_ < newTheta)
    ThetaSketch(newTheta, newValues)
  }

  def intersection(other: ThetaSketch): ThetaSketch = {
    val newTheta = max(theta, other.theta)
    val newHashes = (hashValues intersect other.hashValues)
    ThetaSketch(newTheta, newHashes)
  }

  def aNotB(other: ThetaSketch): ThetaSketch =
    ThetaSketch(theta, hashValues -- other.hashValues)

  def serialized: SerializedThetaSketch = {
    val hashLength = hashValues.size
    val bb = ByteBuffer.allocate(8 + 4 + 8 * hashLength)
    bb.putDouble(theta)
    bb.putInt(hashLength)
    hashValues.foreach { x => bb.putLong(x) }
    SerializedThetaSketch(bb.array)
  }

}

object ThetaSketch {
  def apply(): ThetaSketch = ThetaSketch(1.0, Set.empty[Long])

  def fromStrings(values: Seq[String]): ThetaSketch =
    values.foldLeft(ThetaSketch()) { (th: ThetaSketch, s: String) =>
      th.update(s)
    }
}
