package com.dbproject.ecommerceanalysis;

//import com.datastax.driver.core.Session;
//import com.datastax.oss.driver.api.core.session.Session;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.datastax.oss.driver.api.querybuilder.schema.Drop;

import java.net.InetSocketAddress;
import java.time.Duration;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.*;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable;

public class CassandraSchema {

    //Constants for keyspace and tables
    private static final String KEYSPACE = "ecommerce";
    private static final String EVENT_COUNT_TABLE= "event_count";
    private static final String AVG_PRICE_TABLE= "avg_price";
    private static final String BRANDS_BY_CAT_TABLE= "brand_by_category";
    private static final String BRANDS_BY_CAT_TABLE_EVENT= "brand_by_category_event";

    public void createTablesNew(String ipAddress)
    {
        //Config for cassandra database for read and connection timeouts
        DriverConfigLoader loader =
                DriverConfigLoader.programmaticBuilder()
                        .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(10))
                        .withDuration(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT, Duration.ofSeconds(10))
                        .withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(10))
                        .build();

        //session builder for the database using the config
        try(CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(ipAddress,9042))
                .withLocalDatacenter("datacenter1")
                .withConfigLoader(loader)
                .build())
        {
            //Drop the output tables id exists while running the new job
            Drop eventCountTableDrop = dropTable(KEYSPACE, EVENT_COUNT_TABLE).ifExists();
            Drop avgPriceTableDrop = dropTable(KEYSPACE, AVG_PRICE_TABLE).ifExists();
            Drop barndsByCategoryTableDrop = dropTable(KEYSPACE, BRANDS_BY_CAT_TABLE).ifExists();
            Drop barndsByCategoryEventTableDrop = dropTable(KEYSPACE, BRANDS_BY_CAT_TABLE_EVENT).ifExists();

            //create tables for the output to be saved
            CreateTable createEventCountTable = createTable(KEYSPACE,EVENT_COUNT_TABLE)
                    .withPartitionKey("event_type", DataTypes.TEXT)
                    .withColumn("count", DataTypes.BIGINT);

            CreateTable createAvgPriceTableCount = createTable(KEYSPACE, AVG_PRICE_TABLE)
                    .withPartitionKey("event_type", DataTypes.TEXT)
                    .withColumn("price", DataTypes.FLOAT);

            CreateTable createBrandsByCategoryTable = createTable(KEYSPACE, BRANDS_BY_CAT_TABLE)
                    .withPartitionKey("brand", DataTypes.TEXT)
                    .withColumn("count", DataTypes.BIGINT);

            CreateTable createBrandsByCategoryTableAndEvent = createTable(KEYSPACE, BRANDS_BY_CAT_TABLE_EVENT)
                    .withPartitionKey("brand", DataTypes.TEXT)
                    .withColumn("count", DataTypes.BIGINT)
                    .withColumn("event_type", DataTypes.TEXT);

            //execute the session for dropping the tables
            session.execute(eventCountTableDrop.build());
            session.execute(avgPriceTableDrop.build());
            session.execute(barndsByCategoryTableDrop.build());
            session.execute(barndsByCategoryEventTableDrop.build());

            //execute the session for creatinf the tables
            session.execute(createEventCountTable.build());
            System.out.println("Table 1 created");
            session.execute(createAvgPriceTableCount.build());
            System.out.println("Table 2 created");
            session.execute(createBrandsByCategoryTable.build());
            System.out.println("Table 3 created");
            session.execute(createBrandsByCategoryTableAndEvent.build());
            System.out.println("Table 4 created");

        }
    }

    //This is the code for the old versions for scala and spark cassandra connector
//    public void createTables(JavaSparkContext sc)
//    {
//        try(Session session= cassandraConnector.openSession()) {
//            session.execute()
//            session.execute("drop table if exists ecommerce.event_count");
//            session.execute("drop table if exists ecommerce.avg_price");
//            session.execute("drop table if exists ecommerce.brand_by_category");
//            session.execute("create table ecommerce.event_count(event_type text, count bigint, primary key(event_type))");
//            session.execute("create table ecommerce.avg_price(event_type text,price float, primary key(event_type))");
//            session.execute("create table ecommerce.brand_by_category(brand text, count bigint, primary key(brand))");
//        }
//    }
}
