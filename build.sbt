lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.12"
    )),
    name := "scalatest-example"
  )

val libVersions = Map(
  "spark" -> "3.0.0",
  "spark-testing" -> "3.0.0_1.3.0",
  "scalatest" -> "3.2.14",
  "scalacheck" -> "1.15.4",
)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % libVersions("spark") % "provided",
  "org.apache.spark" %% "spark-sql" % libVersions("spark") % "provided",
  "org.scalatest" %% "scalatest" % libVersions("scalatest") % Test,
  "com.holdenkarau" %% "spark-testing-base" % libVersions("spark-testing") % Test,
  "org.scalacheck" %% "scalacheck" % libVersions("scalacheck") % Test
)

Test / fork := true
Test / parallelExecution := false
Test / logBuffered := false
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:+CMSClassUnloadingEnabled")

