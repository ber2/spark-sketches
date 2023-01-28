package com.ber2.spark.minhash

import scala.annotation.tailrec
import scala.math.min
import java.nio.{ByteBuffer, LongBuffer, Buffer}

import Constants.{maxHash, hashValueByteSize}

case class MinHash(numPerm: Short, hashValues: Array[Long]) {
  val permutations = new RandomLinearPermutations(numPerm)

  private def checkCompatibility(other: MinHash): Unit = {
    if (numPerm != other.numPerm)
      throw new IllegalArgumentException(
        "Cannot compute Jaccard of two MinHashes with different number of permutations"
      )
  }

  def isEqual(other: MinHash): Boolean = {
    val matchingPerm = numPerm == other.numPerm
    val matchingValues = hashValues.deep == other.hashValues.deep
    matchingPerm && matchingValues
  }

  def add(b: Array[Byte]): MinHash = {
    val h = Sha1Hash32.digest(b)
    val ps: Array[Long] = permutations(h)

    val updatedHashValues = hashValues
      .zip(ps)
      .map { case (h: Long, p: Long) => min(h, p) }

    MinHash(numPerm, updatedHashValues)
  }

  def add(s: String): MinHash = add(s.getBytes)

  def countUniques: Double = {
    val k = numPerm.toDouble
    val s = hashValues.sum.toDouble / maxHash.toDouble
    k / s - 1.0
  }

  def jaccard(other: MinHash): Double = {
    checkCompatibility(other)

    val matching = hashValues
      .zip(other.hashValues)
      .filter { case (x: Long, y: Long) => x == y }
      .length
      .toDouble

    matching / numPerm.toDouble
  }

  def merge(other: MinHash): MinHash = {
    checkCompatibility(other)

    val mergedHashValues = hashValues
      .zip(other.hashValues)
      .map { case (x: Long, y: Long) => min(x, y) }

    MinHash(numPerm, mergedHashValues)
  }

  def serialized: SerializedMinHash = {
    val bb = ByteBuffer.allocate(hashValueByteSize * numPerm)
    hashValues.foreach { x => bb.putLong(x) }
    SerializedMinHash(bb.array)
  }
}

object MinHash {
  def trivialMinHash(numPerm: Short): MinHash =
    MinHash(numPerm, Array.fill[Long](numPerm)(maxHash))

  @tailrec
  final def addBatch(mh: MinHash, ls: List[String]): MinHash = ls match {
    case s :: ss => addBatch(mh.add(s), ss)
    case _       => mh
  }
}
