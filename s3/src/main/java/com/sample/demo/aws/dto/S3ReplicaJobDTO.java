package com.sample.demo.aws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class S3ReplicaJobDTO extends ResponseDataDTO {
    private String jobId;
    private String jobExecutionStatus;
}
