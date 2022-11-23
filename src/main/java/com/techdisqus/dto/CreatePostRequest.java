package com.techdisqus.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Validated
public class CreatePostRequest {

    public enum Gender{
        MALE,FEMALE
    }
    private String name;

    private Gender gender;

    @Email()
    @NotEmpty
    private String email;
    @NotEmpty
    private String title;
    @NotEmpty
    private String body;


}
