package com.ber2.spark.sketches.common

trait Sketch[A] extends Serializable {
  def emptySketch: A
  def update(a: A, s: String): A
  def union(a: A, b: A): A
  def getEstimate(a: A): Double

  def fromStrings(ss: Seq[String]): A =
    ss.foldLeft(emptySketch) { (a: A, s: String) => update(a, s) }

  def serialize(a: A): Array[Byte]
  def deserialize(xs: Array[Byte]): A
}
