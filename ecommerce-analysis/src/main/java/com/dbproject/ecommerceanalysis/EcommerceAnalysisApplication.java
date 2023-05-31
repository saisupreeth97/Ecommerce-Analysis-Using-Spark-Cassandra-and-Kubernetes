package com.dbproject.ecommerceanalysis;

import  static com.datastax.spark.connector.japi.CassandraJavaUtil.*;

import com.datastax.spark.connector.japi.CassandraRow;
import com.dbproject.ecommerceanalysis.dao.AvgPrice;
import com.dbproject.ecommerceanalysis.dao.BrandByCategory;
import com.dbproject.ecommerceanalysis.dao.BrandByCategoryEvent;
import com.dbproject.ecommerceanalysis.dao.EventCount;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

import java.math.BigInteger;
import java.util.Collections;

public class EcommerceAnalysisApplication {

	//Constants for keyspace and tables
	private static final String KEYSPACE= "ecommerce";
	private static final String EVENT_COUNT_TABLE= "event_count";
	private static final String AVG_PRICE_TABLE= "avg_price";
	private static final String BRANDS_BY_CAT_TABLE= "brand_by_category";
	private static final String BRANDS_BY_CAT_TABLE_EVENT= "brand_by_category_event";

	public static void main(String[] args) {

		//Schema class for dropping the output tables and creating them for each job run
		CassandraSchema cassandraSchema = new CassandraSchema();

		//Set the spark configuration
		SparkConf conf = new SparkConf();
		conf.setAppName("Ecommerce Analysis");
		//set the hostname and port through command line arguments
		conf.set("spark.cassandra.connection.host", args[0]);
		conf.set("spark.cassandra.connection.port", args[1]);

		//Create the java spark context object
		JavaSparkContext sc = new JavaSparkContext(conf);
		cassandraSchema.createTablesNew(args[0]);

		//get data from DB
		JavaRDD<String> initialData = getDataFromDB(sc);

		//event_count job to analyze the number of views, purchase and items added to the cart
		JavaPairRDD<String, Integer> eventCount = eventTypeStatics(initialData);

		//calculate the average price of the purchase
		calculateAvgPricePurchase(sc,eventCount, initialData);

		//generate the brands count by category
		//here category id passed as command line argument
		brandsByCategory(args[2], initialData);

		//generate the brands count by category and event_type (I.e., view, purchase and cart)
		brandsByCategoryAndType(args[2], initialData,args[3]);
		sc.stop();
	}

	private static JavaRDD<String> getDataFromDB(JavaSparkContext sc){
		//Get data from cassandra using spark cassandra connector
		return javaFunctions(sc).cassandraTable("ecommerce","orders")
						.map(new Function<CassandraRow, String>() {
							@Override
							public String call(CassandraRow v1) throws Exception {
								return v1.columnValues().mkString(",");
							}
						});
	}

	private static JavaPairRDD<String, Integer> eventTypeStatics(JavaRDD<String> initialData)
	{
		EventCount event = new EventCount();
		//job for event count
		JavaPairRDD<String, Integer> eventCount = initialData.mapToPair(row -> new Tuple2<>(row.split(",")[0], 1))
				.reduceByKey((v1, v2) -> v1 + v2);
		JavaRDD<EventCount> finalEventCount = eventCount.map(value -> {
			event.setEventType(value._1);
			event.setCount(BigInteger.valueOf(value._2));
			return event;
		});
		//saving event count data to cassandra
		javaFunctions(finalEventCount).writerBuilder(KEYSPACE,EVENT_COUNT_TABLE,mapToRow(EventCount.class)).saveToCassandra();
		return eventCount;

	}

	private static void calculateAvgPricePurchase(JavaSparkContext sc, JavaPairRDD<String, Integer> eventCount, JavaRDD<String> initialData)
	{
		AvgPrice avgPrice = new AvgPrice();
		//job for average price
		Float purchaseAmount = initialData.filter(row -> row.split(",")[0].equalsIgnoreCase("purchase") && !row.split(",")[6].isEmpty())
				.map(row -> Float.parseFloat(row.split(",")[6])).reduce((v1, v2) -> v1 + v2);
		float avgPriceFinal = purchaseAmount/eventCount.collectAsMap().get("purchase");
		avgPrice.setEventType("purchase");
		avgPrice.setPrice(avgPriceFinal);
		JavaRDD<AvgPrice> avgPriceJavaRDD = sc.parallelize(Collections.singletonList(avgPrice));
		//saving average price to cassandra
		javaFunctions(avgPriceJavaRDD).writerBuilder(KEYSPACE,AVG_PRICE_TABLE,mapToRow(AvgPrice.class)).saveToCassandra();
	}

	private static void brandsByCategory(String category, JavaRDD<String> initialData)
	{
		BrandByCategory brands = new BrandByCategory();
		//job for brands count by category
		JavaPairRDD<String, Integer> brandsByCategory = initialData.filter(row -> row.split(",")[3].equalsIgnoreCase(category) && !row.split(",")[3].isEmpty())
				.mapToPair(filterRow -> new Tuple2<>(filterRow.split(",")[5], 1))
				.reduceByKey((v1, v2) -> v1 + v2);
		JavaRDD<BrandByCategory> brandsMap = brandsByCategory.map(result -> {
			brands.setBrand(result._1);
			brands.setCount(BigInteger.valueOf(result._2));
			return brands;
		});
		//saving brands and their count to cassandra
		javaFunctions(brandsMap).writerBuilder(KEYSPACE,BRANDS_BY_CAT_TABLE,mapToRow(BrandByCategory.class)).saveToCassandra();
	}

	private static void brandsByCategoryAndType(String category, JavaRDD<String> initialData, String eventType)
	{
		BrandByCategoryEvent brandByCategoryEvent = new BrandByCategoryEvent();
		//job for brands by category with event filter
		JavaPairRDD<String, Integer> brandByCategoryAndType = initialData.filter(row -> row.split(",")[3].equalsIgnoreCase(category) && !row.split(",")[3].isEmpty())
				.filter(row -> row.split(",")[0].equalsIgnoreCase(eventType))
				.mapToPair(filterRow -> new Tuple2<>(filterRow.split(",")[5], 1))
				.reduceByKey((v1, v2) -> v1 + v2);
		JavaRDD<BrandByCategoryEvent> brandsMapEvent = brandByCategoryAndType.map(result -> {
			brandByCategoryEvent.setBrand(result._1);
			brandByCategoryEvent.setCount(BigInteger.valueOf(result._2));
			brandByCategoryEvent.setEventType(eventType);
			return brandByCategoryEvent;
		});
		//saving brand count to cassandra
		javaFunctions(brandsMapEvent).writerBuilder(KEYSPACE,BRANDS_BY_CAT_TABLE_EVENT,mapToRow(BrandByCategoryEvent.class)).saveToCassandra();
	}

}
