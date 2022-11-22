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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class UserServiceHelperTest {
    @Autowired
    @InjectMocks
    private UserServiceHelper userServiceHelper;

    @Mock
    private RestHelperUtils restHelperUtils;

    @Mock
    private Client client;

    @Test
    public void testUserHelperNotNull(){
        assertNotNull(userServiceHelper);
    }

    @SneakyThrows
    @Test
    public void testGetUserPostsFutures(){
        RestHelperUtils restHelperUtils =  Mockito.mock(RestHelperUtils.class,RETURNS_DEEP_STUBS);
        Client client = Mockito.mock(Client.class,RETURNS_DEEP_STUBS);
        restHelperUtils.setClient(client);
        userServiceHelper.setRestHelperUtils(restHelperUtils);
        userServiceHelper.setClient(client);
        Invocation.Builder b = Mockito.mock(Invocation.Builder.class, RETURNS_DEEP_STUBS);
        WebTarget target = Mockito.mock(WebTarget.class,RETURNS_DEEP_STUBS);
        List<UserDto> userDtos = new ArrayList<>();
        for(int i = 1; i <=200;i++){
            userDtos.add(getUserDto(i));
        }

        Response responseMock = Mockito.mock(Response.class, invocationOnMock -> Response.ok(userDtos).build());

        when(client.target(anyString())).thenReturn(target);
        when(target.request(APPLICATION_JSON)).thenReturn(b);

        when(b.get()).thenReturn(responseMock);
        when(restHelperUtils.getCount(anyString())).thenReturn(200);
        // when(responseMock.getStatus()).thenReturn(200);
        when(responseMock.readEntity(any(GenericType.class))).thenReturn(userDtos);
        List<Future<List<UserDto>>> futureList = userServiceHelper.getUserListFutures();
        assertEquals(2,futureList.size());
    }

    @Test
    public void testGetUserList() throws ExecutionException, InterruptedException {
        ArrayList<Future<List<UserDto>>> mockFuture =   Mockito.mock(ArrayList.class,RETURNS_DEEP_STUBS);
        Iterator<Future<List<UserDto>>> itr = Mockito.mock(Iterator.class,RETURNS_DEEP_STUBS);
        Future<List<UserDto>> future = Mockito.mock(Future.class,RETURNS_DEEP_STUBS);
        when(mockFuture.iterator()).thenReturn(itr);
        when(itr.hasNext()).thenReturn(true,true,false);
        when(itr.next()).thenReturn(future);
        when(future.get()).thenReturn(getUserDtoList(),getUserDtoList());
        List<UserDto> userDtos = userServiceHelper.getUserList(mockFuture);
        for(Future<List<UserDto>> f : mockFuture){
            userDtos.addAll(f.get());

        }
        assertEquals(200,userDtos.size());
    }

    @Test
    public void testGetUserListOnError() throws ExecutionException, InterruptedException {
        ArrayList<Future<List<UserDto>>> mockFuture =   Mockito.mock(ArrayList.class,RETURNS_DEEP_STUBS);
        Iterator<Future<List<UserDto>>> itr = Mockito.mock(Iterator.class,RETURNS_DEEP_STUBS);
        Future<List<UserDto>> future = Mockito.mock(Future.class,RETURNS_DEEP_STUBS);
        when(mockFuture.iterator()).thenReturn(itr);
        when(itr.hasNext()).thenReturn(true,true,false);
        when(itr.next()).thenReturn(future);
        when(future.get()).thenThrow(new ExecutionException("failed execution",new Throwable()));
        RequestExecutionException thrown = assertThrows(
                RequestExecutionException.class,
                () -> userServiceHelper.getUserList(mockFuture), "failed execution");

        assertTrue(thrown.getMessage().contains("1203"));

    }

    /*
     Invocation.Builder invocationBuilder = restHelperUtils.buildRequest(findUserByMailUrl + emailId);
        javax.ws.rs.core.Response resp = invocationBuilder.get();
        RestHelperUtils.checkResponseStatus(resp,ErrorCodes.ERROR_VALIDATING_EMAIL);
        List<UserDto> userDtos = resp.readEntity(new GenericType<List<UserDto>>() {});

        Optional<UserDto> optionalUser = userDtos == null || userDtos.isEmpty() ?
                Optional.empty() : userDtos.stream().filter(userDto -> userDto.getEmail().equals(emailId)).findFirst();
        log.info("user exists {} for email id {}",optionalUser.isPresent(),emailId);
        return userDtos == null || userDtos.isEmpty() ? Optional.empty() : userDtos.stream().filter(userDto -> userDto.getEmail().equals(emailId)).findFirst();
     */
    @Test
    public void testGetUserByMailIdOnError(){
        UserDto userDto = getUserDto(1);
        RestHelperUtils restHelperUtils =  Mockito.mock(RestHelperUtils.class,RETURNS_DEEP_STUBS);
        Client client = Mockito.mock(Client.class,RETURNS_DEEP_STUBS);
        restHelperUtils.setClient(client);
        userServiceHelper.setRestHelperUtils(restHelperUtils);
        userServiceHelper.setClient(client);
        Invocation.Builder b = Mockito.mock(Invocation.Builder.class, RETURNS_DEEP_STUBS);
        WebTarget target = Mockito.mock(WebTarget.class,RETURNS_DEEP_STUBS);
        when(client.target(anyString())).thenReturn(target);
        when(target.request(MediaType.APPLICATION_JSON)).thenReturn(b);
        Response responseMock = Mockito.mock(Response.class,invocationOnMock -> Response.ok(Arrays.asList(userDto)).build());
        List<UserDto> dtos = new ArrayList<>();
        dtos.add(userDto);
        List<UserDto> userDtos = Mockito.mock(List.class,invocationOnMock -> dtos);
        when(b.get()).thenReturn(responseMock);
        when(responseMock.readEntity(any(GenericType.class))).thenReturn(userDtos);
        when(responseMock.readEntity(List.class)).thenReturn(userDtos);
        Optional<UserDto> opt = userServiceHelper.getUserDetailsByMailId("abc.com");
        assertEquals(false,opt.isPresent());
    }

    @Test
    public void testCreateUser(){
        UserDto userDto = getUserDto(1);
        RestHelperUtils restHelperUtils =  Mockito.mock(RestHelperUtils.class,RETURNS_DEEP_STUBS);
        Client client = Mockito.mock(Client.class,RETURNS_DEEP_STUBS);
        restHelperUtils.setClient(client);
        userServiceHelper.setRestHelperUtils(restHelperUtils);
        userServiceHelper.setClient(client);
        Invocation.Builder b = Mockito.mock(Invocation.Builder.class, RETURNS_DEEP_STUBS);
        WebTarget target = Mockito.mock(WebTarget.class,RETURNS_DEEP_STUBS);
        when(client.target(anyString())).thenReturn(target);
        when(target.request(MediaType.APPLICATION_JSON)).thenReturn(b);

        Response responseMock = Mockito.mock(Response.class,invocationOnMock -> Response.ok(userDto).build());
        when(b.post(any(Entity.class))).thenReturn(responseMock);
        //when(responseMock.getStatus()).thenReturn(200);
        when(responseMock.readEntity(UserDto.class)).thenReturn(userDto);
        UserDto res = userServiceHelper.createUser(getCreatePostRequest());

        assertEquals(userDto.getEmail(),res.getEmail());
        assertEquals(123,res.getId());
        assertEquals(userDto.getName(),res.getName());
        assertEquals(userDto.getGender(),res.getGender());
    }
    private List<UserDto> getUserDtoList(){
        List<UserDto> userDtos = new ArrayList<>(200);
        for(int i = 1; i <= 100; i++){
            userDtos.add(getUserDto(i));
        }
        return userDtos;
    }
    private UserDto getUserDto(int i) {
        UserDto userDto = new UserDto();
        userDto.setId(i);
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

}
