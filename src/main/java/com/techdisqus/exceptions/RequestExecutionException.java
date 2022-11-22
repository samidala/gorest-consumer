package com.techdisqus.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class RequestExecutionException extends RuntimeException{

    private ErrorCodes errorCodes;
    public RequestExecutionException(ErrorCodes errorCodes){
        super(errorCodes.getErrorCode());
        this.errorCodes = errorCodes;
    }
    public RequestExecutionException(Exception e,ErrorCodes errorCodes){
        super(errorCodes.getErrorCode());
        this.errorCodes = errorCodes;
    }

    public ErrorCodes getErrorCodes() {
        return errorCodes;
    }
}
