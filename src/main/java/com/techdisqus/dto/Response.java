package com.techdisqus.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
public class Response implements Comparable<Response>{
    private long userId;
    //@JsonIgnore
    private long postId;
    private String postTitle;
    private String postBody;
    private String userName;
    private String userEmail;
    private String userGender;
    private String status;


    @Override
    public int compareTo(Response that) {
        return (int) (this.getPostId() - that.getPostId());
    }
}
