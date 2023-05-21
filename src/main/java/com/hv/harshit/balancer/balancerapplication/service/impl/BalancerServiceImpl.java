package com.hv.harshit.balancer.balancerapplication.service.impl;

import com.hv.harshit.balancer.balancerapplication.configuration.DockerConfig;
import com.hv.harshit.balancer.balancerapplication.configuration.InstanceConfig;
import com.hv.harshit.balancer.balancerapplication.configuration.ProxyClientConfig;
import com.hv.harshit.balancer.balancerapplication.enums.LoadBalancerAlgorithm;
import com.hv.harshit.balancer.balancerapplication.exception.NoRunningInstanceException;
import com.hv.harshit.balancer.balancerapplication.mapper.InstanceMapper;
import com.hv.harshit.balancer.balancerapplication.model.BalancerDetail;
import com.hv.harshit.balancer.balancerapplication.model.Instance;
import com.hv.harshit.balancer.balancerapplication.persistance.entity.InstanceEntity;
import com.hv.harshit.balancer.balancerapplication.persistance.repository.InstanceRepository;
import com.hv.harshit.balancer.balancerapplication.service.BalancerService;
import com.hv.harshit.balancer.balancerapplication.service.InstanceMetrics;
import com.hv.harshit.balancer.balancerapplication.service.strategy.LoadBalancerStrategy;
import com.hv.harshit.balancer.balancerapplication.service.strategy.LoadBalancerStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalancerServiceImpl implements BalancerService {
    private final InstanceMapper instanceMapper;
    private static final String URI_SCHEME = "http";
    private final LoadBalancerStrategyFactory loadBalancerStrategyFactory;
    private final InstanceMetrics instanceMetrics;
    private final InstanceRepository instanceRepository;
    private final DockerConfig dockerConfig;
    private final InstanceConfig instanceConfig;
    private final ProxyClientConfig proxyClientConfig;
    private final WebClient proxyWebClient;

    @Override
    public BalancerDetail getBalancerDetails() {
        List<InstanceEntity> runningContainers = instanceRepository.findAllRunningContainers();
        final List<Integer> portInUse = runningContainers
                .stream()
                .map(InstanceEntity::getHostBindPort)
                .collect(Collectors.toList());

        final BalancerDetail.PortRange portRange = BalancerDetail.PortRange
                .builder()
                .min(dockerConfig.getPortRangeMin())
                .max(dockerConfig.getPortRangeMax())
                .build();

        return BalancerDetail
                .builder()
                .applicationName(instanceConfig.getImageName())
                .portInUse(portInUse)
                .portRange(portRange)
                .totalInstances(runningContainers.size())
                .exposedPort(instanceConfig.getExposedPort())
                .build();
    }

    @Override
    public ResponseEntity<String> proxyRequest(ServerHttpRequest request, String body) {
        List<Instance> openInstances = getOpenInstances();

        final LoadBalancerAlgorithm algorithm = getLoadBalancerAlgorithm(proxyClientConfig.getLoadBalancingAlgorithm());

        final LoadBalancerStrategy loadBalancer = loadBalancerStrategyFactory.loadBalancerStrategyBy(algorithm);

        final StopWatch pickingInstance = new StopWatch();
        pickingInstance.start();
        Instance pickedInstance;
        try {
            pickedInstance = loadBalancer.pickInstance(openInstances, instanceMetrics);
        } catch (NoRunningInstanceException exception) {
            pickingInstance.stop();
            return ResponseEntity.internalServerError().build();
        }
        pickingInstance.stop();

        final StopWatch responseTime = new StopWatch();
        responseTime.start();
        try {
            final ResponseEntity<String> responseEntity = forwardRequestToInstance(request, body, pickedInstance);
            responseTime.stop();
            instanceMetrics.increaseServeCount(pickedInstance.getContainerId());
            instanceMetrics.setLatestResponseTime(pickedInstance.getContainerId(), responseTime.getTime());

            return responseEntity;
        } catch (WebClientException exception) {
            log.info("Error while proxying request. error={} stackTrace={}", exception.getMessage(), ExceptionUtils.getStackTrace(exception));
            instanceMetrics.increaseFailCount(pickedInstance.getContainerId());
            return ResponseEntity.internalServerError().build();
        } finally {
            if (!responseTime.isStopped()) {
                responseTime.stop();
            }
            log.info("Instance id={} took time={}ms pickingTime={}", pickedInstance.getContainerId(), responseTime.getTime(), pickingInstance.getTime());
        }
    }

    private List<Instance> getOpenInstances() {
        return instanceRepository
                .findAllRunningContainers()
                .stream()
                .map(instanceMapper::map)
                .collect(Collectors.toList());
    }

    private LoadBalancerAlgorithm getLoadBalancerAlgorithm(String loadBalancingAlgorithm) {
        try {
            return LoadBalancerAlgorithm.valueOf(loadBalancingAlgorithm);
        } catch (IllegalArgumentException exception) {
            return LoadBalancerAlgorithm.RANDOM;
        }
    }

    private ResponseEntity<String> forwardRequestToInstance(ServerHttpRequest request, String body, Instance pickedInstance) {
        return proxyWebClient
                .method(request.getMethod())
                .uri(uriBuilder -> getUri(request, pickedInstance, uriBuilder))
                .body(BodyInserters.fromValue(body))
                .accept(MediaType.ALL)
                .retrieve()
                .toEntity(String.class)
                .block();
    }

    private static URI getUri(ServerHttpRequest request, Instance firstInstance, UriBuilder uriBuilder) {
        return uriBuilder
                .scheme(URI_SCHEME)
                .host(request.getURI().getHost())
                .port(firstInstance.getHostBindPort())
                .path(request.getURI().getRawPath())
                .query(request.getURI().getRawQuery())
                .build();
    }
}
