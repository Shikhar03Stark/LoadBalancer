package com.hv.harshit.balancer.balancerapplication.service.strategy;

import com.hv.harshit.balancer.balancerapplication.enums.LoadBalancerAlgorithm;
import com.hv.harshit.balancer.balancerapplication.exception.NoRunningInstanceException;
import com.hv.harshit.balancer.balancerapplication.model.Instance;
import com.hv.harshit.balancer.balancerapplication.service.InstanceMetrics;

import java.util.List;

public interface LoadBalancerStrategy {
    LoadBalancerAlgorithm getAlgorithm();

    Instance pickInstance(List<Instance> availableInstance, InstanceMetrics metrics) throws NoRunningInstanceException;
}
