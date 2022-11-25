package com.techdisqus.service;

import com.techdisqus.exceptions.ErrorCodes;
import com.techdisqus.exceptions.RequestExecutionException;
import com.techdisqus.rest.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Component
public class RestHelperUtils {

    private static Logger log = LoggerFactory.getLogger(RestHelperUtils.class);
    @Autowired
    private Client client;
    //required for mocking
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * validates response status, if it is not 200, error will be thrown
     * @param response
     * @param errorCode
     */
    public static boolean checkResponseStatus(Response response, ErrorCodes errorCode) {
        if(response.getStatus() != 200 && response.getStatus() != 0){
            throw new RequestExecutionException(errorCode);
        }
        return true;
    }

    /**
     * validates response status, if it is not 200, error will be thrown
     * @param response
     */
    public static boolean checkResponseStatus(Response response, int httpStatus) {
        if(response.getStatus() != httpStatus && response.getStatus() != 0){
            throw new RequestExecutionException(ErrorCodes.ERROR_REQ_FAILED_AT_TARGET_SYSTEM);
        }
        return true;
    }

    /**
     * fetches the count of entities ( users or posts) from target system
     * @param url
     * @return
     */
    public int getCount(String url){
        Response response = execGet(url,MediaType.APPLICATION_JSON);
        //assuming the service always provides the total count
        int count = Integer.parseInt(response.getHeaders().get("x-pagination-total").get(0).toString());
        log.debug("url {} and count {} ",url,count);
        return Integer.parseInt(response.getHeaders().get("x-pagination-total").get(0).toString());
    }

    /**
     * builds request
     * @param url
     * @return
     */
    public Invocation.Builder buildRequest(String url) {
        return client.target(url).request(MediaType.APPLICATION_JSON);
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

    public <T> T executeGet(String url,GenericType<T> genericType,String... acceptedRespTypes){
        return execGet(url, acceptedRespTypes).readEntity(genericType);
    }

    public <T> T executeGet(String url,Class<T> type,String... acceptedRespTypes){
        return execGet(url, acceptedRespTypes).readEntity(type);
    }

    private Response execGet(String url, String... acceptedRespTypes) {
        WebTarget webTarget = client.target(url);
        Invocation.Builder invocationBuilder = webTarget.request(acceptedRespTypes);
        Response response = invocationBuilder.get();
        RestHelperUtils.checkResponseStatus(response, HttpStatus.OK.value());
        return response;
    }


    public <T,R> T executePost(String url, R requestEntity, Class<T> clzz, Map<String, Object> headers,
                             String... acceptedRespTypes){
        WebTarget webTarget = client.target(url);
        Invocation.Builder invocationBuilder = webTarget.request(acceptedRespTypes);
        headers.forEach(invocationBuilder::header);
        Response response = invocationBuilder.post(Entity.entity(requestEntity, MediaType.APPLICATION_JSON));
        RestHelperUtils.checkResponseStatus(response, HttpStatus.CREATED.value());
        return response.readEntity(clzz);
    }
}
