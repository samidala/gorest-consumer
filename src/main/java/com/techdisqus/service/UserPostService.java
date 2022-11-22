package com.techdisqus.service;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.dto.Response;
import com.techdisqus.dto.UserPostsResponse;

import java.util.Map;
import java.util.Set;

public interface UserPostService {

    /**
     * Creates user posts
     * Validates if email is already registered with target system, if not API will try to register before posting
     * the post.
     * @param request
     * @return
     */
    Response createPost(CreatePostRequest request);

    /**
     * fetches all the posts from target system
     * @return
     */
    UserPostsResponse getAllPosts();
}
