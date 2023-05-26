package com.ber2.spark.sketches.common

trait SetDifferenceSketch[A] extends Sketch[A] {
  def aNotB(a: A, b: A): A
}
