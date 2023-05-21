package com.hv.harshit.balancer.balancerapplication.service.strategy.impl;

import com.hv.harshit.balancer.balancerapplication.enums.LoadBalancerAlgorithm;
import com.hv.harshit.balancer.balancerapplication.enums.WindowSize;
import com.hv.harshit.balancer.balancerapplication.exception.NoRunningInstanceException;
import com.hv.harshit.balancer.balancerapplication.model.Instance;
import com.hv.harshit.balancer.balancerapplication.service.InstanceMetrics;
import com.hv.harshit.balancer.balancerapplication.service.strategy.LoadBalancerStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseTimeInstanceStrategy implements LoadBalancerStrategy {

    private final LoadBalancerAlgorithm algorithm = LoadBalancerAlgorithm.MINIMUM_RESPONSE_TIME;
    private final WindowSize windowSize = WindowSize.TEN;
    private final InstanceMetrics instanceMetrics;

    @Override
    public LoadBalancerAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public Instance pickInstance(List<Instance> availableInstance, InstanceMetrics metrics) throws NoRunningInstanceException {
        if(CollectionUtils.isEmpty(availableInstance)){
            throw new NoRunningInstanceException();
        }
        Instance pickedInstance = availableInstance.get(0);
        double minResponseTime = instanceMetrics.getAvgResponseTime(pickedInstance.getContainerId(), windowSize);
        for(Instance instance: availableInstance){
            double responseTime = instanceMetrics.getAvgResponseTime(instance.getContainerId(), windowSize);
            if(responseTime < minResponseTime){
                minResponseTime = responseTime;
                pickedInstance = instance;
            }
        }
        log.info("Picked instance {} with response time {}", pickedInstance.getContainerId(), minResponseTime);
        return pickedInstance;

    }
}
