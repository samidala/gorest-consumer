package com.techdisqus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
public class UserPostDetails implements Comparable<UserPostDetails>{
    private long userId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private long postId;
    private String postTitle;
    private String postBody;
    private String userName;
    private String userEmail;
    private String userGender;
    private String status;


    @Override
    public int compareTo(UserPostDetails that) {
        return (int) (this.getPostId() - that.getPostId());
    }
}
