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
import java.util.concurrent.Semaphore;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoundRobinInstanceStrategy implements LoadBalancerStrategy {

    private final LoadBalancerAlgorithm algorithm = LoadBalancerAlgorithm.ROUND_ROBIN;
    private final Semaphore counterSemaphore = new Semaphore(1);
    private int currentInstanceIdx = 0;

    @Override
    public LoadBalancerAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    @Override
    public Instance pickInstance(List<Instance> availableInstance, InstanceMetrics metrics) throws NoRunningInstanceException {
        try {
            counterSemaphore.acquire();
        } catch (InterruptedException exception) {
            log.info("Thread {} waiting to acquire", Thread.currentThread().getName());
        }

        final int totalInstances = availableInstance.size();
        if(totalInstances == 0){
            throw new NoRunningInstanceException();
        }
        final Instance pickedInstance = availableInstance.get(currentInstanceIdx);
        currentInstanceIdx = (currentInstanceIdx + 1) % totalInstances;

        counterSemaphore.release();
        return pickedInstance;
    }
}
