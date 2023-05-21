package com.hv.harshit.balancer.balancerapplication.contoller.dto;

import com.hv.harshit.balancer.balancerapplication.enums.WindowSize;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class InstanceMetricResponseDto {
    private String containerId;
    private double serveRatio;
    private Map<WindowSize, Double> avgResponseTime;
    private long serveCount;
    private long failCount;
    private Map<String, Double> costs;
}
