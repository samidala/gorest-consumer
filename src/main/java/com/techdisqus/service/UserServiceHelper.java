package com.techdisqus.service;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.exceptions.ErrorCodes;
import com.techdisqus.exceptions.InvalidInputException;
import com.techdisqus.exceptions.RequestExecutionException;
import com.techdisqus.rest.dto.CreateUserDto;
import com.techdisqus.rest.dto.User;
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
    public List<Future<List<User>>> getUserListFutures() {
        try {
            int count = restHelperUtils.getCount(userListUrl);
            int itr = RestHelperUtils.getIterationCount(count);
            List<Callable<List<User>>> callables = new ArrayList<>();
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
    public List<User> getUserList(List<Future<List<User>>> userFutures) {
        try {
            List<User> users = new ArrayList<>(userFutures.size() * 100);

            for(Future<List<User>> f : userFutures){
                users.addAll(f.get());
            }
            return users;
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestExecutionException(e,ErrorCodes.ERROR_FETCHING_USERS);
        }
    }
    /**
     * creates user in target system, if not that user by email does not exist in target system
     * @param request
     * @return
     */
    public User createUser(CreatePostRequest request){
        WebTarget webTarget = client.target(userCreateUrl);
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Authorization","Bearer "+accessToken);
        User user;
        CreateUserDto createUserDto = CreateUserDto.createUserDto(request);
        validateEntity(createUserDto);
        Response resp = invocationBuilder.post(Entity.entity(createUserDto,
                                    MediaType.APPLICATION_JSON));
        restHelperUtils.validateCreateResponse(resp,ErrorCodes.ERROR_CREATING_USER);
        user = resp.readEntity(User.class);
        log.debug("user created successfully for email id {} and id is {}",request.getEmail(),user.getId());
        return user;
    }
    /**
     * Query target system by email ID and return optional of user
     * @param emailId to be queried
     * @return optional of user
     */
    public Optional<User> getUserDetailsByMailId(String emailId)  {
        Invocation.Builder invocationBuilder = restHelperUtils.buildRequest(findUserByMailUrl + emailId);
        javax.ws.rs.core.Response resp = invocationBuilder.get();
        List<User> users;
        users = validateAndGetUsers(resp);
        Optional<User> optionalUser = users == null || users.isEmpty() ?
                Optional.empty() : users.stream().filter(user -> user.getEmail().equals(emailId)).findFirst();
        log.info("user exists {} for email id {}",optionalUser.isPresent(),emailId);
        return users == null || users.isEmpty() ? Optional.empty() : users.stream().filter(user -> user.getEmail().equals(emailId)).findFirst();

    }

    /**
     * Validates the rest call status and gets user list
     * @param resp
     * @return
     */
    private List<User> validateAndGetUsers(javax.ws.rs.core.Response resp) {
        List<User> users;
        if(resp.getStatus()!= 200){
            throw new RequestExecutionException(ErrorCodes.ERROR_VALIDATING_EMAIL);
        }else{
            users = resp.readEntity(new GenericType<List<User>>() {});
        }
        return users;
    }

    /**
     * Fetches users froms target system
     * @param counter
     * @return
     */
    private List<User> getUsersFromTargetSystem(AtomicInteger counter) {
        String url = userListUrl+"?per_page="+countPerPage+"&page="+ counter.getAndIncrement();
        log.debug("url::: {}",url);

        javax.ws.rs.core.Response response = restHelperUtils.buildRequest(url).get();
        RestHelperUtils.checkResponseStatus(response, ErrorCodes.ERROR_WHILE_GETTING_COUNT);
        return response.readEntity(new GenericType<List<User>>() {});
    }

    /**
     * Validates user create input before sending to target system
     * @param createUserDto
     */
    private void validateEntity(CreateUserDto createUserDto) {
        Set<ErrorCodes> errorCodes = new HashSet<>();
        if(!StringUtils.hasText(createUserDto.getEmail())){
            errorCodes.add(ErrorCodes.ERROR_MISSING_EMAIL);
        }
        if(createUserDto.getGender() == null){
            errorCodes.add(ErrorCodes.ERROR_MISSING_GENDER);
        }
        if(!StringUtils.hasText(createUserDto.getName())){
            errorCodes.add(ErrorCodes.ERROR_MISSING_NAME);
        }
        if(!errorCodes.isEmpty()){
            throw new InvalidInputException(errorCodes);
        }
    }

}
