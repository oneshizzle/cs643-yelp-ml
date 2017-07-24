/**
 * 
 */
package edu.njit.cs643.group7;

import org.apache.avro.ipc.specific.Person;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;

import edu.njit.cs643.group7.model.Business;

/**
 * Yelp Restaurant Recommender App
 *
 */
public class YelpRR {

	public static final String APP_NAME = "YelpRestaurantRecommender";
	public static final String CLUSTER = "local";
	private static SparkSession spark;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		spark = SparkSession.builder().appName(APP_NAME).master("local[2]").config("spark.executor.memory", "1g")
				.config("spark.some.config.option", "some-value").getOrCreate();
		Encoder<Business> businessEncoder = Encoders.bean(Business.class);
		
		Dataset<Business> businessDS = spark.read()
				.json(ClassLoader.getSystemResource("data/sample_business_json").getPath()).as(businessEncoder);
		businessDS.show();
	}

}
