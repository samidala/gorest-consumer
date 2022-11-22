package com.techdisqus.service;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.exceptions.ErrorCodes;
import com.techdisqus.exceptions.InvalidInputException;
import com.techdisqus.exceptions.RequestExecutionException;
import com.techdisqus.rest.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class UserServiceHelper {

    @Value("${service.find.user.by.mail.url}")
    private String findUserByMailUrl;
    @Autowired
    private Client client;
    @Value("${service.all.users.url}")
    private String userListUrl;
    private static Logger log = LoggerFactory.getLogger(UserServiceHelper.class);
    @Value("${service.count.per.page}")
    private String countPerPage;

    @Value("${service.user.create.url}")
    private String userCreateUrl;

    @Value(("${rest.service.access.token}"))
    private String accessToken;
    @Autowired
    private ExecutorService executorService;

    @Autowired
    private RestHelperUtils restHelperUtils;

    /**
     * Based on the count the requests will be batched and each batch will fetch 100 requests and it is configurable in application.properties
     * @return
     */
    public List<Future<List<UserDto>>> getUserListFutures() {
        try {
            int count = restHelperUtils.getCount(userListUrl);
            int itr = RestHelperUtils.getIterationCount(count);
            List<Callable<List<UserDto>>> callables = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger(1);
            for(int i = 1; i <= itr; i++){
                callables.add(() -> getUsersFromTargetSystem(counter));
            }
            return executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RequestExecutionException(e,ErrorCodes.ERROR_FETCHING_USERS);
        }
    }


    /**
     * process the user futures and returns the user list
     * @param userFutures
     * @return
     */
    public List<UserDto> getUserList(List<Future<List<UserDto>>> userFutures) {
        try {
            List<UserDto> userDtos = new ArrayList<>(userFutures.size() * 100);

            for(Future<List<UserDto>> f : userFutures){
                userDtos.addAll(f.get());
            }
            return userDtos;
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestExecutionException(e,ErrorCodes.ERROR_FETCHING_USERS);
        }
    }
    /**
     * creates user in target system, if not that user by email does not exist in target system
     * @param request
     * @return
     */
    public UserDto createUser(CreatePostRequest request){
        WebTarget webTarget = client.target(userCreateUrl);
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Authorization","Bearer "+accessToken);
        UserDto userDto;
        UserDto user = UserDto.toUser(request);
        validateEntity(user);
        Response resp = invocationBuilder.post(Entity.entity(user,
                                    MediaType.APPLICATION_JSON));
        restHelperUtils.validateCreateResponse(resp,ErrorCodes.ERROR_CREATING_USER);
        userDto = resp.readEntity(UserDto.class);
        log.debug("user created successfully for email id {} and id is {}",request.getEmail(), userDto.getId());
        return userDto;
    }
    /**
     * Query target system by email ID and return optional of user
     * @param emailId to be queried
     * @return optional of user
     */
    public Optional<UserDto> getUserDetailsByMailId(String emailId)  {
        Invocation.Builder invocationBuilder = restHelperUtils.buildRequest(findUserByMailUrl + emailId);
        javax.ws.rs.core.Response resp = invocationBuilder.get();
        RestHelperUtils.checkResponseStatus(resp,ErrorCodes.ERROR_VALIDATING_EMAIL);
        List<UserDto> userDtos = resp.readEntity(new GenericType<List<UserDto>>() {});

        Optional<UserDto> optionalUser = userDtos == null || userDtos.isEmpty() ?
                Optional.empty() : userDtos.stream().filter(userDto -> userDto.getEmail().equals(emailId)).findFirst();
        log.info("user exists {} for email id {}",optionalUser.isPresent(),emailId);
        return userDtos == null || userDtos.isEmpty() ? Optional.empty() : userDtos.stream().filter(userDto -> userDto.getEmail().equals(emailId)).findFirst();

    }

    /**
     * Fetches users froms target system
     * @param counter
     * @return
     */
    private List<UserDto> getUsersFromTargetSystem(AtomicInteger counter) {
        String url = userListUrl+"?per_page="+countPerPage+"&page="+ counter.getAndIncrement();
        log.debug("url::: {}",url);
        Response response = restHelperUtils.buildRequest(url).get();
        RestHelperUtils.checkResponseStatus(response, ErrorCodes.ERROR_WHILE_GETTING_COUNT);
        return response.readEntity(new GenericType<List<UserDto>>() {});
    }

    /**
     * Validates user create input before sending to target system
     * @param userDto
     */
    private void validateEntity(UserDto userDto) {
        Set<ErrorCodes> errorCodes = new HashSet<>();
        if(!StringUtils.hasText(userDto.getEmail())){
            errorCodes.add(ErrorCodes.ERROR_MISSING_EMAIL);
        }
        if(userDto.getGender() == null){
            errorCodes.add(ErrorCodes.ERROR_MISSING_GENDER);
        }
        if(!StringUtils.hasText(userDto.getName())){
            errorCodes.add(ErrorCodes.ERROR_MISSING_NAME);
        }
        if(!errorCodes.isEmpty()){
            throw new InvalidInputException(errorCodes);
        }
    }

}
