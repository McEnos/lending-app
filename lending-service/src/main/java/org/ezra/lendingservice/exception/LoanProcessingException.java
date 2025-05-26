package org.ezra.lendingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class LoanProcessingException extends RuntimeException {
    public LoanProcessingException(String message) {
        super(message);
    }
}