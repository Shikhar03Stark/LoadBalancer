package com.hv.harshit.balancer.balancerapplication.service.strategy.impl;

import com.hv.harshit.balancer.balancerapplication.enums.LoadBalancerAlgorithm;
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
public class BestBalanceInstanceStrategy implements LoadBalancerStrategy {

    private final LoadBalancerAlgorithm algorithm = LoadBalancerAlgorithm.BEST_BALANCE;
    private final WeightedResponseTimeInstanceStrategy weightedResponseTimeInstanceStrategy;

    @Override
    public LoadBalancerAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public Instance pickInstance(List<Instance> availableInstance, InstanceMetrics metrics) throws NoRunningInstanceException {
        if (availableInstance.isEmpty()) {
            throw new NoRunningInstanceException();
        }
        Instance bestInstance = availableInstance.get(0);
        double bestCost = CostCalculator.bestCost(bestInstance.getContainerId(), metrics);
        for (Instance instance : availableInstance) {
            double cost = CostCalculator.bestCost(instance.getContainerId(), metrics);
            if (cost < bestCost) {
                bestCost = cost;
                bestInstance = instance;
            }
        }
        log.info("Picked instance {} with cost {}", bestInstance.getContainerId(), bestCost);
        return bestInstance;
    }
}
