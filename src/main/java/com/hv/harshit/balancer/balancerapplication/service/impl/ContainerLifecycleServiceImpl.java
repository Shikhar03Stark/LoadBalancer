package com.hv.harshit.balancer.balancerapplication.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.hv.harshit.balancer.balancerapplication.mapper.InstanceMapper;
import com.hv.harshit.balancer.balancerapplication.model.Instance;
import com.hv.harshit.balancer.balancerapplication.persistance.entity.InstanceEntity;
import com.hv.harshit.balancer.balancerapplication.persistance.repository.InstanceRepository;
import com.hv.harshit.balancer.balancerapplication.service.ContainerLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContainerLifecycleServiceImpl implements ContainerLifecycleService {

    private final DockerClient dockerClient;
    private final InstanceRepository instanceRepository;
    private final InstanceMapper instanceMapper;

    @Override
    public String startContainer(Instance instance) {
        log.info("Starting container with id={}", instance.getContainerId());
        dockerClient.startContainerCmd(instance.getContainerId()).exec();
        final InstanceEntity instanceEntity = instanceMapper.map(instance);
        final InstanceEntity savedInstanceEntity = instanceRepository.save(instanceEntity);
        return savedInstanceEntity.getContainerId();
    }

    @Override
    public void stopContainer(String containerId) {
        log.info("Stopping container with id={}", containerId);
        try {
            dockerClient.stopContainerCmd(containerId).exec();
        } catch (NotFoundException exception){
            log.info("Container already stopped, skipping. error={}", exception.getMessage());
        } finally {
            instanceRepository.markStopped(containerId);
        }
    }

    @Override
    public void stopAllRunningContainers() {
        final List<InstanceEntity> runningContainers = instanceRepository.findAllRunningContainers();
        log.info("Found {} running containers", runningContainers.size());
        runningContainers
                .forEach(instanceEntity -> stopContainer(instanceEntity.getContainerId()));
    }
}
