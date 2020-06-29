package org.thehoneycomb.imageserving.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class ComponentConfig {
    @Value("${aws-s3-endpoint}") private URI awsS3Endpoint;
    @Value("${aws-accesskey}") private String awsS3Access;
    @Value("${aws-secretkey}") private String awsS3Secret;
    @Value("${logdb-name}") private  String logdbName ;
    @Value("${logdb-endpoint}") private  String logdbEndpoint ;
    @Value("${logdb-username}") private String logdbUsername ;
    @Value("${logdb-password}") private String logdbPassword ;
    @Value("${aws-region}") private String awsRegion;
    @Value("${database.driver}") private String databaseDriver;

    @Bean
    public S3Client getS3Resource() {
        final StaticCredentialsProvider credsV2 =
                StaticCredentialsProvider.create(AwsBasicCredentials.create(awsS3Access, awsS3Secret));
        Region region = Region.of(awsRegion);
        return S3Client.builder()
                .region(region)
                .credentialsProvider(credsV2)
                .endpointOverride(awsS3Endpoint)
                .build();
    }

    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(databaseDriver);
        dataSourceBuilder.url(logdbEndpoint + logdbName);
        dataSourceBuilder.username(logdbUsername);
        dataSourceBuilder.password(logdbPassword);
        return dataSourceBuilder.build();
    }
}
