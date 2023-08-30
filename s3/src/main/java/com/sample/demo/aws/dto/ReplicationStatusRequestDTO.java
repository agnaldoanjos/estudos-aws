package com.sample.demo.aws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ReplicationStatusRequestDTO {

    private String bucketName;

    private String objectName;

}
