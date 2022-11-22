package com.techdisqus.config;

import org.apache.http.auth.AuthenticationException;
import org.springframework.web.client.HttpClientErrorException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class AutherizationMapper implements ExceptionMapper<AuthenticationException> {

    @Override
    public Response toResponse(AuthenticationException ex) {
        return Response.status(Response.Status.UNAUTHORIZED).
                entity(ex.getMessage()).
                type("text/plain").
                build();
    }
}