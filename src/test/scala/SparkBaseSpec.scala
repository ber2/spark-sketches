package minhash

import com.holdenkarau.spark.testing.{DataFrameSuiteBase, SharedSparkContext}
import org.apache.spark.SparkConf
import org.apache.log4j.LogManager

abstract class SparkBaseSpec
    extends BaseSpec
    with SharedSparkContext
    with DataFrameSuiteBase {
  implicit val log = LogManager.getLogger("TestLogger")
}
