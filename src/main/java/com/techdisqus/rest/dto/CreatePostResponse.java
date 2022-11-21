package com.techdisqus.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.techdisqus.dto.Response;
import lombok.*;

//@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
//@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePostResponse {
    //@JsonProperty("id")
    private long id;
    @JsonProperty("user_id")
    private long userId;
    private String title;
    private String body;

    public Response get(){
        return Response
                .builder().postBody(body).userId(userId).postTitle(title).build();
    }
}
