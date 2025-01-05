package com.shopit.orderservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // With Load balancing
    // By default, WebClient in Spring Framework does not provide built-in load
    // balancing capabilities.
    // It is primarily an HTTP client library and does not handle load balancing.
    // Load balancing is typically
    // handled at a higher level in the application stack using frameworks such as
    // Spring Cloud LoadBalancer or
    // by deploying the application behind a load balancer.
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    // Without load balancing
    // @Bean
    // public WebClient webClient(){
    // return WebClient.builder().build();
    // }
}