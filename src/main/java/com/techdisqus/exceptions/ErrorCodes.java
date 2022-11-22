package com.techdisqus.exceptions;

import com.techdisqus.dto.Error;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCodes {
    ERROR_VALIDATING_EMAIL("1001",
            "Unable to validate user email in target system"),
    ERROR_CREATING_USER("1002",
                                   "Unable to create user in target system"),
    ERROR_CREATING_USER_POST("1201",
            "Unable to create user post in target system"),
    ERROR_FETCHING_USER_POSTS("1202",
            "Unable to fetch user posts from target system"),

    ERROR_FETCHING_USERS("1203",
            "Unable to fetch users from target system"),

    ERROR_UNABLE_TO_CONNECT_TO_END_POINT("1301",
            "Unable to connect to target endpoint"),

    REQUEST_TIMED_OUT("1302",
            "request timed out"),

    BAD_REQUEST("1303",
            "bad request"),
    ERROR_WHILE_GETTING_COUNT("1304",
            "Error while getting the count"),

    ERROR_MISSING_EMAIL("1400","missing email"),
    ERROR_MISSING_NAME("1401","missing name"),
    ERROR_MISSING_GENDER("1402","missing gender"),
    ERROR_MISSING_STATUS("1403","missing status"),
    ERROR_INVALID_STATUS("1404","Invalid status, valid values are active and inactive"),
    ERROR_INVALID_GENDER("1405","Invalid gender, valid values are male and female"),
    ;
    private final String errorCode;
    private final Map<String,String> errorDetails = new HashMap<>();

    private final String errorDesc;
    private ErrorCodes(String errorCode,String errorDesc){
        this.errorCode = errorCode;
        this.errorDesc =errorDesc;
        errorDetails.put(errorCode,errorDesc);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }
    public String  getErrorDetails(ErrorCodes  errorCodes){
        return errorDetails.get(errorCodes.errorCode);
    }

    public Map<String, String> getErrorDetails() {
        return errorDetails;
    }
}
