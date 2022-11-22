package com.techdisqus.service;

import com.techdisqus.dto.CreatePostRequest;
import com.techdisqus.exceptions.ErrorCodes;
import com.techdisqus.exceptions.RequestExecutionException;
import com.techdisqus.rest.dto.CreatePostResponse;
import com.techdisqus.rest.dto.UserDto;
import com.techdisqus.rest.dto.UserPostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.techdisqus.service.RestHelperUtils.checkResponseStatus;

@Component
public class UserPostServiceHelper {

    private static Logger log = LoggerFactory.getLogger(UserPostServiceHelper.class);
    @Value("${service.all.list.url}")
    private String allPostsUrl;

    @Autowired
    private Client client;

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

    /**
     * Invokes userposts read call in batches in multiple threads
     * @return
     */
    public List<Future<List<UserPostDto>>> getUserPostsFutures() {
        try {
            int count = restHelperUtils.getCount(allPostsUrl);
            int itr = RestHelperUtils.getIterationCount(count);
            List<Callable<List<UserPostDto>>> callables = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger(1);
            for(int i = 1; i <= itr; i++){
                callables.add(() -> processUserPostFutures(counter));
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
        WebTarget webTarget = client.target(userPostUrl.replace("${userId}", userDto.getId()+""));//.path("employees");
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("Authorization","Bearer "+accessToken);

        Response resp = invocationBuilder
                .post(Entity.entity(UserPostDto.toUserPost(request),
                MediaType.APPLICATION_JSON_TYPE));
        restHelperUtils.validateCreateResponse(resp,ErrorCodes.ERROR_CREATING_USER_POST);
        CreatePostResponse createPostResponse = resp.readEntity(CreatePostResponse.class);
        log.debug("response id {} ",createPostResponse.getId());
        return createPostResponse;
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
    private List<UserPostDto> processUserPostFutures(AtomicInteger counter) {
        String url = allPostsUrl
                + "?per_page=" + countPerPage + "&page=" + counter.getAndIncrement();
        log.info("url for getting user posts {}",url);
        javax.ws.rs.core.Response response = restHelperUtils.buildRequest(url).get();
        checkResponseStatus(response, ErrorCodes.ERROR_FETCHING_USER_POSTS);
        return response.readEntity(new GenericType<List<UserPostDto>>() {});
    }

}
