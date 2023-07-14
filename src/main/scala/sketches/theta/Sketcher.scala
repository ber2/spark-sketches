package com.ber2.spark.sketches.theta

import java.nio.{ByteBuffer, Buffer}

import scala.util.hashing.MurmurHash3
import scala.math.{max, min, pow, round, abs}

import com.ber2.spark.sketches.common.{IntersectionSketch, SetDifferenceSketch}

import Constants.{M, ALPHA, MAX_VALUE}

class Sketcher
    extends IntersectionSketch[Theta]
    with SetDifferenceSketch[Theta] {

  def emptySketch: Theta = Theta(1.0, Set.empty[Long])

  def update(a: Theta, s: String): Theta = {
    val hash = abs(MurmurHash3.bytesHash(s.getBytes).toLong)

    val prefix = hash & (M - 1)
    val suffix = hash & (-M)

    val i = round(Math.log(a.theta) / Math.log(ALPHA))

    if (suffix.toDouble / MAX_VALUE.toDouble < pow(ALPHA, i)) {
      val th = pow(ALPHA, i + 1)
      val hs = (a.hashes + prefix).filter(_ < th * MAX_VALUE)
      Theta(th, hs)
    } else a
  }

  def union(a: Theta, b: Theta): Theta = {
    val th = min(a.theta, b.theta)
    val hs = (a.hashes ++ b.hashes).filter(_ < th * MAX_VALUE)
    Theta(th, hs)
  }

  def getEstimate(a: Theta): Long = round(a.hashes.size.toDouble / a.theta)

  def serialize(a: Theta): Array[Byte] = {
    val l = a.hashes.size
    val bb = ByteBuffer.allocate(8 + 4 + 8 * l)
    bb.putDouble(a.theta)
    bb.putInt(l)
    a.hashes.foreach(bb.putLong(_))
    bb.array
  }

  def deserialize(xs: Array[Byte]): Theta = {
    val tb = xs.length
    val bb = ByteBuffer.allocate(tb)
    val bbb: Buffer = bb
    bb.put(xs)
    bbb.position(0)
    val th = bb.getDouble
    val l = bb.getInt

    val hs = new Array[Long](l)
    (0 until l)
      .foreach { i => hs(i) = bb.getLong }

    Theta(th, hs.toSet)
  }

  def intersection(a: Theta, b: Theta): Theta = {
    val th = min(a.theta, b.theta)
    val hs = (a.hashes intersect b.hashes).filter(_ < th * MAX_VALUE)
    Theta(th, hs)
  }

  def aNotB(a: Theta, b: Theta): Theta = Theta(a.theta, a.hashes -- b.hashes)

}
