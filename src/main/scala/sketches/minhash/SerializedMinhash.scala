package com.ber2.spark.sketches.minhash

import java.nio.{ByteBuffer, Buffer}
import Constants.{bytesInAShort, bytesInALong}


case class SerializedMinHash(bytes: Array[Byte]) {

  def deserialize: MinHash = {
    if (bytes.length % bytesInALong != 0) {
      throw new IndexOutOfBoundsException("Cannot convert byte array to array of Long")
    }

    val numPerm = (bytes.length / bytesInALong).toShort

    val bb = ByteBuffer.allocate(bytesInALong * numPerm)
    val bbb: Buffer = bb
    bb.put(bytes)
    bbb.position(0)

    val xs = Array.fill[Long](numPerm)(1L)

    (0 until numPerm)
      .foreach { i => xs(i) = bb.getLong }

    MinHash(numPerm, xs)
  }
}
