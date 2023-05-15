package com.hv.harshit.balancer.balancerapplication.configuration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
public class DockerConfig {

    @Value("${docker.host.url:unix:///var/run/docker.sock}")
    private String dockerHost;
    @Value("${docker.port.range.min}")
    private long portRangeMin;
    @Value("${docker.port.range.max}")
    private long portRangeMax;

    @Bean
    public DockerClient dockerClient(){
        final DefaultDockerClientConfig dockerClientConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        final DockerHttpClient dockerHttpClient = new ApacheDockerHttpClient
                .Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .build();

        return DockerClientImpl.getInstance(dockerClientConfig, dockerHttpClient);
    }
}
