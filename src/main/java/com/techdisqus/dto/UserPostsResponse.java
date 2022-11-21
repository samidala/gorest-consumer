package com.techdisqus.dto;

import lombok.*;

import java.util.Map;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserPostsResponse {

    private Map<Long, Set<Response>> userPosts;
    private int usersWithoutPosts;
    private int postsWithoutUsers;
    private int usersWithPosts;

   /* public void incrementUserWithoutPosts(){
        usersWithoutPosts++;
    }*/
    public void incrementPostsWithoutUsers(){
        postsWithoutUsers++;
    }
    public void incrementUsersWithPosts(){
        usersWithPosts++;
    }
}
