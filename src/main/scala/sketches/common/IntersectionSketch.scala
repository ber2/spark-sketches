package com.ber2.spark.sketches.common

trait IntersectionSketch[A] extends Sketch[A] {
  def intersection(a: A, b: A): A
}
