package com.techdisqus.controller;

import com.techdisqus.dto.CreatePostRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentsService {

    public ResponseEntity<Object> createComment(@Validated @RequestBody CreatePostRequest createPostRequest){

    }
}
