package com.techdisqus.service;

import com.techdisqus.rest.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserCache {
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private Map<String,User> emailAndIdMapping = new HashMap<>();

    @Autowired
    private Client client;
    @Value("${service.user.list.url}")
    private String userListUrl;

    public void loadUsers() throws InterruptedException {
        try {
            lock.lockInterruptibly();
            WebTarget webTarget = client.target(userListUrl);//.path("employees");
            Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
            List<User> users = invocationBuilder.get(new GenericType<List<User>>(){});
            emailAndIdMapping =  users.stream().collect(Collectors.toMap(
                    User::getEmail, Function.identity()
            ));
        }finally {
            lock.unlock();
        }
    }

    public boolean isUserExists(String email){
        return emailAndIdMapping.containsKey(email);
    }
    public long getUserId(String email){
        return  emailAndIdMapping.get(email).getId();
    }
}
