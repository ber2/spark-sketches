package com.ber2.spark.sketches.theta

import java.nio.{ByteBuffer, Buffer}

case class SerializedThetaSketch(bytes: Array[Byte]) {
  def deserialized: ThetaSketch = {
    val totalBytes = bytes.length
    val bb = ByteBuffer.allocate(totalBytes)
    bb.put(bytes).rewind
    val theta = bb.getDouble
    val length = bb.getInt

    val xs = new Array[Long](length)

    (0 until length)
      .foreach { i => xs(i) = bb.getLong }

    ThetaSketch(theta, xs.toSet)
  }
}


