package com.techdisqus.config;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ComponentScan(basePackages = {"com.techdisqus"})
@EnableSwagger2
@PropertySource("classpath:application.properties")
public class AppConfig implements DisposableBean {


    @Value("${rest.service.connect.timeout.inmillis}")
    private int connectTimeOutInMills;
    @Value("${thread.pool.count}")
    private int count;
    @Value("${rest.service.read.timeout.inmillis}")
    private int readTimeOutInMills;

    @Value("${rest.service.max.per.route}")
    private int maxPerRoute;

    @Value("${rest.service.max.all.routes}")
    private int maxAllRoutes;

    @Autowired
    private ApplicationContext context;

    /**
     * Creates jersey client
     * @return
     */
    @Bean
    public Client client(){

        PoolingHttpClientConnectionManager cm = getPoolingHttpClientConnectionManager();
        final ClientConfig cc = getClientConfig(cm);
        Client client = ClientBuilder.newClient(cc);
        client.property(ClientProperties.CONNECT_TIMEOUT,connectTimeOutInMills);
        client.property(ClientProperties.READ_TIMEOUT,readTimeOutInMills);

        return client;
    }

    /**
     * Jersey client config
     * @param cm
     * @return
     */
    @Bean
    public ClientConfig getClientConfig(PoolingHttpClientConnectionManager cm) {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(getConnectorProvider());
        cc.property(ApacheClientProperties.CONNECTION_MANAGER, cm);
        return cc;
    }

    /**
     * creates custom apache connection provider
     * @return
     */
    @Bean
    public ApacheConnectorProvider getConnectorProvider() {
        return new ApacheConnectorProvider();
    }

    /**
     * http connection pooling
     * @return
     */
    @Bean
    public PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxAllRoutes);
        cm.setDefaultMaxPerRoute(maxPerRoute);
        return cm;
    }

    /**
     * creates executor service
     * @return
     */
    @Bean
    public ExecutorService executorService(){
        return Executors.newFixedThreadPool(count);
    }


    @Override
    public void destroy() {
        ExecutorService executorService = context.getBean(ExecutorService.class);
        if(!executorService.isShutdown() && !executorService.isTerminated()){
            executorService.shutdown();
        }
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfo("MyApp Rest APIs",
                "APIs for MyApp.",
                "1.0",
                "Terms of service",
                new Contact("test", "www.org.com", "test@emaildomain.com"),
                "License of API",
                "API license URL",
                Collections.emptyList());
    }

    /*@Bean
    public MethodValidationPostProcessor getMethodValidationPostProcessor(){
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(this.validator());
        return processor;
    }

    @Bean
    public LocalValidatorFactoryBean validator(){
        return new LocalValidatorFactoryBean();
    }*/
}
