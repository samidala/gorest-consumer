package com.techdisqus.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPost {

    private long id;
    @JsonProperty("user_id")
    private Long userId;
    private String title;
    private String body;
}
