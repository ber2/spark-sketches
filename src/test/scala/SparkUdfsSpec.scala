package minhash

import org.apache.spark.sql.DataFrame

class SparkUdfsSpec extends SparkBaseSpec {

  behavior of "stringToHash"

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
      ("key_1_2", "key_2_2", "document_6"),
      ("key_1_2", "key_2_2", "document_7"),
      ("key_1_2", "key_2_2", "document_8"),
    ).toDF("key_1", "key_2", "document")
  }

  it should "do something" in {
    import spark.implicits._

    val directTransform = data
      .groupBy($"key_1")
      .agg(SparkUdfs.aggStringToHash($"document").as("minhash"))
      .withColumn("cnt", SparkUdfs.countUniques($"minhash"))

    val preAgg = data
      .groupBy($"key_1", $"key_2")
      .agg(SparkUdfs.aggStringToHash($"document").as("minhash"))

    val indirectTransform = preAgg
      .groupBy($"key_1")
      .agg(SparkUdfs.aggHashes($"minhash").as("minhash"))
      .withColumn("cnt", SparkUdfs.countUniques($"minhash"))

    assertDataFrameNoOrderEquals(directTransform, indirectTransform)
  }
}
