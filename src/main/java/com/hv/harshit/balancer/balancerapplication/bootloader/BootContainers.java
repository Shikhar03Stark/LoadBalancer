package com.hv.harshit.balancer.balancerapplication.bootloader;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.hv.harshit.balancer.balancerapplication.configuration.DockerConfig;
import com.hv.harshit.balancer.balancerapplication.configuration.InstanceConfig;
import com.hv.harshit.balancer.balancerapplication.enums.ContainerStatus;
import com.hv.harshit.balancer.balancerapplication.model.Instance;
import com.hv.harshit.balancer.balancerapplication.service.ContainerLifecycleService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootContainers {
    private final DockerClient dockerClient;
    private final DockerConfig dockerConfig;
    private final InstanceConfig instanceConfig;
    private final ContainerLifecycleService containerLifecycleService;

    @EventListener(ContextRefreshedEvent.class)
    public void initContainers() {
        log.info("Starting up replicas");
        final String containerPrefix = instanceConfig.getContainerPrefix();
        final long replicas = instanceConfig.getReplicas();
        final ExposedPort exposedPort = ExposedPort.tcp((int) instanceConfig.getExposedPort());

        validateContainerBootUp();
        int port = (int) dockerConfig.getPortRangeMin();
        for (int replica = 0; replica < replicas; replica++, port++) {
            final String containerSuffix = UUID.randomUUID().toString();
            final String containerName = String.format("%s_%s", containerPrefix, containerSuffix);
            final String image = String.format("%s:%s", instanceConfig.getImageName(), instanceConfig.getImageTag());
            final Ports portBinding = new Ports();
            portBinding.bind(exposedPort, Ports.Binding.bindPort(port));

            final CreateContainerResponse createContainerResponse = dockerClient
                    .createContainerCmd(image)
                    .withName(containerName)
                    .withExposedPorts(ExposedPort.tcp(port), ExposedPort.tcp((int) instanceConfig.getExposedPort()))
                    .withHostConfig(HostConfig
                            .newHostConfig()
                            .withPortBindings(portBinding)
                            .withAutoRemove(true))
                    .exec();

            final Instance instance = getInstance(createContainerResponse.getId(), port, containerName);

            containerLifecycleService.startContainer(instance);
        }
    }

    private Instance getInstance(String containerId, int hostBindPort, String containerName) {
        return Instance
                .builder()
                .containerId(containerId)
                .status(ContainerStatus.RUNNING)
                .containerName(containerName)
                .exposedPort((int) instanceConfig.getExposedPort())
                .hostBindPort(hostBindPort)
                .createdAt(Instant.now().toEpochMilli())
                .build();
    }

    private void validateContainerBootUp() {
        final long portRangeMin = dockerConfig.getPortRangeMin();
        final long portRangeMax = dockerConfig.getPortRangeMax();

        if (portRangeMax - portRangeMin + 1 < instanceConfig.getReplicas()) {
            throw new RuntimeException("Number of replica are more than allowed internal ports. Abort");
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("Terminating all the containers.");
        containerLifecycleService.stopAllRunningContainers();
    }

}
