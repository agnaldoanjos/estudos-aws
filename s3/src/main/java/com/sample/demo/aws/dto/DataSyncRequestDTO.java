package com.sample.demo.aws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class DataSyncRequestDTO {

    private String account;

    private String role;

    private String sourceBucketName;

    private String sourceRegion;

    private String targetBucketName;

    private String targetRegion;
}
