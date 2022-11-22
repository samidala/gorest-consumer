package com.techdisqus.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techdisqus.dto.UserPostDetails;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class CreatePostResponse {
    private Long id;
    @JsonProperty("user_id")
    private long userId;
    private String title;
    private String body;



    public UserPostDetails get(){
        return UserPostDetails
                .builder().postBody(body).userId(userId).postTitle(title).build();
    }
}
