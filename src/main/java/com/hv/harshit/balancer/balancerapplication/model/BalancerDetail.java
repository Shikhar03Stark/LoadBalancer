package com.hv.harshit.balancer.balancerapplication.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
@Builder
public class BalancerDetail {
    private long totalInstances;
    private String applicationName;
    private long exposedPort;
    private PortRange portRange;
    private List<Integer> portInUse;
    @Data
    @Builder
    public static class PortRange {
        private long min;
        private long max;
    }
}
