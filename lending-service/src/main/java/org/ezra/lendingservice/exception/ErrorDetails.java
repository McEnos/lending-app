package org.ezra.lendingservice.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorDetails {
    private String message;
    private LocalDateTime timestamp;
    private ErrorType errorType;
}
