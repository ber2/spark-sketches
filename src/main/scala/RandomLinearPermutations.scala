package minhash

import scala.util.Random

import Constants.{numPerm, seed, mersennePrime, maxHash}

object RandomLinearPermutations {
  private val generator = new Random(seed)

  private def nextLong(min: Long, max: Long = mersennePrime): Long =
    generator.nextLong match {
      case x if (x >= min && x <= max) => x
      case _                           => nextLong(min, max)
    }

  val w: Array[Long] = Array.fill[Long](numPerm)(nextLong(1))
  val b: Array[Long] = Array.fill[Long](numPerm)(nextLong(0))

  def apply(v: Long): Array[Long] = w
    .zip(b)
    .map { case (w: Long, b: Long) =>
      ((w * v + b) % mersennePrime) & maxHash
    }
}
