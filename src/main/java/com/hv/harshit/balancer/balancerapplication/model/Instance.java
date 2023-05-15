package com.hv.harshit.balancer.balancerapplication.model;

import com.hv.harshit.balancer.balancerapplication.enums.ContainerStatus;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
public class Instance {
    private String containerId;
    private String containerName;
    private long createdAt;
    private int exposedPort;
    private int hostBindPort;
    private ContainerStatus status;

}
