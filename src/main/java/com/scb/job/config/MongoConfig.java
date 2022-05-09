package com.scb.job.config;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.util.ResourceUtils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@Profile(value = "!test")
public class MongoConfig extends AbstractMongoClientConfiguration {

  @Value("${mongo.dbName}")
  private String dbName;

  @Value("${secretsPath}")
  private String secretsPath;

  @Override
  protected String getDatabaseName() {
    return dbName;
  }

  @SneakyThrows
  @Override
  public MongoClient mongoClient() {
    /*final URI mongoUriPath = ResourceUtils.getURL(secretsPath + "/MONGO_CLUSTER_URL").toURI();
    final URI mongoUserPath = ResourceUtils.getURL(secretsPath + "/MONGO_USERNAME").toURI();
    final URI mongopassPath = ResourceUtils.getURL(secretsPath + "/MONGO_PASSWORD").toURI();

    final String mongoUri = sanitize(Files.readAllBytes(Paths.get(mongoUriPath)));
    final String mongoUser = sanitize(Files.readAllBytes(Paths.get(mongoUserPath)));
    final String mongopass = sanitize(Files.readAllBytes(Paths.get(mongopassPath)));

    String url = "mongodb://"+mongoUser + ":" + mongopass + "@" + mongoUri;*/
    String url = "mongodb://localhost:27017";
    ConnectionString connectionString = new ConnectionString(url);

    MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(connectionString).readConcern(ReadConcern.MAJORITY).readPreference(ReadPreference.primary())
        .build();
    log.info("Connecting to MongoDB");
    return MongoClients.create(mongoClientSettings);
  }

  @Override
  public Collection<String> getMappingBasePackages() {
    return Collections.singleton("com.scb.job");
  }

  private String sanitize(byte[] strBytes) {
    return new String(strBytes)
        .replace("\r", "")
        .replace("\n", "");
  }

  @Override
  public boolean autoIndexCreation() {
    return true;
  }

}
