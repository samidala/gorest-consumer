package com.techdisqus.service;

import com.techdisqus.config.AppConfig;
import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.exceptions.RequestExecutionException;
import com.techdisqus.rest.dto.CreatePostResponse;
import com.techdisqus.rest.dto.UserDto;
import com.techdisqus.rest.dto.UserPostDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration( classes = AppConfig.class)
public class UserPostServiceHelperTest {

    @Autowired
    @InjectMocks
    private UserPostServiceHelper userPostServiceHelper;

    @Mock
    private RestHelperUtils restHelperUtils;

    @Mock
    private Client client;

    @Test
    public void testUserPostServiceHelperNotNull(){
        assertNotNull(userPostServiceHelper);
    }

    @SneakyThrows
    @Test
    public void testGetUserPostsFutures(){
        RestHelperUtils restHelperUtils =  Mockito.mock(RestHelperUtils.class,RETURNS_DEEP_STUBS);
        Client client = Mockito.mock(Client.class,RETURNS_DEEP_STUBS);
        restHelperUtils.setClient(client);
        userPostServiceHelper.setRestHelperUtils(restHelperUtils);
        List<UserPostDto> userPostDtos = new ArrayList<>();
        for(int i = 1; i <=200;i++){
            userPostDtos.add(getUserPostDto(i));
        }
        when(restHelperUtils.getCount(anyString())).thenReturn(200);
        when(restHelperUtils.executeGet(anyString(),any(GenericType.class), any())).thenReturn(userPostDtos);
        List<Future<List<UserPostDto>>> futureList = userPostServiceHelper.getUserPostsFutures();
        assertEquals(2,futureList.size());
        int count = 0;
        for(Future<List<UserPostDto>> f : futureList){
            count+=f.get().size();
        }
        assertEquals(400,count);
    }

    @Test
    public void testGetUserPostList() {
        List<Future<List<UserPostDto>>> postFeatures  = new ArrayList<>();
        postFeatures.add(getFuture(getUserPostDtos()));
        postFeatures.add(getFuture(getUserPostDtos()));
        List<UserPostDto> userPostDtos = userPostServiceHelper.getUserPostList(postFeatures);
        assertEquals(400,userPostDtos.size());
    }

    @Test
    public void testGetUserPostListOnError() throws ExecutionException, InterruptedException {
        ArrayList<Future<List<UserPostDto>>> mockFuture =   Mockito.mock(ArrayList.class,RETURNS_DEEP_STUBS);
        Iterator<Future<List<UserPostDto>>> itr = Mockito.mock(Iterator.class,RETURNS_DEEP_STUBS);
        Future<List<UserPostDto>> future = Mockito.mock(Future.class,RETURNS_DEEP_STUBS);
        when(mockFuture.iterator()).thenReturn(itr);
        when(itr.hasNext()).thenReturn(true,true,false);
        when(itr.next()).thenReturn(future);
        when(future.get()).thenThrow(new ExecutionException("failed execution",new Throwable()));

        RequestExecutionException thrown = assertThrows(
                RequestExecutionException.class,
                () -> userPostServiceHelper.getUserPostList(mockFuture), "failed execution");

        assertTrue(thrown.getMessage().contains("1202"));

    }

    @Test
    public void testCreatePost(){
        CreatePostRequest createPostRequest = getCreatePostRequest();
        UserDto userDto = getUserDto();
        RestHelperUtils restHelperUtils =  Mockito.mock(RestHelperUtils.class,RETURNS_DEEP_STUBS);
        Client client = Mockito.mock(Client.class,RETURNS_DEEP_STUBS);
        restHelperUtils.setClient(client);
        userPostServiceHelper.setRestHelperUtils(restHelperUtils);
        CreatePostResponse createPostResponse = getCreatePostResponse();
        createPostResponse.setId(1l);
        when(restHelperUtils.executePost(anyString(),any(),any(Class.class),any(),any())).thenReturn(createPostResponse);
        CreatePostResponse res = userPostServiceHelper.createUserPost(createPostRequest,userDto);
        assertEquals(1,res.getUserId());
        assertEquals(1,res.getId());
        assertEquals("body",res.getBody());
        assertEquals("title",res.getTitle());
    }

    private CreatePostResponse getCreatePostResponse() {
        CreatePostResponse createPostResponse = new CreatePostResponse();
        createPostResponse.setUserId(1);
        createPostResponse.setBody("body");
        createPostResponse.setTitle("title");
        return createPostResponse;
    }

    private UserDto getUserDto() {
        UserDto userDto = new UserDto();
        userDto.setEmail("me@me.com");
        userDto.setId(123L);
        return userDto;
    }

    private CreatePostRequest getCreatePostRequest() {
        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setBody("body");
        createPostRequest.setTitle("title");
        createPostRequest.setGender(CreatePostRequest.Gender.MALE);
        createPostRequest.setEmail("me@me.com");
        createPostRequest.setName("name");
        return createPostRequest;
    }

    private List<UserPostDto> getUserPostDtos() {
        List<UserPostDto> userPostDtos = new ArrayList<>();
        for(int i = 1; i <=200;i++){
            userPostDtos.add(getUserPostDto(i));
        }
        return userPostDtos;
    }
    private UserPostDto getUserPostDto(int i) {
        UserPostDto userPostDto = new UserPostDto();
        userPostDto.setUserId(100l);
        userPostDto.setTitle("some title");
        userPostDto.setBody("some body");
        userPostDto.setId(i);
        return userPostDto;
    }

    private Future<List<UserPostDto>> getFuture(List<UserPostDto> userDtoList) {
        return new Future<List<UserPostDto>>() {
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
                return userDtoList;
            }

            @Override
            public List<UserPostDto> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };
    }
}
