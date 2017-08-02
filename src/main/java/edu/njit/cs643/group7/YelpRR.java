package edu.njit.cs643.group7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.recommendation.ALS;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.storage.StorageLevel;

import edu.njit.cs643.group7.model.Business;
import edu.njit.cs643.group7.model.Review;
import edu.njit.cs643.group7.model.User;
import edu.njit.cs643.group7.util.Utils;
import scala.Tuple2;

public class YelpRR {

	public static final String APP_NAME = "YelpRestaurantRecommender";
	public static final String CLUSTER = "local";
	private static SparkSession spark;

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		spark = SparkSession.builder().appName(APP_NAME).getOrCreate();

		String bizJsonPath = "hdfs://hadoop-master:8020/user/ec2-user/yelp_academic_dataset_business.json";
		String reviewJsonPath = "hdfs://hadoop-master:8020/user/ec2-user/yelp_academic_dataset_review.json";
		String userJsonPath = "hdfs://hadoop-master:8020/user/ec2-user/yelp_academic_dataset_user.json";

		Encoder<Business> businessEncoder = Encoders.bean(Business.class);
		Encoder<Review> reviewEncoder = Encoders.bean(Review.class);
		Encoder<User> userEncoder = Encoders.bean(User.class);

		Dataset<Business> businessDS = spark.read().json(bizJsonPath).as(businessEncoder);
		Dataset<Business> restaurantDS = businessDS.filter((FilterFunction<Business>) aBusiness -> Utils.isRestaurant(aBusiness));
		// businessDS.show();

		// restaurantDS.show();

		Dataset<Review> reviewDS = spark.read().json(reviewJsonPath).as(reviewEncoder);

		Dataset<Review> reviewpre2016DS = spark.read().json(reviewJsonPath).as(reviewEncoder)
				.filter((FilterFunction<Review>) aReview -> !aReview.getDate().substring(0, 4).equalsIgnoreCase("2017"))
				.filter((FilterFunction<Review>) aReview -> !aReview.getDate().substring(0, 4).equalsIgnoreCase("2016"));

		Dataset<Review> reviewpost2016DS = spark.read().json(reviewJsonPath).as(reviewEncoder)
				.filter((FilterFunction<Review>) aReview -> aReview.getDate().substring(0, 4).equalsIgnoreCase("2017")
						|| aReview.getDate().substring(0, 4).equalsIgnoreCase("2016"));

		// reviewDS.show();

		Dataset<Row> restaurantOldReviewsDS = reviewpre2016DS.join(restaurantDS.select("business_id", "name", "state"), "business_id").distinct();
		Dataset<Row> restaurantNewReviewsDS = reviewpost2016DS.join(restaurantDS.select("business_id", "name", "state"), "business_id").distinct();
		Dataset<Row> restaurantReviewsDS = reviewDS.join(restaurantDS.select("business_id", "name", "state"), "business_id").distinct();

		System.out.println("Getting Common Users (Reviewers) before and after 2016: ");
		// find common reviews across dataset
		Dataset<Row> commonUsers = restaurantOldReviewsDS
				.filter((FilterFunction<Row>) aRow -> Arrays.asList("NJ", "NY", "CT").contains(aRow.getAs("state")))
				.join(restaurantNewReviewsDS, "user_id")
				.filter((FilterFunction<Row>) aRow -> Arrays.asList("NJ", "NY", "CT").contains(aRow.getAs("state"))).select("user_id").distinct();

		commonUsers.show();

		JavaRDD<Tuple2<Integer, Rating>> ratings = restaurantReviewsDS.javaRDD().map(new Function<Row, Tuple2<Integer, Rating>>() {
			public Tuple2<Integer, Rating> call(Row aRow) throws Exception {
				Integer cacheStamp = Integer.valueOf(aRow.getAs("review_id").hashCode()) % 10;
				Rating rating = new Rating(Integer.valueOf(aRow.getAs("user_id").hashCode()), Integer.valueOf(aRow.getAs("business_id").hashCode()),
						Double.parseDouble(aRow.getAs("stars").toString()));
				return new Tuple2<Integer, Rating>(cacheStamp, rating);
			}
		});

		JavaRDD<Tuple2<Integer, Rating>> pre2016Ratings = restaurantOldReviewsDS.javaRDD().map(new Function<Row, Tuple2<Integer, Rating>>() {
			public Tuple2<Integer, Rating> call(Row aRow) throws Exception {
				Integer cacheStamp = Integer.valueOf(aRow.getAs("review_id").hashCode()) % 10;
				Rating rating = new Rating(Integer.valueOf(aRow.getAs("user_id").hashCode()), Integer.valueOf(aRow.getAs("business_id").hashCode()),
						Double.parseDouble(aRow.getAs("stars").toString()));
				return new Tuple2<Integer, Rating>(cacheStamp, rating);
			}
		});

		Map<Integer, String> products = restaurantReviewsDS.javaRDD().mapToPair(new PairFunction<Row, Integer, String>() {
			public Tuple2<Integer, String> call(Row aRow) throws Exception {
				return new Tuple2<Integer, String>(Integer.valueOf(aRow.getAs("business_id").hashCode()), aRow.getAs("name").toString());
			}
		}).collectAsMap();

		long ratingCount = ratings.count();
		long userCount = ratings.map(new Function<Tuple2<Integer, Rating>, Object>() {
			public Object call(Tuple2<Integer, Rating> tuple) throws Exception {
				return tuple._2().user();
			}
		}).distinct().count();

		long restaurantCount = ratings.map(new Function<Tuple2<Integer, Rating>, Object>() {
			public Object call(Tuple2<Integer, Rating> tuple) throws Exception {
				return tuple._2().product();
			}
		}).distinct().count();

		System.out.println("Got " + ratingCount + " ratings from " + userCount + " users on " + restaurantCount + " restaurants.");

		// Splitting training data
		int numPartitions = 10;
		// training data set
		JavaRDD<Rating> training = ratings.filter(new Function<Tuple2<Integer, Rating>, Boolean>() {
			public Boolean call(Tuple2<Integer, Rating> tuple) throws Exception {
				return tuple._1() < 6;
			}
		}).map(new Function<Tuple2<Integer, Rating>, Rating>() {
			public Rating call(Tuple2<Integer, Rating> tuple) throws Exception {
				return tuple._2();
			}
		}).repartition(numPartitions).cache();

		StorageLevel storageLevel = new StorageLevel();
		// validation data set
		JavaRDD<Rating> validation = ratings.filter(new Function<Tuple2<Integer, Rating>, Boolean>() {
			public Boolean call(Tuple2<Integer, Rating> tuple) throws Exception {
				return tuple._1() >= 6 && tuple._1() < 8;
			}
		}).map(new Function<Tuple2<Integer, Rating>, Rating>() {
			public Rating call(Tuple2<Integer, Rating> tuple) throws Exception {
				return tuple._2();
			}
		}).repartition(numPartitions).persist(storageLevel);

		// test data set
		JavaRDD<Rating> test = ratings.filter(new Function<Tuple2<Integer, Rating>, Boolean>() {
			public Boolean call(Tuple2<Integer, Rating> tuple) throws Exception {
				return tuple._1() >= 8;
			}
		}).map(new Function<Tuple2<Integer, Rating>, Rating>() {
			public Rating call(Tuple2<Integer, Rating> tuple) throws Exception {
				return tuple._2();
			}
		}).persist(storageLevel);

		long numTraining = training.count();
		long numValidation = validation.count();
		long numTest = test.count();

		System.out.println("Training: " + numTraining + ", validation: " + numValidation + ", test: " + numTest + " \n");

		// training model
		int[] ranks = { 8, 12 };
		float[] lambdas = { 0.1f, 10.0f };
		int[] numIters = { 10, 20 };

		double bestValidationRmse = Double.MAX_VALUE;
		int bestRank = 0;
		float bestLambda = -1.0f;
		int bestNumIter = -1;
		MatrixFactorizationModel bestModel = null;

		for (int currentRank : ranks) {
			for (float currentLambda : lambdas) {
				for (int currentNumIter : numIters) {
					MatrixFactorizationModel model = ALS.train(JavaRDD.toRDD(training), currentRank, currentNumIter, currentLambda);

					Double validationRmse = computeRMSE(model, validation);
					System.out.println("RMSE (validation) = " + validationRmse + " for the model trained with rank = " + currentRank + ", lambda = "
							+ currentLambda + ", and numIter = " + currentNumIter + ".");

					if (validationRmse < bestValidationRmse) {
						bestModel = model;
						bestValidationRmse = validationRmse;
						bestRank = currentRank;
						bestLambda = currentLambda;
						bestNumIter = currentNumIter;
					}
				}
			}
		}

		/**
		 * Dataset<Review> review2017DS =
		 * spark.read().json(reviewJsonPath).as(reviewEncoder)
		 * .filter((FilterFunction<Review>) aReview -> aReview.getDate().substring(0,
		 * 4).equalsIgnoreCase("2017") || aReview.getDate().substring(0,
		 * 4).equalsIgnoreCase("2016"));
		 * 
		 **/

		Dataset<Business> njAreaRestaurantDS = restaurantDS
				.filter((FilterFunction<Business>) aRestaurant -> Arrays.asList("NJ", "NY", "CT").contains(aRestaurant.getState()));

		/**
		 * Dataset<Row> restaurantReviews2017DS =
		 * review2017DS.join(njAreaRestaurantDS.select("name", "business_id"),
		 * "business_id").distinct() .select("name", "business_id", "user_id",
		 * "review_id", "stars");
		 * 
		 **/

		System.out.println("\n" + restaurantDS.count() + " total number of restaurants");

		System.out.println(njAreaRestaurantDS.count() + " number of nj area restaurants");

		// Computing Root Mean Square Error in the test dataset
		Double testRmse = computeRMSE(bestModel, test);
		System.out.println("\nThe best model was trained with rank = " + bestRank + " and lambda = " + bestLambda + ", and numIter = " + bestNumIter
				+ ", and its RMSE on the test set is " + testRmse + ".");

		final MatrixFactorizationModel finalModel = bestModel;

		List<Row> user_ids = commonUsers.select("user_id").collectAsList();

		for (Row userRow : user_ids) {
			String user_id = userRow.mkString();

			System.out.println("\nGetting recommendations for: " + user_id);

			List<Rating> recommendations = getRecommendations(user_id, finalModel, pre2016Ratings, njAreaRestaurantDS);

			Dataset<Row> actualpost2016Reviews = restaurantNewReviewsDS
					.filter((FilterFunction<Row>) aReview -> aReview.getAs("user_id").toString().equalsIgnoreCase(user_id))
					.filter((FilterFunction<Row>) aRow -> Arrays.asList("NJ", "NY", "CT").contains(aRow.getAs("state")))
					.select("user_id", "business_id", "stars", "name").cache();

			for (Rating recommendation : recommendations) {
				Dataset<Business> restaurant = restaurantDS
						.filter((FilterFunction<Business>) aRestaurant -> (aRestaurant.getBusiness_id().hashCode() == recommendation.product()));
				System.out.println("\nRestaurant: " + restaurant.first().getName() + " >> Predicted Score: " + recommendation.rating());

				Dataset<Row> actualReview = actualpost2016Reviews
						.filter((FilterFunction<Row>) aReview -> aReview.getAs("user_id").toString().hashCode() == recommendation.user()
								&& aReview.getAs("business_id").toString().hashCode() == recommendation.product());

				if (actualReview.count() > 0) {
					System.out.println(
							"\nRestaurant: " + actualReview.first().getAs("name") + " >> Actual Score: " + actualReview.first().getAs("stars"));
				}

			}
		}

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("\nTime taken in ms: " + elapsedTime);

	}

	public static Double computeRMSE(MatrixFactorizationModel model, JavaRDD<Rating> data) {
		JavaRDD<Tuple2<Object, Object>> userProducts = data.map(new Function<Rating, Tuple2<Object, Object>>() {
			public Tuple2<Object, Object> call(Rating r) {
				return new Tuple2<Object, Object>(r.user(), r.product());
			}
		});

		JavaPairRDD<Tuple2<Integer, Integer>, Double> predictions = JavaPairRDD.fromJavaRDD(
				model.predict(JavaRDD.toRDD(userProducts)).toJavaRDD().map(new Function<Rating, Tuple2<Tuple2<Integer, Integer>, Double>>() {
					public Tuple2<Tuple2<Integer, Integer>, Double> call(Rating r) {
						return new Tuple2<Tuple2<Integer, Integer>, Double>(new Tuple2<Integer, Integer>(r.user(), r.product()), r.rating());
					}
				}));
		JavaRDD<Tuple2<Double, Double>> predictionsAndRatings = JavaPairRDD
				.fromJavaRDD(data.map(new Function<Rating, Tuple2<Tuple2<Integer, Integer>, Double>>() {
					public Tuple2<Tuple2<Integer, Integer>, Double> call(Rating r) {
						return new Tuple2<Tuple2<Integer, Integer>, Double>(new Tuple2<Integer, Integer>(r.user(), r.product()), r.rating());
					}
				})).join(predictions).values();

		double mse = JavaDoubleRDD.fromRDD(predictionsAndRatings.map(new Function<Tuple2<Double, Double>, Object>() {
			public Object call(Tuple2<Double, Double> pair) {
				Double err = pair._1() - pair._2();
				return err * err;
			}
		}).rdd()).mean();

		return Math.sqrt(mse);

	}

	/**
	 * Returns the list of recommendations for a given user
	 *
	 * @param userId
	 *            user id.
	 * @param model
	 *            best model.
	 * @param ratings
	 *            rating data.
	 * @param products
	 *            product list.
	 * @return The list of recommended restaurants.
	 */
	private static List<Rating> getRecommendations(String userId, MatrixFactorizationModel model, JavaRDD<Tuple2<Integer, Rating>> ratings,
			Dataset<Business> restaurantInterestSet) {
		List<Rating> recommendations;

		// Getting the users ratings
		// System.out.println("ratings: " + ratings.count());

		// System.out.println("userId: " + userId + " " + userId.hashCode());

		JavaRDD<Rating> userRatings = ratings.filter(new Function<Tuple2<Integer, Rating>, Boolean>() {
			public Boolean call(Tuple2<Integer, Rating> tuple) throws Exception {
				return tuple._2().user() == userId.hashCode();
			}
		}).map(new Function<Tuple2<Integer, Rating>, Rating>() {
			public Rating call(Tuple2<Integer, Rating> tuple) throws Exception {
				return tuple._2();
			}
		});

		// System.out.println("userRatings: " + userRatings.count());

		// Getting the restaurants ID's of the restaurants that user rated
		JavaRDD<Tuple2<Object, Object>> userProducts = userRatings.map(new Function<Rating, Tuple2<Object, Object>>() {
			public Tuple2<Object, Object> call(Rating r) {
				return new Tuple2<Object, Object>(r.user(), r.product());
			}
		});

		// System.out.println("userProducts: " + userProducts.count());

		List<Integer> productSet = new ArrayList<Integer>();
		List<Integer> visitedSet = new ArrayList<Integer>();

		restaurantInterestSet.collectAsList().stream().forEach(x -> productSet.add(x.getBusiness_id().hashCode()));

		Iterator<Tuple2<Object, Object>> productIterator = userProducts.toLocalIterator();

		// Removing the user visited (rated) set from the all product set
		while (productIterator.hasNext()) {
			Integer businessId = (Integer) productIterator.next()._2();
			// System.out.println("BusinessId: " + businessId);
			if (productSet.contains(businessId)) {
				productSet.remove(businessId);
				visitedSet.add(businessId);
			}
		}

		JavaRDD<Integer> candidates = new JavaSparkContext(spark.sparkContext()).parallelize(productSet);
		// System.out.println("candidates: " + candidates.count());

		JavaRDD<Tuple2<Integer, Integer>> userCandidates = candidates.map(new Function<Integer, Tuple2<Integer, Integer>>() {
			public Tuple2<Integer, Integer> call(Integer integer) throws Exception {
				return new Tuple2<Integer, Integer>(userId.hashCode(), integer);
			}
		});

		// System.out.println("userCandidates: " + userCandidates.count());

		// Predict recommendations for the given user
		recommendations = model.predict(JavaPairRDD.fromJavaRDD(userCandidates)).collect();
		List<Rating> sortedList = new ArrayList<Rating>();

		for (Rating recommendation : recommendations) {
			sortedList.add(recommendation);
		}

		// Sorting the recommended products and sort them according to the rating

		Collections.sort(sortedList, new Comparator<Rating>() {
			public int compare(Rating r1, Rating r2) {
				return r1.rating() < r2.rating() ? -1 : r1.rating() > r2.rating() ? 1 : 0;
			}
		});

		System.out.println("\nRecommendations sort completed");

		Collections.reverse(sortedList);
		int max = sortedList.size() < 10 ? sortedList.size() : 11;
		// get top 50 from the recommended products.
		if (max > 0) {
			sortedList = sortedList.subList(0, max - 1);
		}

		return sortedList;
	}
}
