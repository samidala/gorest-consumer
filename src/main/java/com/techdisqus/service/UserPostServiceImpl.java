package com.techdisqus.service;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.dto.Response;
import com.techdisqus.dto.UserPostsResponse;
import com.techdisqus.rest.dto.CreatePostResponse;
import com.techdisqus.rest.dto.User;
import com.techdisqus.rest.dto.UserPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserPostServiceImpl implements UserPostService{

    private static Logger log = LoggerFactory.getLogger(UserPostServiceImpl.class);

    @Autowired
    private UserServiceHelper userServiceHelper;

    @Autowired
    private UserPostServiceHelper userPostServiceHelper;
    private final User EMPTY = new User();

    private static final Comparator<Response> SORT_BY_POST_ID = (o1, o2) -> (int) (o1.getPostId() - o2.getPostId());


    /**
     * creates post in target system. Checks if email is present in target system.
     * if not present create user with email id and then use the userId to create the post in the system
     * @param request
     * @return
     */
    @Override
    public Response createPost(CreatePostRequest request){

        Optional<User> userOptional = userServiceHelper.getUserDetailsByMailId(request.getEmail());
        User user = userOptional.orElseGet(() -> userServiceHelper.createUser(request));
        CreatePostResponse createPostResponse = userPostServiceHelper.createUserPost(request, user);
        log.info("post created successfully {}",createPostResponse.getId());
        return createPostResponse.get().toBuilder().userId(user.getId())
                .userGender(user.getGender())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .status(user.getStatus()).build();

    }

    /**
     * fetches the user posts from target system and transform the response.
     * @return
     */
    @Override
    public UserPostsResponse getAllPosts() {
        List<Future<List<User>>> userFutures = userServiceHelper.getUserListFutures();
        List<Future<List<UserPost>>> postsFutures = userPostServiceHelper.getUserPostsFutures();
        List<User> users = userServiceHelper.getUserList(userFutures);
        Map<Long,User> userToIdMapping = users.stream().collect(Collectors.toMap(
                                                User::getId, Function.identity()));
        List<UserPost> posts = userPostServiceHelper.getUserPostList(postsFutures);
        UserPostsResponse userPostsResponse = new UserPostsResponse();
        Map<Long,Set<Response>> userPosts = transformUserPostsResponse(posts, userToIdMapping,userPostsResponse);
        userPostsResponse.setUsersWithPosts(userPosts.size());
        userPostsResponse.setUsersWithoutPosts(users.size() - userPosts.size());
        userPostsResponse.setUserPosts(userPosts);
        return userPostsResponse;

    }

    /**
     * transform response as per awareX format
     * @param posts
     * @param userToIdMapping
     * @param userPostsResponse
     * @return
     */
    private HashMap<Long, Set<Response>> transformUserPostsResponse(List<UserPost> posts,
                                                                    Map<Long, User> userToIdMapping,
                                                                    UserPostsResponse userPostsResponse) {
        return posts.stream().map(post -> buildResponse(post, userToIdMapping, userPostsResponse))
                .collect(Collectors.groupingBy(Response::getUserId, HashMap::new,
                Collectors.toCollection(this::getUserPostsSet)));
    }


    private TreeSet<Response> getUserPostsSet(){
        return new TreeSet<>(SORT_BY_POST_ID);
    }

    /**
     * Helper method to build response
     * @param post
     * @param userToIdMapping
     * @param userPostsResponse
     * @return
     */
    private Response buildResponse(UserPost post, Map<Long, User> userToIdMapping,
                                   UserPostsResponse userPostsResponse) {
        User user = EMPTY;
        if(post.getUserId() != null) {
            userPostsResponse.incrementPostsWithoutUsers();
            user = userToIdMapping.getOrDefault(post.getUserId(), EMPTY);
        }
        return buildResponse(post, user);
    }

    /**
     * builds awareX response
     * @param post
     * @param user
     * @return
     */
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
}
