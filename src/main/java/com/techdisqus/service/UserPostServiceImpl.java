package com.techdisqus.service;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.dto.Response;
import com.techdisqus.dto.UserPostsResponse;
import com.techdisqus.exceptions.ErrorCodes;
import com.techdisqus.exceptions.InvalidInputException;
import com.techdisqus.exceptions.RequestExecutionException;
import com.techdisqus.rest.dto.CreatePostResponse;
import com.techdisqus.rest.dto.CreateUserDto;
import com.techdisqus.rest.dto.User;
import com.techdisqus.rest.dto.UserPost;
import com.techdisqus.rest.dto.UserPostDto;

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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserPostServiceImpl implements UserPostService{

    private static Logger log = LoggerFactory.getLogger(UserPostServiceImpl.class);

    @Value("${service.user.create.url}")
    private String userCreateUrl;

    @Value("${service.user.post.url}")
    private String userPostUrl;
    @Value("${service.all.users.url}")
    private String userListUrl;

    @Value("${service.all.list.url}")
    private String allPostsUrl;

    @Value("${service.find.user.by.mail.url}")
    private String findUserByMailUrl;

    @Value("${service.count.per.page}")
    private String countPerPage;

    @Value(("${rest.service.access.token}"))
    private String accessToken;

    @Autowired
    private Client client;

    @Autowired
    private ExecutorService executorService;
    private final User EMPTY = new User();

    private static final Comparator<Response> SORT_BY_POST_ID = (o1, o2) -> (int) (o1.getPostId() - o2.getPostId());


    @Override
    public Response createPost(CreatePostRequest request){

        Optional<User> userOptional = getUserDetailsByMailId(request.getEmail());
        User user = userOptional.orElseGet(() -> createUser(request));
        CreatePostResponse createPostResponse = createUserPost(request, user);
        log.info("post created successfully {}",createPostResponse.getId());
        return createPostResponse.get().toBuilder().userId(user.getId())
                .userGender(user.getGender())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .status(user.getStatus()).build();

    }

    @Override
    public UserPostsResponse getAllPosts() {
        List<Future<List<User>>> userFutures = getUserListFutures();
        List<Future<List<UserPost>>> postsFutures = getUserPostsFutures();
        List<User> users = getUserList(userFutures);
        Map<Long,User> userToIdMapping = getUserToIdMapping(users);
        List<UserPost> posts = getUserPostList(postsFutures);
        UserPostsResponse userPostsResponse = new UserPostsResponse();
        Map<Long,Set<Response>> userPosts = trasnfromUserPostsResponse(posts, userToIdMapping,userPostsResponse);
        userPostsResponse.setUsersWithPosts(userPosts.size());
        userPostsResponse.setUsersWithoutPosts(users.size() - userPosts.size());
        userPostsResponse.setUserPosts(userPosts);
        return userPostsResponse;

    }


    protected List<Future<List<UserPost>>> getUserPostsFutures() {
        List<Future<List<UserPost>>> postsFutures;
        try {
            postsFutures = prepareAndExecUserPostCalls();
        } catch (InterruptedException e) {
            throw new RequestExecutionException(e,ErrorCodes.ERROR_FETCHING_USER_POSTS);
        }
        return postsFutures;
    }

    private List<Future<List<User>>> getUserListFutures() {
        List<Future<List<User>>> userFutures;
        try {
            userFutures = prepareAndExecAllUsersCall();
        } catch (InterruptedException e) {
            throw new RequestExecutionException(e,ErrorCodes.ERROR_FETCHING_USERS);
        }
        return userFutures;
    }

    private List<UserPost> getUserPostList(List<Future<List<UserPost>>> postsFutures) {
        List<UserPost> posts;
        try {
            posts = getUserPosts(postsFutures);
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestExecutionException(e,ErrorCodes.ERROR_FETCHING_USER_POSTS);
        }
        return posts;
    }

    private List<User> getUserList(List<Future<List<User>>> userFutures) {
        List<User> users;
        try {
            users = getUsers(userFutures);
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestExecutionException(e,ErrorCodes.ERROR_FETCHING_USERS);
        }
        return users;
    }

    private Map<Long, User> getUserToIdMapping(List<User> users) {
        return users.stream().collect(Collectors.toMap(
                User::getId, Function.identity()
        ));
    }

    private HashMap<Long, Set<Response>> trasnfromUserPostsResponse(List<UserPost> posts,
                                                                    Map<Long, User> userToIdMapping,
                                                                    UserPostsResponse userPostsResponse) {
        return posts.stream().map(post -> getResponse(post, userToIdMapping, userPostsResponse)
        ).collect(Collectors.groupingBy(Response::getUserId, HashMap::new,
                Collectors.toCollection(this::getUserPostsSet)));
    }

    private TreeSet<Response> getUserPostsSet(){
        return new TreeSet<>(SORT_BY_POST_ID);
    }

    private Response getResponse(UserPost post, Map<Long, User> userToIdMapping,
                                 UserPostsResponse userPostsResponse) {
        User user = EMPTY;
        if(post.getUserId() != null)
             user = userToIdMapping.getOrDefault(post.getUserId(), EMPTY);

        if(user.equals(EMPTY)){
            userPostsResponse.incrementPostsWithoutUsers();
        }
        return buildResponse(post, user);
    }

    private Response buildResponse(UserPost post, User user) {
        return Response.builder()
                .postBody(post.getBody())
                .postId(post.getId())
                .postTitle(post.getTitle())
                .userId(post.getUserId())
                .userName(user.getName())
                .status(user.getStatus())
                .userEmail(user.getEmail())
                .userGender(user.getGender())
                .build();
    }

    private CreatePostResponse createUserPost(CreatePostRequest request, User user) {
        WebTarget webTarget = client.target(userPostUrl.replace("${userId}", user.getId()+""));//.path("employees");
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Authorization","Bearer "+accessToken);

        javax.ws.rs.core.Response resp = invocationBuilder.post(Entity.entity(UserPostDto.userPostDto(request),
                MediaType.APPLICATION_JSON_TYPE));
        if(resp.getStatus() == 401){
            throw new RequestExecutionException(ErrorCodes.ERROR_AUTH_FAILED);
        }else if(resp.getStatus() != 201){
            throw new RequestExecutionException(ErrorCodes.ERROR_CREATING_USER);
        }
        CreatePostResponse createPostResponse = resp.readEntity(CreatePostResponse.class);
        log.debug("response id {} ",createPostResponse.getId());
        return createPostResponse;
    }


    protected User createUser(CreatePostRequest request){
        WebTarget webTarget = client.target(userCreateUrl);
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Authorization","Bearer "+accessToken);
        User user;
        javax.ws.rs.core.Response resp = invocationBuilder.post(getEntity(CreateUserDto.createUserDto(request)));
        if(resp.getStatus()!= 201){
            if(resp.getStatus() == 401){
                throw new RequestExecutionException(ErrorCodes.ERROR_AUTH_FAILED);
            }
            throw new RequestExecutionException(ErrorCodes.ERROR_CREATING_USER);
        }else{
            user = resp.readEntity(User.class);
        }

        log.debug("user created successfully for email id {} and id is {}",request.getEmail(),user.getId());
        return user;
    }

    private Entity<CreateUserDto> getEntity( CreateUserDto createUserDto) {

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
        return Entity.entity(createUserDto,
                MediaType.APPLICATION_JSON);
    }

    protected Optional<User> getUserDetailsByMailId(String emailId)  {
        Invocation.Builder invocationBuilder = buildRequest(findUserByMailUrl + emailId);
        javax.ws.rs.core.Response resp = invocationBuilder.get();
        List<User> users;
        users = getAndValidateStatus(resp);
        Optional<User> optionalUser = users == null || users.isEmpty() ?
                Optional.empty() : users.stream().filter(user -> user.getEmail().equals(emailId)).findFirst();
        log.info("user exists {} for email id {}",optionalUser.isPresent(),emailId);
        return users == null || users.isEmpty() ? Optional.empty() : users.stream().filter(user -> user.getEmail().equals(emailId)).findFirst();

    }

    private List<User> getAndValidateStatus(javax.ws.rs.core.Response resp) {
        List<User> users;
        if(resp.getStatus()!= 200){
            throw new RequestExecutionException(ErrorCodes.ERROR_VALIDATING_EMAIL);
        }else{
            users = resp.readEntity(new GenericType<List<User>>() {});
        }
        return users;
    }

    private int getUserCount(){
        return getCount(userListUrl);
    }

    private int getPostsCount(){
        return getCount(allPostsUrl);
    }

    private int getCount(String url){
        WebTarget webTarget = client.target(url);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        javax.ws.rs.core.Response response = invocationBuilder.get();
        checkResponseStatus(response, ErrorCodes.ERROR_WHILE_GETTING_COUNT);
        return Integer.parseInt(response.getHeaders().get("x-pagination-total").get(0).toString());
    }
    private List<Future<List<User>>> prepareAndExecAllUsersCall() throws InterruptedException {
        int count = getUserCount();
        int itr = getIterationCount(count);
        List<Callable<List<User>>> callables = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        for(int i = 1; i <= itr; i++){
            callables.add(() -> fetchUsers(counter));
        }
        return executorService.invokeAll(callables);
    }

    private int getIterationCount(int count) {
        int itr = count / 100;
        itr = itr + (count % 100 == 0 ? 0 : 1);
        return itr;
    }


    private List<User> fetchUsers(AtomicInteger counter) {
        String url = userListUrl+"?per_page="+countPerPage+"&page="+ getAndIncrement(counter);
        log.debug("url::: {}",url);
        try{
            javax.ws.rs.core.Response response = buildRequest(url).get();
            checkResponseStatus(response, ErrorCodes.ERROR_FETCHING_USERS);
            return response.readEntity(new GenericType<List<User>>() {});
        }catch (Exception e){
            throw new RequestExecutionException(e,ErrorCodes.ERROR_FETCHING_USERS);
        }

    }

    private int getAndIncrement(final AtomicInteger counter) {
        synchronized (counter) {
            return counter.getAndIncrement();
        }
    }


    private List<User> getUsers(List<Future<List<User>>> futures) throws InterruptedException, ExecutionException {
        List<User> users = new ArrayList<>(futures.size() * 100);

        for(Future<List<User>> f : futures){
            users.addAll(f.get());
        }
        return users;
    }



    private List<Future<List<UserPost>>> prepareAndExecUserPostCalls() throws InterruptedException {
        int count = getPostsCount();
        int itr = getIterationCount(count);
        List<Callable<List<UserPost>>> callables = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        for(int i = 1; i <= itr; i++){
            callables.add(() -> getUserPosts(counter));
        }
        return executorService.invokeAll(callables);
    }

    private List<UserPost> getUserPosts(AtomicInteger counter) {
        String url = allPostsUrl
                + "?per_page=" + countPerPage + "&page=" + getAndIncrement(counter);
        log.info("url for getting user posts {}",url);
        javax.ws.rs.core.Response response = buildRequest(url).get();
        checkResponseStatus(response, ErrorCodes.ERROR_FETCHING_USER_POSTS);
        return response.readEntity(new GenericType<List<UserPost>>() {});
    }

    private void checkResponseStatus(javax.ws.rs.core.Response response, ErrorCodes errorFetchingUserPosts) {
        if(response.getStatus() != 200){
            throw new RequestExecutionException(errorFetchingUserPosts);
        }
    }

    private Invocation.Builder buildRequest(String url) {
        WebTarget webTarget = client.target(url);
        return webTarget.request(MediaType.APPLICATION_JSON);
    }

    private List<UserPost> getUserPosts(List<Future<List<UserPost>>> futures) throws InterruptedException, ExecutionException {
        List<UserPost> userPosts = new ArrayList<>(futures.size() * 100);
        for(Future<List<UserPost>> f : futures){
            userPosts.addAll(f.get());
        }
        return userPosts;
    }
}
