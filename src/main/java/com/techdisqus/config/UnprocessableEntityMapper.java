package com.techdisqus.config;

import org.springframework.web.client.HttpClientErrorException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class UnprocessableEntityMapper implements ExceptionMapper<HttpClientErrorException.UnprocessableEntity> {

    @Override
    public Response toResponse(HttpClientErrorException.UnprocessableEntity ex) {
        return Response.status(BAD_REQUEST).
                entity(ex.getMessage()).
                type("text/plain").
                build();
    }
}