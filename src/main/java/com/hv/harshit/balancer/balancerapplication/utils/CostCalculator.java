package com.hv.harshit.balancer.balancerapplication.utils;

import com.hv.harshit.balancer.balancerapplication.enums.WindowSize;
import com.hv.harshit.balancer.balancerapplication.service.InstanceMetrics;

public final class CostCalculator {

    private static final double POS_INFINITY = 1e30;
    public static double weightedCost(String containerId, InstanceMetrics metrics){
        double cost = 0;
        for(WindowSize windowSize: metrics.getWindowSizes()){
            cost += metrics.getAvgResponseTime(containerId, windowSize) * (1.0 / windowSize.getLength());
        }
        return cost;
    }

    public static double bestCost(String containerId, InstanceMetrics metrics) {
        double timeComponent = weightedCost(containerId, metrics);
        double countComponent = metrics.getServeCount(containerId) + metrics.getFailCount(containerId);

        double lambda = countComponent/(1 + timeComponent);
        double timeWeight = positiveLimitingFn(lambda);
        double countWeight = 1 - timeWeight;

        double linearCost =  timeWeight * timeComponent + countWeight * countComponent;

        linearCost /= 1.0 + timeComponent + countComponent;
        linearCost *= Math.pow(lambda, 1.0 + timeWeight);
        return linearCost;
    }

    public static double positiveLimitingFn(double x){
        double exp = Double.isInfinite(Math.exp(x)) ? POS_INFINITY : Math.exp(x);
        double nr = exp - 1;
        double dr = exp + 1;
        return nr/dr;
    }
}
