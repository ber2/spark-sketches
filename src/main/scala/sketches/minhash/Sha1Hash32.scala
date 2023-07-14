package com.ber2.spark.sketches.minhash

import java.security.MessageDigest
import java.nio.{ByteBuffer, ByteOrder}

object Sha1Hash32 {
  def digest(input: Array[Byte]): Long = {
    val md = MessageDigest.getInstance("SHA-1")
    val croppedDigestedBytes = md.digest(input).take(4)

    val bb = ByteBuffer.wrap(croppedDigestedBytes)
    bb.order(ByteOrder.LITTLE_ENDIAN).getInt & 0xffffffffL
  }
}
