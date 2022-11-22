package com.techdisqus.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techdisqus.dto.CreatePostRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private long id;
    private String email;

    private String name;
    private String gender;
    private String status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return Objects.equals(email, userDto.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    public static UserDto toUser(CreatePostRequest request){
        UserDto userDto = new UserDto();
        userDto.email = request.getEmail();
        userDto.gender = request.getGender().name();
        userDto.name = request.getName();
        userDto.status = "active";
        return userDto;
    }
}
