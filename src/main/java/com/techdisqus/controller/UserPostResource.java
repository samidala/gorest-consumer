package com.techdisqus.controller;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.dto.Response;
import com.techdisqus.dto.UserPostsResponse;
import com.techdisqus.service.UserPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("")
public class UserPostResource {

    @Autowired
    private UserPostService userPostService;

    @PostMapping("/v1/createpost")
    public ResponseEntity<Response> createComment(@Validated(value = CreatePostRequest.class) @RequestBody CreatePostRequest createPostRequest){
        return ResponseEntity.ok(userPostService.createPost(createPostRequest));
    }

    @GetMapping("/v1/posts/all")
    public ResponseEntity<UserPostsResponse> getPosts(@Validated(value = CreatePostRequest.class) @RequestBody CreatePostRequest createPostRequest){
        return ResponseEntity.ok(userPostService.getAllPosts());
    }
}
