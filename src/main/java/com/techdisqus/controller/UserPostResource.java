package com.techdisqus.controller;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.dto.UserPostDetails;
import com.techdisqus.dto.UserPostsResponse;
import com.techdisqus.service.UserPostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
public class UserPostResource {

    private static final String VERSION = "v1";

    @Autowired
    private UserPostService userPostService;

    @ApiOperation(value = "Create a new user post", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created a user post"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request"),
            @ApiResponse(code = 422, message = "request is not processed by target system"),
            @ApiResponse(code = 400, message = "Invalid input")
    })
    @PostMapping(value = "/createpost",consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UserPostDetails> createUserPost(@Valid @RequestBody  CreatePostRequest createPostRequest){
        return ResponseEntity.ok(userPostService.createPost(createPostRequest));
    }

    @ApiOperation(value = "Fetches user posts from target system", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns all the user posts and stats"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request"),
            @ApiResponse(code = 422, message = "Request execution failed in target system")
    })
    @GetMapping(value = "/posts/all", consumes = {MediaType.ALL_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UserPostsResponse> getPosts(@RequestParam(value = "pageNo", required = false) Short pageNo,
                                                      @RequestParam(value = "count", required = false) Short count){
        return ResponseEntity.ok(userPostService.getAllPosts());
    }
}
