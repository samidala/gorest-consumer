package com.techdisqus.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidInputException extends RuntimeException{
    private final Set<ErrorCodes> errorCodes;
    public InvalidInputException(Set<ErrorCodes> errorCodes){
        super("Invalid input");
        this.errorCodes = errorCodes;
    }

    public Set<ErrorCodes> getErrorCodes() {
        return errorCodes;
    }
}
