package minhash

import scala.math.min
import scala.annotation.tailrec
import java.nio.ByteBuffer
import java.nio.LongBuffer

import Constants.{numPerm, seed, maxHash, hashValueByteSize}

trait MinHash {
  type MinHash = Array[Long]
  type SerializedMinHash = Array[Byte]
  val trivialMinHash: MinHash = Array.fill[Long](numPerm)(maxHash)

  @tailrec
  final def updateRecursively(mh: MinHash, ls: List[String]): MinHash = ls match {
    case s :: ss => updateRecursively(mh.update(s), ss)
    case _ => mh
  }

  implicit class MinHashOps(xs: MinHash) {

    def update(b: Array[Byte]): MinHash = {
      val h = Sha1Hash32(b).toLong
      val ps: Array[Long] = RandomLinearPermutations(h)

      xs
        .zip(ps)
        .map { case (h: Long, p: Long) => min(h, p) }
    }

    def update(s: String): MinHash = update(s.getBytes)

    def countUniques: Double = {
      val k = numPerm.toDouble
      val s = xs.map { _ / maxHash.toDouble }.sum
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
      xs.foreach{ x => bb.putLong(x) }
      bb.array
    }
  }

  implicit class SerializedMinHashOps(ys: SerializedMinHash) {
    def toLong: MinHash = {
      val bb = ByteBuffer.allocate(hashValueByteSize * numPerm)

      bb.put(ys)
      bb.position(0)

      val xs = Array.fill[Long](numPerm)(1L)

      (0 until numPerm)
        .foreach { i => xs(i) = bb.getLong }

      xs
    }
  }
}
