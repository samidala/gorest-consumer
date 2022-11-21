package com.techdisqus.service;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.dto.Response;
import com.techdisqus.dto.UserPostsResponse;
import com.techdisqus.rest.dto.*;
import javafx.geometry.Pos;
import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

//@Slf4j
@Component
public class UserPostServiceImpl implements UserPostService{


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
    @Autowired
    private Client client;

    @Autowired
    private ExecutorService executorService;

    @Override
    public Response createPost(CreatePostRequest request){

        Optional<User> userOptional = getUserDetailsByMailId(request.getEmail());
        User user;
        user = userOptional.orElseGet(() -> createUser(request));
        //.orElse(createUser(request));
        CreatePostResponse createPostResponse = createUserPost(request, user);
        return createPostResponse.get().toBuilder().userId(user.getId())
                .userGender(user.getGender())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .status(user.getStatus()).build();

    }

    private final User EMPTY = new User();


    @SneakyThrows
    @Override
    public UserPostsResponse getAllPosts() {


        /*Future<List<User>> usersFetchFuture = executorService.submit(this::getUsers);
        Future<List<UserPost>> postsFetchFuture = executorService.submit(this::getPosts);
        List<User> users = usersFetchFuture.get();
        Map<Long,User> userToIdMapping = getUserToIdMapping(users);
        List<UserPost> posts = postsFetchFuture.get();*/

        List<User> users = getUsers();
        Map<Long,User> userToIdMapping = getUserToIdMapping(users);
        List<UserPost> posts = getPosts();
        UserPostsResponse userPostsResponse = new UserPostsResponse();
        Map<Long,Set<Response>> userPosts = processPostsResponse(posts, userToIdMapping,userPostsResponse);

        userPostsResponse.setUsersWithPosts(posts.size() - userPostsResponse.getUsersWithoutPosts());
        userPostsResponse.setUserPosts(userPosts);
        return userPostsResponse;

    }

    private Map<Long, User> getUserToIdMapping(List<User> users) {
        return users.stream().collect(Collectors.toMap(
                User::getId, Function.identity()
        ));
    }

    private HashMap<Long, Set<Response>> processPostsResponse(List<UserPost> posts,
                                                        Map<Long, User> userToIdMapping,
                                                        UserPostsResponse userPostsResponse) {
        return posts.stream().map(post -> getResponse(post, userToIdMapping,userPostsResponse)
        ).collect(Collectors.groupingBy(Response::getUserId, HashMap::new,
                Collectors.toCollection(this::getUserPostsSet)));
                //Collectors.toCollection(TreeSet::new)));
    }

    private TreeSet<Response> getUserPostsSet(){
        return new TreeSet<>(SORT_BY_POST_ID_1);
    }

    private Response getResponse(UserPost post, Map<Long, User> userToIdMapping,
                                 UserPostsResponse userPostsResponse) {
        User user = EMPTY;
        if(post.getUserId() != null)
             user = userToIdMapping.getOrDefault(post.getUserId(), EMPTY);

        if(user.equals(EMPTY)){
            userPostsResponse.incrementPostsWithoutUsers();
        }else{
            userPostsResponse.incrementUsersWithPosts();
        }


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

    private static final Comparator<UserPost> SORT_BY_POST_ID = (o1, o2) -> (int) (o1.getId() - o2.getId());

    private static final Comparator<Response> SORT_BY_POST_ID_1 = (o1, o2) -> (int) (o1.getPostId() - o2.getPostId());

    private static final Comparator<UserPost> SORT_BY_USER_ID = (o1, o2) -> (int) (o1.getUserId() - o2.getUserId());

    private static final List<Comparator<UserPost>> comparators = new ArrayList<>();
    static {
        comparators.add(SORT_BY_USER_ID);
        comparators.add(SORT_BY_POST_ID);
    }
    private CreatePostResponse createUserPost(CreatePostRequest request, User user) {
        WebTarget webTarget = client.target(userPostUrl.replace("${userId}", user.getId()+""));//.path("employees");
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Authorization","Bearer 3a6ddfbacd8a61477da76a8be080f536b58ee662086ccbc35f8a5f232ada214d");
        CreatePostResponse createPostResponse = invocationBuilder.post(Entity.entity(UserPostDto.userPostDto(request),
                MediaType.APPLICATION_JSON_TYPE), CreatePostResponse.class);
        return createPostResponse;
    }


    protected User createUser(CreatePostRequest request){
        WebTarget webTarget = client.target(userCreateUrl);//.path("employees");
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Authorization","Bearer 3a6ddfbacd8a61477da76a8be080f536b58ee662086ccbc35f8a5f232ada214d");
        return invocationBuilder.post(Entity.entity(CreateUserDto.createUserDto(request),
                MediaType.APPLICATION_JSON),User.class);
    }
    protected Optional<User> getUserDetailsByMailId(String emailId)  {
        String url = findUserByMailUrl+emailId;
        WebTarget webTarget = client.target(url);//.path("employees");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        List<User> users = invocationBuilder.get(new GenericType<List<User>>() {});
        return users == null || users.isEmpty() ? Optional.empty() : users.stream().filter(user -> user.getEmail().equals(emailId)).findFirst();

    }

    private final int getUserCount(){
        return getCount(userListUrl);
    }

    private final int getPostsCount(){
        return getCount(allPostsUrl);
    }

    private final int getCount(String url){
        WebTarget webTarget = client.target(url);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        javax.ws.rs.core.Response response = invocationBuilder.get();
        return Integer.parseInt(response.getHeaders().get("x-pagination-total").get(0).toString());
    }
    @SneakyThrows
    private List<User> getUsers() {
        int count = getUserCount();
        int itr = count / 100;
        itr = itr + (count % 100 == 0 ? 0 : 1);
        List<Callable<List<User>>> callables = new ArrayList<>();
        List<User> users = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        for(int i = 1; i <= itr; i++){
            callables.add(() -> {
                String url = userListUrl+"?per_page="+countPerPage+"&page="+counter.getAndIncrement();
                WebTarget webTarget = client.target(url);
                Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
                List<User> list = invocationBuilder.get(new GenericType<List<User>>() {});
                return list;

            });
        }
        List<Future<List<User>>> futures = executorService.invokeAll(callables);
        for(Future<List<User>> f : futures){
            users.addAll(f.get());
        }

       /*
        String url = userListUrl+"per_page="+countPerPage+"&page="+pageNo;
        WebTarget webTarget = client.target(url);//.path("employees");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        javax.ws.rs.core.Response response = invocationBuilder.get();
        System.out.println(response.getHeaders());

        while (true) {
            url = userListUrl+"per_page="+countPerPage+"&page="+pageNo;
             webTarget = client.target(url);//.path("employees");
            invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            List<User> list = invocationBuilder.get(new GenericType<List<User>>() {});
            if(list.isEmpty()) break;
            users.addAll(list);
            pageNo++;
        }*/
        return users;
    }


    @SneakyThrows
    private List<UserPost> getPosts() {


        List<UserPost> userPosts = new ArrayList<>();

        int count = getPostsCount();
        int itr = count / 100;
        itr = itr + (count % 100 == 0 ? 0 : 1);
        List<Callable<List<UserPost>>> callables = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        for(int i = 1; i <= itr; i++){
            callables.add(() -> {
                String url = allPostsUrl+"?per_page="+countPerPage+"&page="+counter.getAndIncrement();
                WebTarget webTarget = client.target(url);
                Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
                List<UserPost> list = invocationBuilder.get(new GenericType<List<UserPost>>() {});
                return list;

            });
        }
        List<Future<List<UserPost>>> futures = executorService.invokeAll(callables);
        for(Future<List<UserPost>> f : futures){
            userPosts.addAll(f.get());
        }
       /* while (true) {
            String url = allPostsUrl +"per_page="+countPerPage+"&page="+pageNo;;
            WebTarget webTarget = client.target(url);//.path("employees");
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            List<UserPost> list = invocationBuilder.get(new GenericType<List<UserPost>>() {});
            if(list.isEmpty()) break;
            userPosts.addAll(list);
            pageNo++;
        }
*/
        return userPosts;
    }


}
