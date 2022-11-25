package com.techdisqus.service;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.rest.dto.CreatePostResponse;
import com.techdisqus.rest.dto.UserDto;
import com.techdisqus.rest.dto.UserPostDto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestHelperUtils {

    public static CreatePostRequest getCreatePostRequest() {
        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setBody("body");
        createPostRequest.setTitle("title");
        createPostRequest.setGender(CreatePostRequest.Gender.MALE);
        createPostRequest.setEmail("me@me.com");
        createPostRequest.setName("name");
        return createPostRequest;
    }

    public static UserDto getUserDto() {
        UserDto userDto = new UserDto();
        userDto.setEmail("me@me.com");
        userDto.setId(123L);
        return userDto;
    }

    public static UserDto getUserDto(int i) {
        UserDto userDto = new UserDto();
        userDto.setId((long) i);
        userDto.setEmail("me@me.com");
        userDto.setGender("MALE");
        userDto.setStatus("Active");
        return userDto;
    }


    public static  List<UserDto> getUserDtoList(){
        List<UserDto> userDtos = new ArrayList<>(200);
        for(int i = 1; i <= 100; i++){
            userDtos.add(getUserDto(i));
        }
        return userDtos;
    }

    public static  List<UserPostDto> getUserPostDtos() {
        List<UserPostDto> userPostDtos = new ArrayList<>();
        for(int i = 1; i <=200;i++){
            userPostDtos.add(getUserPostDto(i));
        }
        return userPostDtos;
    }
    public static  UserPostDto getUserPostDto(int i) {
        UserPostDto userPostDto = new UserPostDto();
        userPostDto.setUserId(100l);
        userPostDto.setTitle("some title");
        userPostDto.setBody("some body");
        userPostDto.setId(i);
        return userPostDto;
    }

    public static  CreatePostResponse getCreatePostResponse() {
        CreatePostResponse createPostResponse = new CreatePostResponse();
        createPostResponse.setUserId(1);
        createPostResponse.setBody("body");
        createPostResponse.setTitle("title");
        return createPostResponse;
    }


    public static <T> Future<List<T>> getFuture(List<T> userDtoList) {
        return new Future<List<T>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public List<T> get() throws InterruptedException, ExecutionException {
                return userDtoList;
            }

            @Override
            public List<T> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };
    }

}
