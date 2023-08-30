package com.sample.demo.aws.controller;

import com.sample.demo.aws.dto.BachtReplicationRequestDTO;
import com.sample.demo.aws.dto.DataSyncRequestDTO;
import com.sample.demo.aws.dto.MessageResponseDTO;
import com.sample.demo.aws.dto.ReplicationStatusRequestDTO;
import com.sample.demo.aws.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/was/s3")
@RequiredArgsConstructor
public class ClientAWSController {

    private final S3Service s3Service;

    @PostMapping("/batch-replication")
    public ResponseEntity<MessageResponseDTO> s3BatchPreplication(@RequestBody BachtReplicationRequestDTO bachtReplicationRequestDTO) {

        MessageResponseDTO messageResponseDTO = s3Service.batchPreplication(bachtReplicationRequestDTO);

        return ResponseEntity.ok(messageResponseDTO);
    }

    @PostMapping("/replication-status")
    public ResponseEntity<MessageResponseDTO> replicationStatus(@RequestBody ReplicationStatusRequestDTO replicationStatusRequestDTO) {

        MessageResponseDTO messageResponseDTO = s3Service.replicationStatus(replicationStatusRequestDTO);

        return ResponseEntity.ok(messageResponseDTO);
    }

    @PostMapping("/data-sync")
    public ResponseEntity<MessageResponseDTO> replicationStatus(@RequestBody DataSyncRequestDTO dataSyncRequestDTO) {

        /*
            Sample value
            {
              "account": "374834910945",
              "role": "arn:aws:iam::374834910945:role/sample-role-batch-s3",
              "sourceBucketName": "arn:aws:s3:::agnaldo.anjos-dados-02",
              "sourceRegion": "us-east-1",
              "targetBucketName": "arn:aws:s3:::agnaldo.anjos-dados-03",
              "targetRegion": "us-east-2"
            }

            Configuring AWS DataSync transfers with Amazon S3
            https://docs.aws.amazon.com/datasync/latest/userguide/create-s3-location.html#create-s3-location-s3-requests

            Creating an AWS DataSync task with the AWS CLI
            https://docs.aws.amazon.com/datasync/latest/userguide/create-task-cli.html

            Create a source location for AWS DataSync
            https://docs.aws.amazon.com/datasync/latest/userguide/configure-source-location.html

            Provide temporary credentials to the AWS SDK for Java
            https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html

         */

        MessageResponseDTO messageResponseDTO = s3Service.dataSync(dataSyncRequestDTO);

        return ResponseEntity.ok(messageResponseDTO);
    }

}
