package com.techdisqus.rest.dto;

import com.techdisqus.dto.CreatePostRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;


@Getter
@Setter
public class CreateUserDto {

    @NotNull
    private String name;
    @NotNull
    private CreatePostRequest.Gender gender;
    @NotNull
    private String email;
    @NotNull
    private String status;

    public static  CreateUserDto createUserDto(CreatePostRequest request){
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.email = request.getEmail();
        createUserDto.gender = request.getGender();
        createUserDto.name = request.getName();
        createUserDto.status = "active";
        return createUserDto;
    }
}
