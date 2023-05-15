package com.hv.harshit.balancer.balancerapplication.configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Data
@Configuration
@RequiredArgsConstructor
public class ProxyClientConfig {

    public static final String TRACE_ID_HEADER = "trace_id";

    @Value("${proxy.load.balance.algo:RANDOM}")
    private String loadBalancingAlgorithm;

    @Value("${proxy.client.retries}")
    private int retries;

    @Value("${proxy.client.methods}")
    private List<String> allowedMethods;

    @Bean
    public WebClient proxyWebClient(WebClient.Builder webClientBuilder){
        return webClientBuilder
                .filter(traceFilter)
                .filter(logFilter)
                .filter(httpMethodFilter)
                .build();
    }

    private ExchangeFilterFunction httpMethodFilter = (clientRequest, nextFilterFunction) -> {
        final String httpMethod = clientRequest.method().name();
        if(!allowedMethods.contains(httpMethod)){
            log.info("Request blocked. method={} path={}", httpMethod, clientRequest.url().getPath());
            return Mono.error(new HttpException(String.format("HTTP method=%s blocked", httpMethod), HttpStatus.UNAUTHORIZED));
        }
        return nextFilterFunction.exchange(clientRequest);
    };
    private ExchangeFilterFunction traceFilter = (clientRequest, nextFilterFunction) -> {
        final ClientRequest clientRequestWithTrace = ClientRequest
                .from(clientRequest)
                .header(TRACE_ID_HEADER, UUID.randomUUID().toString())
                .build();
        return nextFilterFunction.exchange(clientRequestWithTrace);
    };

    private ExchangeFilterFunction logFilter = (clientReqeust, nextFilterFunction) -> {
        final HttpMethod httpMethod = clientReqeust.method();
        final String traceId = clientReqeust.headers().getFirst(TRACE_ID_HEADER);
        final String path = clientReqeust.url().getPath();
        log.info("{} - {} - {}", traceId, httpMethod.name(), path);
        return nextFilterFunction.exchange(clientReqeust);
    };
}
