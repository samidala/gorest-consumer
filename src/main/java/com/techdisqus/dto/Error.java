package com.techdisqus.dto;

public enum Error {
    ;
    private final int errorCode;
    private final String errorDesc;

    Error(int errorCode, String errorDesc) {
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }
}
