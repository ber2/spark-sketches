package minhash

object Constants {
  // These values are used to generate the permutations
  val mersennePrime: Long = (1L << 61) - 1
  val maxHash: Long = (1L << 32) - 1

  // Do we need this?
  val hashValueByteSize: Int = 8 // len(bytes(np.int64(42))): int

  val seed: Int = 1
  val numPerm: Int = 256
}
