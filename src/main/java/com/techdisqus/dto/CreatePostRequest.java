package com.techdisqus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
public class CreatePostRequest {

    public enum Gender{
        MALE,FEMALE
    }
    private String name;

    private Gender gender;

    @Email()
    private String email;
    @NotNull()
    private String title;
    @NotNull()
    private String body;

    public static void main(String[] args) {
        List<String> list = Arrays.asList("Java", "Java-8", "Java Streams", "Concurrency");
        System.out.println(list.stream()
                .collect(Collectors.joining("\n")));
    }

}
