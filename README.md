# spark-minhash

This is an implementation of the [MinHash algorithm](https://en.wikipedia.org/wiki/MinHash) in **Scala**.

This algorithm allows to store a collection of values in a digested way, in the form of a so-called _sketch_. One may think that, instead of storing the original information, the _sketch_ stores a summary of the values, in such a way enough information is retained to compute:

- An estimation of the unique values in the collection.
- Given another sketch, an estimation of the [Jaccard index](https://en.wikipedia.org/wiki/Jaccard_index) of the two. 

On one hand, obtaining a sketch uses constant memory and involves a single pass through the original values. On the other hand, sketches may be updated in linear time by adding new values, and it is also possible to obtain the _merge_ of two sketches in linear time.

In order to implement the algorithm on **Spark dataframes**, we have decided to:

- Supply methods to serialize and deserialize sketches into the Scala `Array[Byte]` type, which is compatible with Spark's native `BinaryType`. This is a very fast and natural way to store sketches as a Spark column.

- Provide two User Defined Aggregated Functions ([UDAFs](https://spark.apache.org/docs/latest/sql-ref-functions-udf-aggregate.html)):
  - `aggStringToHash`, which allows to group string values in a DataFrame column into a sketch (preaggregation).
  - `aggHashes`, which merges a collection of hashes into a single one (further aggregation).

- Provide three User-Defined Functions ([UDFs](https://spark.apache.org/docs/latest/sql-ref-functions-udf-scalar.html)) to turn sketches into numeric values:
	- `countUniques` estimates the number of unique values in a data sketch.
	- `jaccard` estimates the Jaccard index of two sketches.
	- `overlap` uses the Jaccard index in order to estimate the number of values in common between two sketches.

