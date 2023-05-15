package com.hv.harshit.balancer.balancerapplication.service;

import com.hv.harshit.balancer.balancerapplication.enums.WindowSize;

public interface InstanceMetrics {
    void setLatestResponseTime(String containerId, long responseTime);
    void increaseServeCount(String containerId);
    void increaseFailCount(String containerId);
    double getServeRatio(String containerId);
    double getAvgResponseTime(String containerId, WindowSize windowSize);
}
