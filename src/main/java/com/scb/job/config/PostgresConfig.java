package com.scb.job.config;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@Profile("!test & !local")
public class PostgresConfig {

    private static final String DEFAULT_JDBC_DRIVER = "jdbc:postgresql://";

    @Value("${spring.datasource.database}")
    private String dbName;

    @Value("${secretsPath}")
    private String secretsPath;

    @SneakyThrows
    @Bean
    public DataSource getDataSource() {

        final URI dbUriPath = ResourceUtils.getURL(secretsPath + "/POSTGRES_CLUSTER_URL").toURI();
        final URI dbUserPath = ResourceUtils.getURL(secretsPath + "/POSTGRES_USERNAME").toURI();
        final URI dbpassPath = ResourceUtils.getURL(secretsPath + "/POSTGRES_PASSWORD").toURI();

        final String dbUri = sanitize(Files.readAllBytes(Paths.get(dbUriPath)));
        final String dbUser = sanitize(Files.readAllBytes(Paths.get(dbUserPath)));
        final String dbpass = sanitize(Files.readAllBytes(Paths.get(dbpassPath)));

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(DEFAULT_JDBC_DRIVER + dbUri +  "/" + dbName);
        dataSourceBuilder.username(dbUser);
        dataSourceBuilder.password(dbpass);
        return dataSourceBuilder.build();
    }

    @SneakyThrows
    private String sanitize(byte[] strBytes) {
        return new String(strBytes)
                .replace("\r", "")
                .replace("\n", "");
    }

}