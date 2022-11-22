package com.techdisqus.service;

import com.techdisqus.exceptions.ErrorCodes;
import com.techdisqus.exceptions.RequestExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
public class RestHelperUtils {

    @Autowired
    private Client client;
    /**
     * validates response status, if it is not 200, error will be thrown
     * @param response
     * @param errorFetchingUserPosts
     */
    public static void checkResponseStatus(javax.ws.rs.core.Response response, ErrorCodes errorFetchingUserPosts) {
        if(response.getStatus() != 200){
            throw new RequestExecutionException(errorFetchingUserPosts);
        }
    }

    /**
     * fetches the count of users from target system
     * @param url
     * @return
     */
    public int getCount(String url){
        WebTarget webTarget = client.target(url);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        javax.ws.rs.core.Response response = invocationBuilder.get();
        RestHelperUtils.checkResponseStatus(response, ErrorCodes.ERROR_WHILE_GETTING_COUNT);
        return Integer.parseInt(response.getHeaders().get("x-pagination-total").get(0).toString());
    }

    /**
     * builds request
     * @param url
     * @return
     */
    public Invocation.Builder buildRequest(String url) {
        WebTarget webTarget = client.target(url);
        return webTarget.request(MediaType.APPLICATION_JSON);
    }

    /**
     * counts the no of batches
     * @param count
     * @return
     */
    public static int getIterationCount(int count) {
        int itr = count / 100;
        itr = itr + (count % 100 == 0 ? 0 : 1);
        return itr;
    }

    public void validateCreateResponse(Response resp,ErrorCodes errorCodes) {
        if(resp.getStatus() == 401){
            throw new RequestExecutionException(ErrorCodes.ERROR_AUTH_FAILED);
        }else if(resp.getStatus() != 201){
            throw new RequestExecutionException(errorCodes);
        }
    }
}
