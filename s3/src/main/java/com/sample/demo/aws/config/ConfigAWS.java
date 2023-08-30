package com.sample.demo.aws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3control.S3ControlClient;

@Configuration
public class ConfigAWS {

    @Bean
    public S3ControlClient s3ControlClient() {
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();

        return S3ControlClient.builder()
                .credentialsProvider(credentialsProvider)
                .build();
    }
    @Bean
    public S3Client s3Client() {
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();

        return S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public AwsCredentials awsCredentials() {
        return AwsSessionCredentials.create(
                System.getenv("AWS_ACCESS_KEY_ID"),
                System.getenv("AWS_SECRET_ACCESS_KEY"),
                System.getenv("AWS_SESSION_TOKEN")
        );
    }
}
