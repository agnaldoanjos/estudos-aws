package com.sample.demo.aws.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3control.S3ControlClient;
import software.amazon.awssdk.services.s3control.model.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/was/s3")
@RequiredArgsConstructor
public class ClientAWSController {

    private final S3ControlClient s3Control;

    @Value("${s3.account.id}")
    private final String accountId;

    @Value("${s3.bucket.source}")
    private final String bucketSource;

    @Value("${s3.bucket.inventory}")
    private final String bucketInventory;

    @PostMapping("/sync")
    public ResponseEntity<String> replication() {

        S3JobManifestGenerator s3JobManifestGenerator = S3JobManifestGenerator.builder()
                .filter(
                        JobManifestGeneratorFilter.builder()
                                .objectReplicationStatuses(ReplicationStatus.COMPLETED)// REPLICA, NONE, UNKNOWN_TO_SDK_VERSION, FAILED
                                .eligibleForReplication(true)
                                .createdBefore(Instant.now())
                                .build()
                )
                .sourceBucket(bucketSource)
                .build();

        JobManifestGenerator manifestGenerator = JobManifestGenerator
                .fromS3JobManifestGenerator(s3JobManifestGenerator);


        S3ReplicateObjectOperation s3ReplicateObjectOperation = S3ReplicateObjectOperation.builder().build();

        JobReport jobReport = JobReport.builder()
                .bucket(bucketInventory)
                .prefix("reports")
                .format("Report_CSV_20180820")
                .enabled(true)
                .reportScope("AllTasks") //AllTasks | FailedTasksOnly
                .build();


        CreateJobRequest createJobRequest = CreateJobRequest.builder()
                .accountId(accountId)
                .operation(JobOperation.builder().s3ReplicateObject(s3ReplicateObjectOperation).build())
                .manifestGenerator(manifestGenerator)
                .clientRequestToken(UUID.randomUUID().toString())
                .confirmationRequired(Boolean.FALSE)
                .description("Exemplo de sincronismo de replicacao")
                .report(jobReport)
                .priority(44)
                .roleArn("arn:aws:iam::374834910945:role/service-role/s3crr_role_for_agnaldo.anjos-dados-01") // Role com as permissões necessárias
                .build();

        //S3ControlClient s3Control = S3ControlClient.builder().build();

        CreateJobResponse createJobResponse = s3Control.createJob(createJobRequest);

        System.out.println("Job ID: " + createJobResponse.jobId());

        waitForJobCompletion(createJobResponse.jobId());

        return ResponseEntity.ok("Sucess - Job ID: " + createJobResponse.jobId());
    }

    private void waitForJobCompletion(String jobId) {
        DescribeJobRequest describeRequest;
        DescribeJobResponse describeResponse;
        JobStatus status;

        // Aguarde o trabalho ser concluído
        do {
            // Aguarde um período antes de consultar novamente para não sobrecarregar o serviço
            try {
                Thread.sleep(60000); // Espera 60 segundos
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Consulte o status atual do trabalho
            describeRequest = DescribeJobRequest.builder()
                    .accountId(accountId)
                    .jobId(jobId)
                    .build();
            describeResponse = s3Control.describeJob(describeRequest);
            status = describeResponse.job().status();

            System.out.println("Current job status: " + status);
        } while (status == JobStatus.PREPARING || status == JobStatus.ACTIVE);

        // Depois que sairmos do loop, o trabalho estará em um estado finalizado (completo, falho, cancelado, etc.)
        System.out.println("Job finished with status: " + status);
    }

}
