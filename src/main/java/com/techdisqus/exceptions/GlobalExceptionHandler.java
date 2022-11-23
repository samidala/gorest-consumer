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

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.techdisqus.exceptions.ErrorCodes.ERROR_INVALID_GENDER;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler  {

    private final static Map<String,String> errorCodesInfo = new TreeMap<>();
    private static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintFailureException(
            ConstraintViolationException ex, WebRequest request) {

        StringBuilder sb = new StringBuilder();
        ex.getConstraintViolations().forEach(con -> sb.append(con.getMessage()));
        log.error("error details, constraints failed {} ",sb,ex);
        return new ResponseEntity<>(
                ErrorDetails.builder()
                        .error(ex.getConstraintViolations().toString())
                        .errorCodes(errorCodesInfo)
                        .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAllOtherExceptions(
            Exception ex, WebRequest request) {

        log.error("Error while execution ",ex);

        return response(ErrorCodes.INTERNAL_SERVER_ERROR,HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler({RequestExecutionException.class})
    public ResponseEntity<Object> handleRequestExecFailedException(
            RequestExecutionException ex, WebRequest request) {
        log.error("error details ",ex);
        return response(ex.getErrorCodes(),HttpStatus.UNPROCESSABLE_ENTITY);

    }
    @ExceptionHandler({InvalidInputException.class})
    public ResponseEntity<Object> handleInvalidInput(
            InvalidInputException ex, WebRequest request) {
        log.error("invalid user inputs, constraints failed ",ex);
        return response(ex.getErrorCodes(),HttpStatus.BAD_REQUEST);
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
        return response(ERROR_INVALID_GENDER,HttpStatus.BAD_REQUEST);
    }

    private String getError(ErrorCodes errorInvalidGender) {
        return errorInvalidGender.getErrorCode() + "-" + errorInvalidGender.getErrorDesc();
    }
    @PostConstruct
    public void init(){
        errorCodesInfo.putAll(buildErrorMessages());
    }

    /**
     * builds response with error with single error
     * @param errorCodes
     * @param status
     * @return
     */
    private ResponseEntity<Object> response(ErrorCodes errorCodes, HttpStatus status){
        return new ResponseEntity<>(
                ErrorDetails.builder()
                        .error(getError(errorCodes))
                        .errorCodes(errorCodesInfo)
                        .errorCode(errorCodes.getErrorCode())
                        .build(), status);
    }

    /**
     * builds response with error with multiple validation error
     * @param errors
     * @param status
     * @return
     */
    private ResponseEntity<Object> response(Set<ErrorCodes> errors, HttpStatus status){
        return new ResponseEntity<>(
                ErrorDetails.builder()
                        .errors(errors.stream().map(this::getError)
                                .collect(Collectors.toSet()))
                        .errorCodes(errorCodesInfo)
                        .build(), status);
    }


    private Map<String,String> buildErrorMessages(){
        return Arrays.stream(ErrorCodes.values()).collect(Collectors.toMap(ErrorCodes::getErrorCode, ErrorCodes::getErrorDesc));
    }
}
