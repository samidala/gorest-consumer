package com.techdisqus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder(toBuilder = true)
@AllArgsConstructor
public class CreatePostResponse {
    private long userId;
    private String postTitle;
    private String postBody;
    private String userName;
    private String userEmail;
    private String userGender;
    private String status;


}
