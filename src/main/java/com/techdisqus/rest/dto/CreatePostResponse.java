package com.techdisqus.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostDto {
    private long id;
    private String userId;
    private String title;
    private String body;
}
