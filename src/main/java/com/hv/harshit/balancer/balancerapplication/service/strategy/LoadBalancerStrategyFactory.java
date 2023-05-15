package com.hv.harshit.balancer.balancerapplication.service.strategy;

import com.hv.harshit.balancer.balancerapplication.enums.LoadBalancerAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class LoadBalancerStrategyFactory {
    private final Map<LoadBalancerAlgorithm, LoadBalancerStrategy> loadBalancerStrategy = new HashMap<>();

    public LoadBalancerStrategyFactory(Set<LoadBalancerStrategy> strategies){
        strategies
                .forEach(strategy -> loadBalancerStrategy.put(strategy.getAlgorithm(), strategy));
    }

    public LoadBalancerStrategy loadBalancerStrategyBy(LoadBalancerAlgorithm algorithm){
        return this.loadBalancerStrategy.get(algorithm);
    }
}
