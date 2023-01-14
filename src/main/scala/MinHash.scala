package com.ber2.spark.minhash

import scala.annotation.tailrec
import scala.math.min
import java.nio.{ByteBuffer, LongBuffer, Buffer}

import Constants.{numPerm, seed, maxHash, hashValueByteSize}

trait MinHash {
  type MinHash = Array[Long]
  type SerializedMinHash = Array[Byte]
  val trivialMinHash: MinHash = Array.fill[Long](numPerm)(maxHash)

  @tailrec
  final def addBatch(mh: MinHash, ls: List[String]): MinHash = ls match {
    case s :: ss => addBatch(mh.add(s), ss)
    case _       => mh
  }

  implicit class MinHashOps(xs: MinHash) {

    def add(b: Array[Byte]): MinHash = {
      val h = Sha1Hash32.digest(b)
      val ps: Array[Long] = RandomLinearPermutations(h)

      xs
        .zip(ps)
        .map { case (h: Long, p: Long) => min(h, p) }
    }

    def add(s: String): MinHash = add(s.getBytes)

    def countUniques: Double = {
      val k = numPerm.toDouble
      val s = xs.sum.toDouble / maxHash.toDouble
      k / s - 1.0
    }

    def jaccard(ys: MinHash): Double = {
      val matching = xs
        .zip(ys)
        .filter { case (x: Long, y: Long) => x == y }
        .length
        .toDouble

      matching / numPerm.toDouble
    }

    def merge(ys: MinHash): MinHash = {
      xs
        .zip(ys)
        .map { case (x: Long, y: Long) => min(x, y) }
    }

    def toBytes: SerializedMinHash = {
      val bb = ByteBuffer.allocate(hashValueByteSize * numPerm)
      xs.foreach { x => bb.putLong(x) }
      bb.array
    }
  }

  implicit class SerializedMinHashOps(ys: SerializedMinHash) {
    def toLong: MinHash = {
      val bb = ByteBuffer.allocate(hashValueByteSize * numPerm)
      val bbb: Buffer = bb

      bb.put(ys)
      bbb.position(0)

      val xs = Array.fill[Long](numPerm)(1L)

      (0 until numPerm)
        .foreach { i => xs(i) = bb.getLong }

      xs
    }
  }
}
