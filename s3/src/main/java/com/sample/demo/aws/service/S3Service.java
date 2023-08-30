package com.sample.demo.aws.service;

import com.sample.demo.aws.dto.*;
import com.sample.demo.aws.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.datasync.DataSyncClient;
import software.amazon.awssdk.services.datasync.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3control.S3ControlClient;
import software.amazon.awssdk.services.s3control.model.*;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3ControlClient s3Control;

    private final S3Client s3Client;

    private final AwsCredentials awsCreds;

    private static final int NUM_OF_STEPS = 5;

    private static final int TIME_INTERVAL = 30000;

    public MessageResponseDTO batchPreplication(BachtReplicationRequestDTO bachtReplicationRequestDTO) {

        final String id = UUID.randomUUID().toString();

        try {

            S3JobManifestGenerator s3JobManifestGenerator = S3JobManifestGenerator.builder()
                    .filter(
                            JobManifestGeneratorFilter.builder()
                                    //.objectReplicationStatuses(ReplicationStatus.NONE)// REPLICA, NONE, UNKNOWN_TO_SDK_VERSION, FAILED
                                    .eligibleForReplication(true)
                                    //.createdBefore(Instant.now())
                                    .build()
                    )
                    .sourceBucket(bachtReplicationRequestDTO.getBucketSource())
                    .build();

            JobManifestGenerator manifestGenerator = JobManifestGenerator
                    .fromS3JobManifestGenerator(s3JobManifestGenerator);


            S3ReplicateObjectOperation s3ReplicateObjectOperation = S3ReplicateObjectOperation.builder().build();

            JobReport jobReport = JobReport.builder().enabled(Boolean.FALSE).build();

            CreateJobRequest createJobRequest = CreateJobRequest.builder()
                    .accountId(bachtReplicationRequestDTO.getAccountId())
                    .operation(JobOperation.builder().s3ReplicateObject(s3ReplicateObjectOperation).build())
                    .manifestGenerator(manifestGenerator)
                    .clientRequestToken(UUID.randomUUID().toString())
                    .confirmationRequired(Boolean.FALSE)
                    .description("Exemplo de sincronismo de replicacao")
                    .report(jobReport)
                    .priority(44)
                    .roleArn(bachtReplicationRequestDTO.getJobRole()) // Role com as permissões necessárias
                    .build();

            CreateJobResponse createJobResponse = s3Control.createJob(createJobRequest);

            String status = waitForJobCompletion(bachtReplicationRequestDTO.getAccountId(), createJobResponse.jobId());

            return MessageResponseDTO.builder()
                    .id(id)
                    .status("success")
                    .message("Job created")
                    .data(S3ReplicaJobDTO.builder()
                            .jobId(createJobResponse.jobId())
                            .jobExecutionStatus(status)
                            .build())
                    .build();
        } catch (Exception ex) {
            return MessageResponseDTO.builder()
                    .id(id)
                    .status("fail")
                    .message(ex.getMessage())
                    .build();
        }
    }

    public String waitForJobCompletion(final String accountId, final String jobId) {
        DescribeJobRequest describeRequest;
        DescribeJobResponse describeResponse;
        JobStatus status;

        int offset = 0;

        do {
            Utils.safeSleep(TIME_INTERVAL);

            // Consulte o status atual do trabalho

            describeRequest = DescribeJobRequest.builder()
                    .accountId(accountId)
                    .jobId(jobId)
                    .build();
            describeResponse = s3Control.describeJob(describeRequest);

            status = describeResponse.job().status();

            offset++;

            System.out.println("Current job status: " + status);
        } while ((status == JobStatus.PREPARING || status == JobStatus.ACTIVE) && (offset < NUM_OF_STEPS));


        if (status == JobStatus.FAILED) {
            describeResponse.job().failureReasons().stream().forEach(j -> {
            });

        }


        // Depois que sairmos do loop, o trabalho estará em um estado finalizado (completo, falho, cancelado, etc.)
        return status.toString();
    }

    public MessageResponseDTO replicationStatus(final ReplicationStatusRequestDTO replicationStatusRequestDTO) {

        final String id = UUID.randomUUID().toString();

        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(replicationStatusRequestDTO.getBucketName())
                .key(replicationStatusRequestDTO.getObjectName())
                .build();

        HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);

        return MessageResponseDTO.builder()
                .id(id)
                .status("success")
                .data(headObjectResponse.responseMetadata().toString())
                .build();
    }

    public MessageResponseDTO dataSync(DataSyncRequestDTO dataSyncRequestDTO) {

        final String id = UUID.randomUUID().toString();

        DataSyncClient dataSyncClientSource = DataSyncClient.builder()
                .region(Region.of(dataSyncRequestDTO.getSourceRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        DataSyncClient dataSyncClientTarget = DataSyncClient.builder()
                .region(Region.of(dataSyncRequestDTO.getTargetRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        // Criar local de origem para o bucket S3
        CreateLocationS3Request createSourceLocationRequest = CreateLocationS3Request.builder()
                .s3BucketArn(dataSyncRequestDTO.getSourceBucketName())
                .s3Config(S3Config.builder().bucketAccessRoleArn(dataSyncRequestDTO.getRole()).build())
                .build();

        CreateLocationS3Response createSourceLocationResponse = dataSyncClientSource.createLocationS3(createSourceLocationRequest);
        String sourceLocationArn = createSourceLocationResponse.locationArn();

        // Criar local de destino para o bucket S3
        CreateLocationS3Request createDestinationLocationRequest = CreateLocationS3Request.builder()
                .s3BucketArn(dataSyncRequestDTO.getTargetBucketName())
                .s3Config(S3Config.builder().bucketAccessRoleArn(dataSyncRequestDTO.getRole()).build())
                .build();

        CreateLocationS3Response createDestinationLocationResponse = dataSyncClientTarget.createLocationS3(createDestinationLocationRequest);
        String destinationLocationArn = createDestinationLocationResponse.locationArn();

        // Criar tarefa de DataSync
        CreateTaskRequest createTaskRequest = CreateTaskRequest.builder()
                .sourceLocationArn(sourceLocationArn)
                .destinationLocationArn(destinationLocationArn)
                .build();

        CreateTaskResponse createTaskResponse = dataSyncClientSource.createTask(createTaskRequest);
        String taskArn = createTaskResponse.taskArn();
        System.out.println("Created DataSync Task with ARN: " + taskArn);

        // Iniciar a tarefa
        StartTaskExecutionRequest startTaskExecutionRequest = StartTaskExecutionRequest.builder()
                .taskArn(taskArn)
                .build();

        StartTaskExecutionResponse startTaskExecutionResponse = dataSyncClientSource
                .startTaskExecution(startTaskExecutionRequest);


        return MessageResponseDTO.builder()
                .id(id)
                .status("success")
                .message("Created DataSync Task")
                .data(startTaskExecutionResponse.taskExecutionArn())
                .build();
    }
}
