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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
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
        userPostServiceHelper.setClient(client);
        Invocation.Builder b = Mockito.mock(Invocation.Builder.class, RETURNS_DEEP_STUBS);
        WebTarget target = Mockito.mock(WebTarget.class,RETURNS_DEEP_STUBS);
        List<UserPostDto> userPostDtos = new ArrayList<>();
        for(int i = 1; i <=200;i++){
            userPostDtos.add(getUserPostDto(i));
        }

        Response responseMock = Mockito.mock(Response.class,invocationOnMock -> Response.ok(userPostDtos).build());

        when(client.target(anyString())).thenReturn(target);
        when(target.request(APPLICATION_JSON)).thenReturn(b);

        when(b.get()).thenReturn(responseMock);
        when(restHelperUtils.getCount(anyString())).thenReturn(200);
       // when(responseMock.getStatus()).thenReturn(200);
        when(responseMock.readEntity(any(GenericType.class))).thenReturn(userPostDtos);
        List<Future<List<UserPostDto>>> futureList = userPostServiceHelper.getUserPostsFutures();
        assertEquals(2,futureList.size());
    }

    @Test
    public void testGetUserPostList() throws ExecutionException, InterruptedException {
        ArrayList<Future<List<UserPostDto>>> mockFuture =   Mockito.mock(ArrayList.class,RETURNS_DEEP_STUBS);
        Iterator<Future<List<UserPostDto>>> itr = Mockito.mock(Iterator.class,RETURNS_DEEP_STUBS);
        Future<List<UserPostDto>> future = Mockito.mock(Future.class,RETURNS_DEEP_STUBS);
        when(mockFuture.iterator()).thenReturn(itr);
        when(itr.hasNext()).thenReturn(true,true,false);
        when(itr.next()).thenReturn(future);
        when(future.get()).thenReturn(getUserPostDtos(),getUserPostDtos());
        List<UserPostDto> userPostDtos = userPostServiceHelper.getUserPostList(mockFuture);
        for(Future<List<UserPostDto>> f : mockFuture){
            System.out.println(f.get());
        }
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
        userPostServiceHelper.setClient(client);
        Invocation.Builder b = Mockito.mock(Invocation.Builder.class, RETURNS_DEEP_STUBS);
        WebTarget target = Mockito.mock(WebTarget.class,RETURNS_DEEP_STUBS);
        when(client.target(anyString())).thenReturn(target);
        when(target.request(MediaType.APPLICATION_JSON)).thenReturn(b);
        CreatePostResponse createPostResponse = getCreatePostResponse();
        createPostResponse.setId(1l);
        Response responseMock = Mockito.mock(Response.class,invocationOnMock -> Response.ok(createPostResponse).build());
        when(b.post(any(Entity.class))).thenReturn(responseMock);
        //when(responseMock.getStatus()).thenReturn(200);
        when(responseMock.readEntity(CreatePostResponse.class)).thenReturn(createPostResponse);
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
        userDto.setId(123);
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
}
