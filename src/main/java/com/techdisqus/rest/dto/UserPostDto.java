package com.techdisqus.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techdisqus.dto.CreatePostRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPostDto {

    private long id;
    @JsonProperty("user_id")
    private Long userId;
    private String title;
    private String body;
    public static UserPostDto toUserPost(CreatePostRequest request){
        UserPostDto userPostDto = new UserPostDto();
        userPostDto.setBody(request.getBody());
        userPostDto.setTitle(request.getTitle());
        return userPostDto;
    }
}
