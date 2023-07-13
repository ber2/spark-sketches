package com.ber2.spark.sketches.theta

object Constants {
  val K = 12
  val M = 1 << K
  val ALPHA = M.toDouble / (M + 1).toDouble
  val MAX_VALUE = (Int.MaxValue & (-M)).toDouble
}
