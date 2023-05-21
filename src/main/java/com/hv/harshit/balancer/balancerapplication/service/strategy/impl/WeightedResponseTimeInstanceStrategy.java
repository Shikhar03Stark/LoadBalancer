package com.hv.harshit.balancer.balancerapplication.service.strategy.impl;

import com.hv.harshit.balancer.balancerapplication.enums.LoadBalancerAlgorithm;
import com.hv.harshit.balancer.balancerapplication.enums.WindowSize;
import com.hv.harshit.balancer.balancerapplication.exception.NoRunningInstanceException;
import com.hv.harshit.balancer.balancerapplication.model.Instance;
import com.hv.harshit.balancer.balancerapplication.service.InstanceMetrics;
import com.hv.harshit.balancer.balancerapplication.service.strategy.LoadBalancerStrategy;
import com.hv.harshit.balancer.balancerapplication.utils.CostCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeightedResponseTimeInstanceStrategy implements LoadBalancerStrategy {

    private final LoadBalancerAlgorithm algorithm = LoadBalancerAlgorithm.WEIGHTED_RESPONSE_TIME;
    @Override
    public LoadBalancerAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public Instance pickInstance(List<Instance> availableInstance, InstanceMetrics metrics) throws NoRunningInstanceException {
        if(availableInstance.isEmpty()){
            throw new NoRunningInstanceException();
        }
        Instance pickedInstance = availableInstance.get(0);
        double minWeightedCost = CostCalculator.weightedCost(pickedInstance.getContainerId(), metrics);
        for(Instance instance: availableInstance){
            double weightedCost = CostCalculator.weightedCost(instance.getContainerId(), metrics);
            if(weightedCost < minWeightedCost){
                minWeightedCost = weightedCost;
                pickedInstance = instance;
            }
        }
        log.info("Picked instance {} with weighted cost {}", pickedInstance.getContainerId(), minWeightedCost);
        return pickedInstance;
    }
}
