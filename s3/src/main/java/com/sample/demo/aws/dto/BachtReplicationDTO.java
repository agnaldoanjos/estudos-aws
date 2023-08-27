package com.sample.demo.aws.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BachtReplicationDTO {

    private String accountId;

    private String bucketSource;

    private String bucketInventory;

}
