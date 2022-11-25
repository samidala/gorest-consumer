package com.techdisqus.service;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.exceptions.ErrorCodes;
import com.techdisqus.exceptions.InvalidInputException;
import com.techdisqus.exceptions.RequestExecutionException;
import com.techdisqus.rest.dto.CreatePostResponse;
import com.techdisqus.rest.dto.UserDto;
import com.techdisqus.rest.dto.UserPostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class UserPostServiceHelper {

    private static Logger log = LoggerFactory.getLogger(UserPostServiceHelper.class);
    @Value("${service.all.list.url}")
    private String allPostsUrl;

    @Value("${service.count.per.page}")
    private String countPerPage;
    @Autowired
    private ExecutorService executorService;

    @Autowired
    private RestHelperUtils restHelperUtils;

    @Value("${service.user.post.url}")
    private String userPostUrl;
    @Value(("${rest.service.access.token}"))
    private String accessToken;
    //Useful for setting mock objects
    protected void setRestHelperUtils(RestHelperUtils restHelperUtils) {
        this.restHelperUtils = restHelperUtils;
    }


    /**
     * Invokes userposts read call in batches in multiple threads
     * @return
     */
    public List<Future<List<UserPostDto>>> getUserPostsFutures() {
        try {
            int count = restHelperUtils.getCount(allPostsUrl);
            log.info("total user posts count {}",count);
            int itr = ServiceUtils.getIterationCount(count);
            log.info("total batch size {} ",itr);
            List<Callable<List<UserPostDto>>> callables = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger(1);
            for(int i = 1; i <= itr; i++){
                callables.add(() -> prepareUserPostsGetCallAndExecute(counter));
            }
            return executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RequestExecutionException(e, ErrorCodes.ERROR_FETCHING_USER_POSTS);
        }
    }

    /**
     * creates user post in target system
     * first it queries the target system by email, if found user user ID to create the post
     * if not, it creates the user in target system, and creates the user post
     * @param request
     * @param userDto
     * @return
     */
    public CreatePostResponse createUserPost(CreatePostRequest request, UserDto userDto) {
        validateUserPost(request,userDto);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization","Bearer "+accessToken);
        try {
            CreatePostResponse createPostResponse = restHelperUtils.executePost(userPostUrl.replace("${userId}", userDto.getId() + ""),
                    UserPostDto.toUserPost(request), CreatePostResponse.class, headers,
                    MediaType.APPLICATION_JSON);

            log.info("response id {} for title {}", createPostResponse.getId(), createPostResponse.getTitle());
            log.debug("response {} ", createPostResponse);
            return createPostResponse;
        }catch (RequestExecutionException e){
            throw new RequestExecutionException(e,ErrorCodes.ERROR_CREATING_USER_POST);
        }
    }


    /**
     * process the future objects and creates the user posts.
     * @param postsFutures
     * @return
     */
    public List<UserPostDto> getUserPostList(List<Future<List<UserPostDto>>> postsFutures) {
        try {

            List<UserPostDto> userPostDtos = new ArrayList<>(postsFutures.size() * 100);
            for(Future<List<UserPostDto>> f : postsFutures){
                userPostDtos.addAll(f.get());
            }
            log.info("total user post size {} ",userPostDtos.size());
            return userPostDtos;
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestExecutionException(e,ErrorCodes.ERROR_FETCHING_USER_POSTS);
        }
    }
    /**
     * Invokes rest call to target system to read user posts
     * @param counter
     * @return
     */
    private List<UserPostDto> prepareUserPostsGetCallAndExecute(AtomicInteger counter) {
        String url = allPostsUrl
                + "?per_page=" + countPerPage + "&page=" + counter.getAndIncrement();
        log.info("url for getting user posts {}",url);
        try{
            return restHelperUtils.executeGet(url, new GenericType<List<UserPostDto>>() {
            }, MediaType.APPLICATION_JSON);
        } catch (RequestExecutionException e){
            throw new RequestExecutionException(e,ErrorCodes.ERROR_FETCHING_USER_POSTS);
        }
    }

    /**
     * Validates the data before making REST service call.
     * @param request
     * @param userDto
     */
    private void validateUserPost(CreatePostRequest request,UserDto userDto){
        Set<ErrorCodes> errorCodes = new HashSet<>();
        if(!StringUtils.hasText(request.getBody())) errorCodes.add(ErrorCodes.ERROR_BODY_SHOULDNT_BE_EMPTY);
        if(!StringUtils.hasText(request.getTitle())) errorCodes.add(ErrorCodes.ERROR_TITLE_SHOULDNT_BE_EMPTY);
        if(userDto.getId() == null) errorCodes.add(ErrorCodes.ERROR_USER_ID_MISSING);
        log.warn("request validation status {} and errors {} ",errorCodes.isEmpty(),errorCodes);
        if(!errorCodes.isEmpty())
            throw new InvalidInputException(errorCodes);
    }
}
