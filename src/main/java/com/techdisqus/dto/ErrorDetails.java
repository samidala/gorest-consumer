package com.techdisqus.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {
    private Map<String,String> errorCodes;
    private String errorCode;
    private Collection<String> errors;
}
