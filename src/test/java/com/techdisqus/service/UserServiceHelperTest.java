package com.techdisqus.service;

import com.techdisqus.config.AppConfig;
import com.techdisqus.exceptions.RequestExecutionException;
import com.techdisqus.rest.dto.UserDto;
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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.techdisqus.service.TestHelperUtils.getCreatePostRequest;
import static com.techdisqus.service.TestHelperUtils.getUserDto;
import static com.techdisqus.service.TestHelperUtils.getUserDtoList;
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
    public void testGetUsersFutures(){
        RestHelperUtils restHelperUtils =  Mockito.mock(RestHelperUtils.class,RETURNS_DEEP_STUBS);
        Client client = Mockito.mock(Client.class,RETURNS_DEEP_STUBS);
        restHelperUtils.setClient(client);
        userServiceHelper.setRestHelperUtils(restHelperUtils);
        List<UserDto> userDtos = new ArrayList<>();
        for(int i = 1; i <=200;i++){
            userDtos.add(getUserDto(i));
        }
        when(restHelperUtils.getCount(anyString())).thenReturn(200);
        when(restHelperUtils.executeGet(anyString(),any(GenericType.class), any())).thenReturn(userDtos);
        List<Future<List<UserDto>>> futureList = userServiceHelper.getUserListFutures();
        assertEquals(2,futureList.size());
        int count = 0;
        for(Future<List<UserDto>> f : futureList){
            count+=f.get().size();
        }
        assertEquals(400,count);
    }

    @Test
    public void testGetUserList() {
        List<Future<List<UserDto>>> userFutures  = new ArrayList<>();
        userFutures.add(TestHelperUtils.getFuture(getUserDtoList()));
        userFutures.add(TestHelperUtils.getFuture(getUserDtoList()));
        List<UserDto> userDtos = userServiceHelper.getUserList(userFutures);
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


    @Test
    public void testGetUserByMailIdOnError(){
        RestHelperUtils restHelperUtils =  Mockito.mock(RestHelperUtils.class,RETURNS_DEEP_STUBS);
        Client client = Mockito.mock(Client.class,RETURNS_DEEP_STUBS);
        restHelperUtils.setClient(client);
        userServiceHelper.setRestHelperUtils(restHelperUtils);
        when(restHelperUtils.executeGet(anyString(),any(GenericType.class),any())).thenReturn(new ArrayList<>());
        Optional<UserDto> opt = userServiceHelper.getUserDetailsByMailId("abc.com");
        assertEquals(false,opt.isPresent());
    }
    @Test
    public void testGetUserByMailIdOnSuccess(){
        UserDto userDto = getUserDto(1);
        RestHelperUtils restHelperUtils =  Mockito.mock(RestHelperUtils.class,RETURNS_DEEP_STUBS);
        Client client = Mockito.mock(Client.class,RETURNS_DEEP_STUBS);
        restHelperUtils.setClient(client);
        userServiceHelper.setRestHelperUtils(restHelperUtils);
        List<UserDto> dtos = new ArrayList<>();
        dtos.add(userDto);
        when(restHelperUtils.executeGet(anyString(),any(GenericType.class),any())).thenReturn(dtos);
        Optional<UserDto> opt = userServiceHelper.getUserDetailsByMailId("me@me.com");
        assertEquals(true,opt.isPresent());
        assertEquals("me@me.com",opt.get().getEmail());
    }

    @Test
    public void testCreateUser(){
        UserDto userDto = getUserDto(1);
        RestHelperUtils restHelperUtils =  Mockito.mock(RestHelperUtils.class,RETURNS_DEEP_STUBS);
        Client client = Mockito.mock(Client.class,RETURNS_DEEP_STUBS);
        restHelperUtils.setClient(client);
        userServiceHelper.setRestHelperUtils(restHelperUtils);
        when(restHelperUtils.executePost(anyString(),any(),any(Class.class),any(),any())).thenReturn(userDto);
        UserDto res = userServiceHelper.createUser(getCreatePostRequest());

        assertEquals(userDto.getEmail(),res.getEmail());
        assertEquals(1,res.getId());
        assertEquals(userDto.getName(),res.getName());
        assertEquals(userDto.getGender(),res.getGender());
    }

}
