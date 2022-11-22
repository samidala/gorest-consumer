package com.techdisqus.exceptions;


import com.techdisqus.dto.ErrorDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintFailureException(
            ConstraintViolationException ex, WebRequest request) {

        StringBuilder sb = new StringBuilder();
        ex.getConstraintViolations().forEach(
                con -> sb.append(con.getMessage())
        );
        log.error("constraints failed {} ",sb,ex);

        return new ResponseEntity<>(
                ErrorDetails.builder()
                        .error(ex.getConstraintViolations().toString())
                        .errorCodes(buildErrorMessages())
                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAllOtherExceptions(
            Exception ex, WebRequest request) {

        log.error("constraints failed ",ex);

        return new ResponseEntity<>(
                ErrorDetails.builder()
                        .error(ex.getMessage())
                        .errorCodes(buildErrorMessages())
                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({RequestExecutionException.class})
    public ResponseEntity<Object> handleRequestExecFailedException(
            RequestExecutionException ex, WebRequest request) {
        log.error("constraints failed ",ex);


        return createErrorResponse(ex);
    }
    @ExceptionHandler({InvalidInputException.class})
    public ResponseEntity<Object> handleInvalidInput(
            InvalidInputException ex, WebRequest request) {
        log.error("constraints failed ",ex);


        return new ResponseEntity<>(
                ErrorDetails.builder()
                        .errors(ex.getErrorCodes().stream().map(e -> e.getErrorCode() + "-"+e.getErrorDesc())
                                .collect(Collectors.toSet()))
                        .errorCodes(buildErrorMessages())
                        .build(), HttpStatus.BAD_REQUEST);
    }


    private ResponseEntity<Object> createErrorResponse(RequestExecutionException ex) {
        return new ResponseEntity<>(
                ErrorDetails.builder()
                        .error(ex.getErrorCodes().getErrorCode())
                        .errorCodes(buildErrorMessages())
                        .build(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private Map<String,String> buildErrorMessages(){
        return Arrays.stream(ErrorCodes.values()).collect(Collectors.toMap(ErrorCodes::getErrorCode, ErrorCodes::getErrorDesc));
    }
}
