package com.techdisqus.config;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Value("${thread.pool.count}")
    private int count;
    @Value("${rest.service.connect.timeout.inmillis}")
    private int connectTimeOutInMills;

    @Value("${rest.service.read.timeout.inmillis}")
    private int readTimeOutInMills;

    @Value("${rest.service.max.per.route}")
    private int maxPerRoute;

    @Value("${rest.service.max.all.routes}")
    private int maxAllRoutes;


    @Bean
    public Client client(){

        PoolingHttpClientConnectionManager cm = getPoolingHttpClientConnectionManager();
        final ClientConfig cc = getClientConfig(cm);
        Client client = ClientBuilder.newClient(cc);
        client.property(ClientProperties.CONNECT_TIMEOUT,connectTimeOutInMills);
        client.property(ClientProperties.READ_TIMEOUT,readTimeOutInMills);

        return client;
    }
    @Bean
    public ClientConfig getClientConfig(PoolingHttpClientConnectionManager cm) {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(getConnectorProvider());
        cc.property(ApacheClientProperties.CONNECTION_MANAGER, cm);
        return cc;
    }

    @Bean
    public ApacheConnectorProvider getConnectorProvider() {
        return new ApacheConnectorProvider();
    }

    @Bean
    public PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxAllRoutes);
        cm.setDefaultMaxPerRoute(maxPerRoute);
        return cm;
    }

    @Bean
    public ExecutorService executorService(){
        return Executors.newFixedThreadPool(count);
    }

}
