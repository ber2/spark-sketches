package minhash

import org.apache.spark.sql.DataFrame

class SparkUdfsSpec extends SparkBaseSpec {

  behavior of "Spark Minhash UDFs and UDAFs"

  lazy val data: DataFrame = {
    import spark.implicits._
    Seq(
      ("key_1_1", "key_2_1", "document_1"),
      ("key_1_1", "key_2_1", "document_2"),
      ("key_1_1", "key_2_1", "document_3"),
      ("key_1_1", "key_2_1", "document_4"),
      ("key_1_1", "key_2_2", "document_1"),
      ("key_1_1", "key_2_2", "document_6"),
      ("key_1_1", "key_2_2", "document_7"),
      ("key_1_1", "key_2_2", "document_8"),
      ("key_1_2", "key_2_1", "document_1"),
      ("key_1_2", "key_2_1", "document_2"),
      ("key_1_2", "key_2_1", "document_3"),
      ("key_1_2", "key_2_1", "document_4"),
      ("key_1_2", "key_2_2", "document_5"),
      ("key_1_2", "key_2_2", "document_9"),
      ("key_1_2", "key_2_2", "document_10"),
      ("key_1_2", "key_2_2", "document_11")
    ).toDF("key_1", "key_2", "document")
  }

  lazy val aggData = {
    import spark.implicits._

    data
      .groupBy($"key_1")
      .agg(SparkUdfs.aggStringToHash($"document").as("minhash"))
      .withColumn("cnt", SparkUdfs.countUniques($"minhash"))
  }

  it should "aggregate and merge" in {
    import spark.implicits._

    val preAgg = data
      .groupBy($"key_1", $"key_2")
      .agg(SparkUdfs.aggStringToHash($"document").as("minhash"))

    val indirectTransform = preAgg
      .groupBy($"key_1")
      .agg(SparkUdfs.aggHashes($"minhash").as("minhash"))
      .withColumn("cnt", SparkUdfs.countUniques($"minhash"))

    assertDataFrameNoOrderEquals(aggData, indirectTransform)
  }

  it should "count uniques" in {
    import spark.implicits._

    val expectedCounts = Seq(
      ("key_1_1", 7L),
      ("key_1_2", 8L)
    ).toDF("key_1", "cnt")

    val actualCounts = aggData
      .drop($"minhash")

    assertDataFrameNoOrderEquals(expectedCounts, actualCounts)
  }

  it should "compute jaccards" in {
    import spark.implicits._

    val left = aggData.filter($"key_1" === "key_1_1").drop($"cnt")
    val right = aggData.filter($"key_1" === "key_1_2").drop($"cnt")

    val expectedJaccard = Seq(0.359375).toDF("jaccard")

    val actualJaccard =
      left
        .as("l")
        .crossJoin(right.as("r"))
        .withColumn("jaccard", SparkUdfs.jaccard($"l.minhash", $"r.minhash"))
        .select($"jaccard")

    assertDataFrameApproximateEquals(expectedJaccard, actualJaccard, 1e-3)
  }

  it should "compute overlaps" in {
    import spark.implicits._

    val left = aggData.filter($"key_1" === "key_1_1").drop($"cnt")
    val right = aggData.filter($"key_1" === "key_1_2").drop($"cnt")

    val expectedOverlap = Seq(4L).toDF("overlap")

    val actualOverlap =
      left
        .as("l")
        .crossJoin(right.as("r"))
        .withColumn("overlap", SparkUdfs.overlap($"l.minhash", $"r.minhash"))
        .select($"overlap")

    assertDataFrameEquals(expectedOverlap, actualOverlap)
  }
}
