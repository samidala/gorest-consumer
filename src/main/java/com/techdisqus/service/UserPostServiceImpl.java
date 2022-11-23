package com.techdisqus.service;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.dto.UserPostDetails;
import com.techdisqus.dto.UserPostsResponse;
import com.techdisqus.rest.dto.CreatePostResponse;
import com.techdisqus.rest.dto.UserDto;
import com.techdisqus.rest.dto.UserPostDto;
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

    public void setUserPostServiceHelper(UserPostServiceHelper userPostServiceHelper) {
        this.userPostServiceHelper = userPostServiceHelper;
    }

    public void setUserServiceHelper(UserServiceHelper userServiceHelper) {
        this.userServiceHelper = userServiceHelper;
    }

    private final UserDto EMPTY = new UserDto();

    private static final Comparator<UserPostDetails> SORT_BY_POST_ID = (o1, o2) -> (int) (o1.getPostId() - o2.getPostId());


    /**
     * creates post in target system. Checks if email is present in target system.
     * if not present create user with email id and then use the userId to create the post in the system
     * @param request
     * @return
     */
    @Override
    public UserPostDetails createPost(CreatePostRequest request){

        Optional<UserDto> userOptional = userServiceHelper.getUserDetailsByMailId(request.getEmail());
        UserDto userDto = userOptional.orElseGet(() -> userServiceHelper.createUser(request));
        CreatePostResponse createPostResponse = userPostServiceHelper.createUserPost(request, userDto);
        log.info("post created successfully {}",createPostResponse.getId());
        return createPostResponse.toUserPostDetails().toBuilder().userId(userDto.getId())
                .userGender(userDto.getGender())
                .userName(userDto.getName())
                .userEmail(userDto.getEmail())
                .status(userDto.getStatus()).build();

    }

    /**
     * fetches the user posts from target system and transform the response.
     * @return
     */
    @Override
    public UserPostsResponse getAllPosts() {
        List<Future<List<UserDto>>> userFutures = userServiceHelper.getUserListFutures();
        List<Future<List<UserPostDto>>> postsFutures = userPostServiceHelper.getUserPostsFutures();
        List<UserDto> userDtos = userServiceHelper.getUserList(userFutures);
        log.info("userDtos size {} ",userDtos.size());
        Map<Long, UserDto> userToIdMapping = userDtos.stream().collect(Collectors.toMap(
                                                UserDto::getId, Function.identity()));
        List<UserPostDto> posts = userPostServiceHelper.getUserPostList(postsFutures);
        log.info("user posts size {} ",posts.size());
        UserPostsResponse userPostsResponse = new UserPostsResponse();
        Map<Long,Set<UserPostDetails>> userPosts = transformUserPostsResponse(posts, userToIdMapping,userPostsResponse);
        userPostsResponse.setUsersWithPosts(userPosts.size());
        userPostsResponse.setUsersWithoutPosts(userDtos.size() - userPosts.size());
        userPostsResponse.setUserPosts(userPosts);
        log.info("user posts {} usersWithPosts {} usersWithoutPosts {} and postsWithoutUser {} ",
                userPosts.size(),userPostsResponse.getUsersWithPosts(),userPostsResponse.getPostsWithoutUsers(),
                userPostsResponse.getPostsWithoutUsers());
        return userPostsResponse;

    }

    /**
     * transform response as per awareX format
     * @param posts
     * @param userToIdMapping
     * @param userPostsResponse
     * @return
     */
    private HashMap<Long, Set<UserPostDetails>> transformUserPostsResponse(List<UserPostDto> posts,
                                                                           Map<Long, UserDto> userToIdMapping,
                                                                           UserPostsResponse userPostsResponse) {
        return posts.stream().map(post -> buildResponse(post, userToIdMapping, userPostsResponse))
                .collect(Collectors.groupingBy(UserPostDetails::getUserId, HashMap::new,
                Collectors.toCollection(this::getUserPostsSet)));
    }


    private TreeSet<UserPostDetails> getUserPostsSet(){
        return new TreeSet<>(SORT_BY_POST_ID);
    }

    /**
     * Helper method to build response
     * @param post
     * @param userToIdMapping
     * @param userPostsResponse
     * @return
     */
    private UserPostDetails buildResponse(UserPostDto post, Map<Long, UserDto> userToIdMapping,
                                          UserPostsResponse userPostsResponse) {
        UserDto userDto = EMPTY;
        if(post.getUserId() != null) {
            userPostsResponse.incrementPostsWithoutUsers();
            userDto = userToIdMapping.getOrDefault(post.getUserId(), EMPTY);
        }
        return buildResponse(post, userDto);
    }

    /**
     * builds awareX response
     * @param post
     * @param userDto
     * @return
     */
    private UserPostDetails buildResponse(UserPostDto post, UserDto userDto) {
        return UserPostDetails.builder()
                .postBody(post.getBody())
                .postId(post.getId())
                .postTitle(post.getTitle())
                .userId(post.getUserId())
                .userName(userDto.getName())
                .status(userDto.getStatus())
                .userEmail(userDto.getEmail())
                .userGender(userDto.getGender())
                .build();
    }
}
