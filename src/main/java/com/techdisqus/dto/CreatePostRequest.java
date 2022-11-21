package com.techdisqus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class CreatePostRequest {

    public enum Gender{
        MALE,FEMALE,OTHERS
    }
    private String name;

    private Gender gender;

    @Email()
    private String email;
    @NotNull()
    private String title;
    @NotNull()
    private String body;

}
