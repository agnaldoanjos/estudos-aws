package com.sample.demo.aws.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class BachtReplicationRequestDTO {

    private String accountId;

    private String bucketSource;

    private String bucketInventory;

    private String jobRole;

}
