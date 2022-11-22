package com.techdisqus.dto;

import lombok.*;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserPostsResponse {

    private Map<Long, Set<Response>> userPosts;
    private int usersWithoutPosts;
    private int postsWithoutUsers;
    private int usersWithPosts;

    public void incrementPostsWithoutUsers(){
        postsWithoutUsers++;
    }
}
