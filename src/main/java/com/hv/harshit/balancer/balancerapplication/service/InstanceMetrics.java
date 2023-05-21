package com.hv.harshit.balancer.balancerapplication.service;

import com.hv.harshit.balancer.balancerapplication.contoller.dto.InstanceMetricResponseDto;
import com.hv.harshit.balancer.balancerapplication.enums.WindowSize;

import java.util.List;

public interface InstanceMetrics {
    void setLatestResponseTime(String containerId, long responseTime);
    void increaseServeCount(String containerId);
    void increaseFailCount(String containerId);
    double getServeRatio(String containerId);
    double getAvgResponseTime(String containerId, WindowSize windowSize);
    List<WindowSize> getWindowSizes();
    long getServeCount(String containerId);
    long getFailCount(String containerId);
    InstanceMetricResponseDto getInstanceMetrics(String containerId);
    List<InstanceMetricResponseDto> getAllInstanceMetrics();
}
