package com.techdisqus.rest.dto;

import com.techdisqus.dto.CreatePostRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPostDto {
    private String title;
    private String body;

    public static UserPostDto userPostDto(CreatePostRequest request){
        UserPostDto userPostDto = new UserPostDto();
        userPostDto.setBody(request.getBody());
        userPostDto.setTitle(request.getTitle());
        return userPostDto;
    }
}
