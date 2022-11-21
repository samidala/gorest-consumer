package com.techdisqus.rest.dto;

import com.techdisqus.dto.CreatePostRequest;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CreateUserDto {

    private String name;
    private CreatePostRequest.Gender gender;
    private String email;
    private String status;

    public static CreateUserDto createUserDto(CreatePostRequest request){
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.email = request.getEmail();
        createUserDto.gender = request.getGender();
        createUserDto.name = request.getName();
        createUserDto.status = "active";
        return createUserDto;
    }
}
