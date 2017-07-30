package edu.njit.cs643.group7;

import java.util.Arrays;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import edu.njit.cs643.group7.model.Business;
import edu.njit.cs643.group7.model.Review;
import edu.njit.cs643.group7.util.Utils;

public class TestSpark {

	public static void main(String[] args) {
		SparkSession spark = SparkSession.builder().appName("TEST_APP_NAME").master("local[2]")
				.config("spark.executor.memory", "1g").config("spark.some.config.option", "some-value").getOrCreate();

		JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());

		// Parallelized with 2 partitions
		JavaRDD<String> rddX = sc.parallelize(
				Arrays.asList("Joseph", "Jimmy", "Tina", "Thomas", "James", "Cory", "Christine", "Jackeline", "Juan"),
				3);

		JavaPairRDD<Character, Iterable<String>> rddY = rddX.groupBy(word -> word.charAt(0));

		System.out.println(rddY.collect());
		
		Encoder<Business> businessEncoder = Encoders.bean(Business.class);
		Encoder<Review> reviewEncoder = Encoders.bean(Review.class);


		Dataset<Business> businessDS = spark.read()
				.json(ClassLoader.getSystemResource("data/sample_business_json").getPath()).as(businessEncoder);
		Dataset<Business> restaurantDS = businessDS
				.filter((FilterFunction<Business>) aBusiness -> Utils.isRestaurant(aBusiness))
				.filter((FilterFunction<Business>) aBusiness -> Utils.isRestaurant(aBusiness));
		businessDS.show();

		restaurantDS.show();

		Dataset<Review> reviewDS = spark.read().json(ClassLoader.getSystemResource("data/sample_review_json").getPath())
				.as(reviewEncoder);
		reviewDS.show();

		Dataset<Row> restaurantReviewsDS = reviewDS.join(businessDS, "business_id");
	}

}
