package com.techdisqus.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techdisqus.dto.Response;
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



    public Response get(){
        return Response
                .builder().postBody(body).userId(userId).postTitle(title).build();
    }
}
