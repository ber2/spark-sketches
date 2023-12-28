package com.ber2.spark.sketches.theta

import java.util.UUID

import org.apache.spark.sql.{DataFrame, Dataset}
import com.ber2.spark.sketches.common.SparkBaseSpec
import com.ber2.spark.sketches.theta.SparkUdfs.{
  stringsToSketch,
  aggSketches,
  getEstimate,
  intersection,
  setDifference
}

case class KeyId(key: String, id: String)

case class KeySketch(key: String, sketch: Array[Byte], uniques: Double)

case class FinalResult(sketch: Array[Byte], uniques: Double)

class PythonIntegrationSpec extends SparkBaseSpec {

  behavior of "Python Integration"

  it should "haha" in {
    import spark.implicits._

    val keys = (0 to 10).map(d => s"key_$d")
    val docs: Dataset[KeyId] = keys.flatMap { key =>
      (0 to 10000).map { _ => KeyId(key, UUID.randomUUID.toString) }
    }.toDS

    val sketches = docs
      .groupBy($"key")
      .agg(stringsToSketch($"id") as "sketch")
      .withColumn("uniques", getEstimate($"sketch"))
      .as[KeySketch]

    docs.show
    sketches.show
    sketches.coalesce(1).write.mode("overwrite").parquet("./preagg/")

    val result = sketches
      .agg(aggSketches($"sketch") as "sketch")
      .withColumn("uniques", getEstimate($"sketch"))
      .as[FinalResult]

    result.show
    result.coalesce(1).write.mode("overwrite").parquet("./result/")

  }
}
