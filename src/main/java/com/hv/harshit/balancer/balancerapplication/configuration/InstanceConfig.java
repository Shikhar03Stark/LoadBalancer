package com.hv.harshit.balancer.balancerapplication.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
public class InstanceConfig {

    @Value("${docker.image.name}")
    private String imageName;
    @Value("${docker.image.tag}")
    private String imageTag;
    @Value("${instance.replicas}")
    private long replicas;
    @Value("${instance.prefix}")
    private String containerPrefix;
    @Value("${instance.exposed.port}")
    private long exposedPort;
}
