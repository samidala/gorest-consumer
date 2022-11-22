package com.techdisqus.exceptions;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.techdisqus.dto.ErrorDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice

public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
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
    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
                                                               HttpHeaders headers, HttpStatus status, WebRequest request) {
        String errors = "Unacceptable JSON " + exception.getMessage();

        if (exception.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ifx = (InvalidFormatException) exception.getCause();
            if (ifx.getTargetType()!=null && ifx.getTargetType().isEnum()) {
                errors = String.format("Invalid enum value: '%s' for the field: '%s'. The value must be one of: %s.",
                        ifx.getValue(), ifx.getPath().get(ifx.getPath().size()-1).getFieldName(), Arrays.toString(ifx.getTargetType().getEnumConstants()));
            }
        }
        log.error("error details {}",errors);
        return handleExceptionInternal(exception, ErrorDetails.builder()
                .error(ErrorCodes.ERROR_INVALID_GENDER.getErrorCode()).errorCodes(buildErrorMessages()).build(), headers, HttpStatus.BAD_REQUEST, request);
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
