package com.techdisqus.controller;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.dto.Response;
import com.techdisqus.dto.UserPostsResponse;
import com.techdisqus.dto.validators.CreatePostRequestValidator;
import com.techdisqus.service.UserPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class UserPostResource {

    private static final String VERSION = "v1";

    @Autowired
    private UserPostService userPostService;

    @PostMapping("/createpost")
    public ResponseEntity<Response> createComment(
                                                      @RequestBody CreatePostRequest createPostRequest){
        return ResponseEntity.ok(userPostService.createPost(createPostRequest));
    }

    @GetMapping("/posts/all")
    public ResponseEntity<UserPostsResponse> getPosts(@RequestParam(value = "pageNo", required = false) Short pageNo,
                                                      @RequestParam(value = "count", required = false) Short count){
        return ResponseEntity.ok(userPostService.getAllPosts());
    }
}
