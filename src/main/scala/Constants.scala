package com.ber2.spark.minhash

object Constants {
  val mersennePrime: Long = (1L << 61) - 1
  val maxHash: Long = (1L << 32) - 1

  val bytesInAShort: Int = 2
  val bytesInALong: Int = 8

  val seed: Int = 1
  val numPerm: Short = 256
}
