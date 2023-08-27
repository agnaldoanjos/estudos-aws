package com.sample.demo.aws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
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
}
