package com.sample.demo.aws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MessageResponseDTO {

    private final String id;
    private final String status;
    private final String message;
    private final Object data;

}
