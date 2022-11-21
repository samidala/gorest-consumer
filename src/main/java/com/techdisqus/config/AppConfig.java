package com.techdisqus.config;

import org.glassfish.jersey.client.ClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Value("${thread.pool.count}")
    private int count;
    @Bean
   // @Scope("prototype")
    public Client client(){
        return ClientBuilder.newClient(new ClientConfig());
    }

    @Bean
    public ExecutorService executorService(){
        return Executors.newFixedThreadPool(count);
    }

}
