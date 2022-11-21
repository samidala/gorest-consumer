package com.techdisqus.dto;

import lombok.Data;

@Data
public class Result<T> {
    private T data;
    private Error error;
}
