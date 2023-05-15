package com.hv.harshit.balancer.balancerapplication.service;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.hv.harshit.balancer.balancerapplication.model.Instance;
import org.springframework.transaction.annotation.Transactional;

public interface ContainerLifecycleService {
    @Transactional
    String startContainer(Instance instance);
    @Transactional
    void stopContainer(String containerId);
    @Transactional
    void stopAllRunningContainers();
}
