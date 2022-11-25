package com.techdisqus.service;

import com.techdisqus.config.AppConfig;
import com.techdisqus.dto.UserPostDetails;
import com.techdisqus.dto.UserPostsResponse;
import com.techdisqus.rest.dto.UserDto;
import com.techdisqus.rest.dto.UserPostDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.techdisqus.service.TestHelperUtils.getCreatePostRequest;
import static com.techdisqus.service.TestHelperUtils.getCreatePostResponse;
import static com.techdisqus.service.TestHelperUtils.getUserDto;
import static com.techdisqus.service.TestHelperUtils.getUserDtoList;
import static com.techdisqus.service.TestHelperUtils.getUserPostDtos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration( classes = AppConfig.class)
public class UserPostServiceImplTest {

    @Autowired
    private UserPostServiceImpl userPostService;

    @Test
    public void testNonNull(){
        assertNotNull(userPostService);
    }

    @Test
    public void testCreatePost(){

        UserServiceHelper userServiceHelper = Mockito.mock(UserServiceHelper.class,RETURNS_DEEP_STUBS);
        UserPostServiceHelper userPostServiceHelper = Mockito.mock(UserPostServiceHelper.class,RETURNS_DEEP_STUBS);
        userPostService.setUserPostServiceHelper(userPostServiceHelper);
        userPostService.setUserServiceHelper(userServiceHelper);
        when(userServiceHelper.getUserDetailsByMailId(anyString())).thenReturn(Optional.of(getUserDto(123)));
        when(userPostServiceHelper.createUserPost(any(),any())).thenReturn(getCreatePostResponse());
        UserPostDetails res = userPostService.createPost(getCreatePostRequest());
        assertEquals("body",res.getPostBody());
        assertEquals("title",res.getPostTitle());
        assertEquals(123,res.getUserId());
        assertEquals("MALE",res.getUserGender());
        assertEquals("Active",res.getStatus());

    }
    @Test
    public void testGetAllUserPosts(){

        UserServiceHelper userServiceHelper = Mockito.mock(UserServiceHelper.class,RETURNS_DEEP_STUBS);
        UserPostServiceHelper userPostServiceHelper = Mockito.mock(UserPostServiceHelper.class,RETURNS_DEEP_STUBS);
        userPostService.setUserPostServiceHelper(userPostServiceHelper);
        userPostService.setUserServiceHelper(userServiceHelper);
        when(userServiceHelper.getUserListFutures()).thenReturn(getUsersFutures());
        when(userPostServiceHelper.getUserPostsFutures()).thenReturn(getUserPostsFutures());
        when(userServiceHelper.getUserList(any())).thenReturn(getUserDtoList());
        when(userPostServiceHelper.getUserPostList(any())).thenReturn(getUserPostDtos());
        UserPostsResponse res = userPostService.getAllPosts();
        assertEquals(1,res.getUserPosts().size());
        assertEquals(99,res.getUsersWithoutPosts());
        assertEquals(1,res.getUsersWithPosts());
        assertEquals(0,res.getPostsWithoutUsers());
    }

    private List<Future<List<UserPostDto>>> getUserPostsFutures(){
        List<Future<List<UserPostDto>>> futureList = new ArrayList<>();
        futureList.add(new Future<List<UserPostDto>>() {
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
            public List<UserPostDto> get() throws InterruptedException, ExecutionException {
                return getUserPostDtos();
            }

            @Override
            public List<UserPostDto> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        });
        return futureList;
    }

    private List<Future<List<UserDto>>> getUsersFutures(){
        List<Future<List<UserDto>>> futureList = new ArrayList<>();
        futureList.add(new Future<List<UserDto>>() {
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
            public List<UserDto> get() throws InterruptedException, ExecutionException {
                return getUserDtoList();
            }

            @Override
            public List<UserDto> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        });
        return futureList;
    }




}
