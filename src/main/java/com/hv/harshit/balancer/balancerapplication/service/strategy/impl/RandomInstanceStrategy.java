package com.hv.harshit.balancer.balancerapplication.service.strategy.impl;

import com.hv.harshit.balancer.balancerapplication.enums.LoadBalancerAlgorithm;
import com.hv.harshit.balancer.balancerapplication.exception.NoRunningInstanceException;
import com.hv.harshit.balancer.balancerapplication.model.Instance;
import com.hv.harshit.balancer.balancerapplication.service.InstanceMetrics;
import com.hv.harshit.balancer.balancerapplication.service.strategy.LoadBalancerStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class RandomInstanceStrategy implements LoadBalancerStrategy {

    private final LoadBalancerAlgorithm algorithm = LoadBalancerAlgorithm.RANDOM;
    private final Random randomIterator = new Random();
    @Override
    public LoadBalancerAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    @Override
    public Instance pickInstance(List<Instance> availableInstance, InstanceMetrics metrics) throws NoRunningInstanceException {
        if(availableInstance.isEmpty()){
            throw new NoRunningInstanceException();
        }
        return availableInstance.get(randomIterator.nextInt(availableInstance.size()));
    }
}
