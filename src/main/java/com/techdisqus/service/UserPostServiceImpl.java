package com.techdisqus.service;

import com.techdisqus.dto.CreatePostRequest;

public interface UserPostService {

    CreatePostRequest createPost(CreatePostRequest request);
}
