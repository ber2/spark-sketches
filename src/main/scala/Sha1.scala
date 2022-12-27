package minhash

import java.security.MessageDigest

case class Sha1Hash32(digest: Array[Byte]) {
  lazy val toHex: String = {
    val sb = new StringBuilder
    for (b <- digest) {
      sb.append(String.format("%02x", Byte.box(b)))
    }
    sb.toString
  }

  lazy val toLong: Long = BigInt(1, digest).toLong
}

object Sha1Hash32 {
  def digest(input: String): Sha1Hash32 = {
    val md = MessageDigest.getInstance("SHA-1")
    val croppedDigestedBytes = md.digest(input.getBytes).take(4)
    Sha1Hash32(croppedDigestedBytes)
  }
}
